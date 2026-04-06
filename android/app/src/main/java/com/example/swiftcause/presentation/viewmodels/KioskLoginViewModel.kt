package com.example.swiftcause.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swiftcause.data.api.RetrofitClient
import com.example.swiftcause.data.repository.KioskRepository
import com.example.swiftcause.domain.models.KioskSession
import com.example.swiftcause.utils.FirebaseManager
import com.example.swiftcause.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class KioskLoginUiState(
    val kioskId: String = "",
    val accessCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val kioskSession: KioskSession? = null,
    val isAuthenticated: Boolean = false,
    val networkType: String = ""
)

class KioskLoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = KioskRepository(
        apiService = RetrofitClient.kioskApiService,
        firebaseAuth = FirebaseManager.auth
    )
    
    private val _uiState = MutableStateFlow(KioskLoginUiState())
    val uiState: StateFlow<KioskLoginUiState> = _uiState.asStateFlow()
    
    init {
        checkNetworkStatus()
    }
    
    private fun checkNetworkStatus() {
        val networkType = NetworkUtils.getNetworkType(getApplication())
        _uiState.value = _uiState.value.copy(networkType = networkType)
    }
    
    fun updateKioskId(kioskId: String) {
        _uiState.value = _uiState.value.copy(
            kioskId = kioskId,
            error = null
        )
    }
    
    fun updateAccessCode(accessCode: String) {
        _uiState.value = _uiState.value.copy(
            accessCode = accessCode,
            error = null
        )
    }
    
    fun login() {
        val currentState = _uiState.value
        
        if (currentState.kioskId.isBlank() || currentState.accessCode.isBlank()) {
            _uiState.value = currentState.copy(
                error = "Please enter both Kiosk ID and Access Code"
            )
            return
        }
        
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            _uiState.value = currentState.copy(
                error = "No internet connection. Please check your network settings and try again."
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            val result = repository.authenticateKiosk(
                kioskId = currentState.kioskId.trim(),
                accessCode = currentState.accessCode.trim()
            )
            
            result.fold(
                onSuccess = { kioskSession ->
                    _uiState.value = KioskLoginUiState(
                        kioskSession = kioskSession,
                        isAuthenticated = true,
                        isLoading = false,
                        networkType = currentState.networkType
                    )
                },
                onFailure = { exception ->
                    val errorMessage = when (exception) {
                        is UnknownHostException -> {
                            "Cannot reach server. Please check:\n" +
                            "• Internet connection is active\n" +
                            "• Device can access external websites\n" +
                            "• Emulator DNS is configured (see troubleshooting guide)"
                        }
                        else -> exception.message ?: "Authentication failed. Please try again."
                    }
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun signOut() {
        repository.signOut()
        _uiState.value = KioskLoginUiState()
    }
}
