package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object EncryptedPreferencesManager {
    private const val PREFS_FILE = "secure_admin_prefs"
    private const val KEY_ADMIN_PASS = "admin_password"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAdminPassword(context: Context, password: String) {
        try {
            getEncryptedSharedPreferences(context).edit().apply {
                putString(KEY_ADMIN_PASS, password)
                apply()
            }
        } catch (_: Exception) {}
    }

    fun getAdminPassword(context: Context): String? {
        return try {
            getEncryptedSharedPreferences(context).getString(KEY_ADMIN_PASS, null)
        } catch (_: Exception) {
            null
        }
    }

    fun savePasswordHash(context: Context, hash: String) {
        try {
            getEncryptedSharedPreferences(context).edit().apply {
                putString("admin_password_hash", hash)
                apply()
            }
        } catch (_: Exception) {}
    }

    fun getPasswordHash(context: Context): String? {
        return try {
            getEncryptedSharedPreferences(context).getString("admin_password_hash", null)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteAdminPassword(context: Context) {
        try {
            getEncryptedSharedPreferences(context).edit().apply {
                remove(KEY_ADMIN_PASS)
                apply()
            }
        } catch (_: Exception) {}
    }
}
