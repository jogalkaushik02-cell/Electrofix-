package com.example.data.security

import java.security.MessageDigest

object PasswordSecurityHelper {
    const val DEFAULT_SALT = "ElectroFix_Secure_2026"

    /**
     * Compute SHA-256 hash with salt for secure password storage in Firestore
     */
    fun hashPassword(password: String, salt: String = DEFAULT_SALT): String {
        val trimmed = password.trim()
        val combined = "$trimmed:$salt"
        val digest = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify whether an input password matches the stored SHA-256 hash
     */
    fun verifyPassword(input: String, storedHash: String?, salt: String = DEFAULT_SALT): Boolean {
        if (storedHash.isNullOrBlank()) return false
        val inputHash = hashPassword(input.trim(), salt)
        return inputHash.equals(storedHash.trim(), ignoreCase = true)
    }

    /**
     * Precomputed SHA-256 hash for default initial password "admin123"
     */
    val DEFAULT_ADMIN_HASH: String by lazy {
        hashPassword("admin123")
    }
}
