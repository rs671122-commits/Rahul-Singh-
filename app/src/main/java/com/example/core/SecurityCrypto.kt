package com.example.core

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Cryptographic helper for PIN/passcode encryption (SHA-256 + 16-byte random salt)
 * and 6-digit OTP generation/verification digests.
 */
object SecurityCrypto {
    private val secureRandom = SecureRandom()

    fun hashSecret(rawSecret: String): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val saltEncoded = Base64.getEncoder().encodeToString(salt)
        val digest = sha256Hex("$saltEncoded:$rawSecret")
        return "sha256:$saltEncoded:$digest"
    }

    fun verifySecret(rawSecret: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 3 || parts[0] != "sha256") return false
        val saltEncoded = parts[1]
        val expectedDigest = parts[2]
        val actualDigest = sha256Hex("$saltEncoded:$rawSecret")
        return MessageDigest.isEqual(
            expectedDigest.toByteArray(Charsets.UTF_8),
            actualDigest.toByteArray(Charsets.UTF_8)
        )
    }

    fun generateOtpCode(): String {
        val code = 100000 + secureRandom.nextInt(900000)
        return code.toString()
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
