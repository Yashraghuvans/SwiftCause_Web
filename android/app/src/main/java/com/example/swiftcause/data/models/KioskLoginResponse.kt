package com.example.swiftcause.data.models

import com.google.gson.annotations.SerializedName

data class KioskLoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("token")
    val token: String?,
    @SerializedName("kioskData")
    val kioskData: KioskDataDto?,
    @SerializedName("error")
    val error: String?
)

data class KioskDataDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizationId")
    val organizationId: String?,
    @SerializedName("assignedCampaigns")
    val assignedCampaigns: List<String>,
    @SerializedName("settings")
    val settings: KioskSettingsDto
)

data class KioskSettingsDto(
    @SerializedName("displayMode")
    val displayMode: String,
    @SerializedName("showAllCampaigns")
    val showAllCampaigns: Boolean,
    @SerializedName("maxCampaignsDisplay")
    val maxCampaignsDisplay: Int,
    @SerializedName("autoRotateCampaigns")
    val autoRotateCampaigns: Boolean
)
