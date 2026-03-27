package com.example.swiftcause.data.api

import com.example.swiftcause.data.models.KioskLoginRequest
import com.example.swiftcause.data.models.KioskLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface KioskApiService {
    @POST("kioskLogin")
    suspend fun kioskLogin(
        @Body request: KioskLoginRequest
    ): Response<KioskLoginResponse>
}
