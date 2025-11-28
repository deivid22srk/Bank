package com.bancoseguro.app.security

object NativeCrypto {
    external fun encryptData(data: ByteArray, key: ByteArray): ByteArray
    external fun decryptData(encryptedData: ByteArray, key: ByteArray): ByteArray
    external fun hashPassword(password: String, salt: ByteArray): ByteArray
    external fun generateKey(): ByteArray
    external fun obfuscateTraffic(data: ByteArray): ByteArray
    external fun deobfuscateTraffic(obfuscated: ByteArray): ByteArray
}
