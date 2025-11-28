package com.bancoseguro.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveEncryptionKey(key: ByteArray) {
        prefs.edit().putString("encryption_key", bytesToHex(key)).apply()
    }

    fun getEncryptionKey(): ByteArray? {
        val hexKey = prefs.getString("encryption_key", null) ?: return null
        return hexToBytes(hexKey)
    }

    fun getOrCreateEncryptionKey(): ByteArray {
        return getEncryptionKey() ?: run {
            val newKey = NativeCrypto.generateKey()
            saveEncryptionKey(newKey)
            newKey
        }
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    fun savePasswordHash(hash: ByteArray) {
        prefs.edit().putString("password_hash", bytesToHex(hash)).apply()
    }

    fun getPasswordHash(): ByteArray? {
        val hexHash = prefs.getString("password_hash", null) ?: return null
        return hexToBytes(hexHash)
    }

    fun saveSalt(salt: ByteArray) {
        prefs.edit().putString("salt", bytesToHex(salt)).apply()
    }

    fun getSalt(): ByteArray? {
        val hexSalt = prefs.getString("salt", null) ?: return null
        return hexToBytes(hexSalt)
    }

    fun getOrCreateSalt(): ByteArray {
        return getSalt() ?: run {
            val newSalt = ByteArray(32)
            SecureRandom().nextBytes(newSalt)
            saveSalt(newSalt)
            newSalt
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getUsername() != null && getPasswordHash() != null
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
