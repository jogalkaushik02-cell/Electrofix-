package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIconForCategory(categoryKey: String): ImageVector {
        return when (categoryKey.lowercase()) {
            "screen", "screens & parts" -> Icons.Outlined.PhoneAndroid
            "battery", "batteries & power" -> Icons.Outlined.BatteryChargingFull
            "charger", "chargers & cables" -> Icons.Outlined.Bolt
            "cable" -> Icons.Outlined.Cable
            "tool", "repair tools" -> Icons.Outlined.Build
            "storage", "pc & storage" -> Icons.Outlined.Memory
            "audio", "audio & gadgets" -> Icons.Outlined.Headphones
            "laptop", "pc" -> Icons.Outlined.Laptop
            "tv", "tv & monitor" -> Icons.Outlined.Tv
            else -> Icons.Outlined.Devices
        }
    }
}
