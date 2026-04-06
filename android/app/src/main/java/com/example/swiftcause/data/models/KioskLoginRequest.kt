package com.example.swiftcause.data.models

import com.google.gson.annotations.SerializedName

data class KioskLoginRequest(
    @SerializedName("kioskId")
    val kioskId: String,
    @SerializedName("accessCode")
    val accessCode: String
)
