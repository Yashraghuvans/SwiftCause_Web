package com.example.swiftcause.data.models

import com.example.swiftcause.domain.models.DisplayMode
import com.example.swiftcause.domain.models.KioskSession
import com.example.swiftcause.domain.models.KioskSettings
import java.text.SimpleDateFormat
import java.util.*

fun KioskDataDto.toDomainModel(): KioskSession {
    return KioskSession(
        kioskId = id,
        kioskName = name,
        organizationId = organizationId,
        assignedCampaigns = assignedCampaigns,
        settings = KioskSettings(
            displayMode = DisplayMode.fromString(settings.displayMode),
            showAllCampaigns = settings.showAllCampaigns,
            maxCampaignsDisplay = settings.maxCampaignsDisplay,
            autoRotateCampaigns = settings.autoRotateCampaigns
        ),
        startTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
    )
}
