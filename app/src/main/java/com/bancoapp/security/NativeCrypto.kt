package com.bancoapp.security

import android.util.Log

object NativeCrypto {
    
    private const val TAG = "NativeCrypto"
    private var isInitialized = false
    
    fun initialize() {
        if (!isInitialized) {
            try {
                getSecurityToken()
                isInitialized = true
                Log.i(TAG, "Native crypto initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize native crypto", e)
            }
        }
    }
    
    external fun encryptData(data: ByteArray): ByteArray
    
    external fun decryptData(data: ByteArray): ByteArray
    
    external fun getSecurityToken(): String
    
    fun encryptString(text: String): String {
        return try {
            val encrypted = encryptData(text.toByteArray(Charsets.UTF_8))
            android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            text
        }
    }
    
    fun decryptString(encryptedText: String): String {
        return try {
            val encrypted = android.util.Base64.decode(encryptedText, android.util.Base64.NO_WRAP)
            val decrypted = decryptData(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            encryptedText
        }
    }
}
