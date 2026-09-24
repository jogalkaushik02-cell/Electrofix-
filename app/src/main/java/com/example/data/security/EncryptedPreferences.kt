package com.example.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object EncryptedPreferencesManager {

    private const val ADMIN_PREFS_NAME = "admin_prefs_encrypted"

    fun getEncryptedSharedPreferences(context: Context) = EncryptedSharedPreferences.create(
        context,
        ADMIN_PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAdminPassword(context: Context, password: String) {
        try {
            getEncryptedSharedPreferences(context).edit().apply {
                putString("admin_password", password)
                apply()
            }
        } catch (_: Exception) {}
    }

    fun getAdminPassword(context: Context): String? {
        return try {
            getEncryptedSharedPreferences(context).getString("admin_password", null)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteAdminPassword(context: Context) {
        try {
            getEncryptedSharedPreferences(context).edit().apply {
                remove("admin_password")
                apply()
            }
        } catch (_: Exception) {}
    }
}
