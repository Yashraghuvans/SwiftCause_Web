package com.example.swiftcause.domain.models

data class KioskSession(
    val kioskId: String,
    val kioskName: String,
    val organizationId: String?,
    val assignedCampaigns: List<String>,
    val settings: KioskSettings,
    val startTime: String
)

data class KioskSettings(
    val displayMode: DisplayMode,
    val showAllCampaigns: Boolean,
    val maxCampaignsDisplay: Int,
    val autoRotateCampaigns: Boolean
)

enum class DisplayMode {
    GRID, LIST, CAROUSEL;

    companion object {
        fun fromString(value: String): DisplayMode = when (value.lowercase()) {
            "grid" -> GRID
            "list" -> LIST
            "carousel" -> CAROUSEL
            else -> GRID
        }
    }
}
