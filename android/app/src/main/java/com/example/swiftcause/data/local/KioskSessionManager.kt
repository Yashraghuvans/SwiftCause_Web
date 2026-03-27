package com.example.swiftcause.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.swiftcause.domain.models.DisplayMode
import com.example.swiftcause.domain.models.KioskSession
import com.example.swiftcause.domain.models.KioskSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kiosk_session")

@Serializable
data class KioskSessionData(
    val kioskId: String,
    val kioskName: String,
    val organizationId: String?,
    val assignedCampaigns: List<String>,
    val displayMode: String,
    val showAllCampaigns: Boolean,
    val maxCampaignsDisplay: Int,
    val autoRotateCampaigns: Boolean,
    val startTime: String
)

class KioskSessionManager(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val KIOSK_SESSION_KEY = stringPreferencesKey("kiosk_session")
    }
    
    suspend fun saveSession(session: KioskSession) {
        val sessionData = KioskSessionData(
            kioskId = session.kioskId,
            kioskName = session.kioskName,
            organizationId = session.organizationId,
            assignedCampaigns = session.assignedCampaigns,
            displayMode = session.settings.displayMode.name,
            showAllCampaigns = session.settings.showAllCampaigns,
            maxCampaignsDisplay = session.settings.maxCampaignsDisplay,
            autoRotateCampaigns = session.settings.autoRotateCampaigns,
            startTime = session.startTime
        )
        
        context.dataStore.edit { preferences ->
            preferences[KIOSK_SESSION_KEY] = json.encodeToString(sessionData)
        }
    }
    
    fun getSession(): Flow<KioskSession?> = context.dataStore.data.map { preferences ->
        val sessionJson = preferences[KIOSK_SESSION_KEY]
        sessionJson?.let {
            try {
                val sessionData = json.decodeFromString<KioskSessionData>(it)
                KioskSession(
                    kioskId = sessionData.kioskId,
                    kioskName = sessionData.kioskName,
                    organizationId = sessionData.organizationId,
                    assignedCampaigns = sessionData.assignedCampaigns,
                    settings = KioskSettings(
                        displayMode = DisplayMode.valueOf(sessionData.displayMode),
                        showAllCampaigns = sessionData.showAllCampaigns,
                        maxCampaignsDisplay = sessionData.maxCampaignsDisplay,
                        autoRotateCampaigns = sessionData.autoRotateCampaigns
                    ),
                    startTime = sessionData.startTime
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KIOSK_SESSION_KEY)
        }
    }
}
