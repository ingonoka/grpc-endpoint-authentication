/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:JvmName("AndroidTokenProviderV1ImplKt")

package com.ingonoka.grpcendpointauthentication

import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


actual typealias  SecretKey = SecretKey

actual fun decrypt(key: SecretKey, encryptedSecret: ByteArray): Result<ByteArray> = try {

    val aesCipher = Cipher.getInstance(EncryptionAlgorithm)

    val ivSpec = IvParameterSpec(ByteArray(16))

    aesCipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

    val decryptedSecret = aesCipher.doFinal(encryptedSecret)

    Result.success(decryptedSecret)

} catch (e: Exception) {

    Result.failure(Exception("Failed to decrypt secret in authentication token."))
}

actual fun encrypt(key: SecretKey, secret: ByteArray): Result<ByteArray> = try {

    val aesCipher = Cipher.getInstance(EncryptionAlgorithm)

    val ivSpec = IvParameterSpec(ByteArray(16))

    aesCipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

    val encryptedSecret: ByteArray = aesCipher.doFinal(secret)

    Result.success(encryptedSecret)

} catch (e: Exception) {

    Result.failure(Exception("Failed to encrypt secret for authentication token."))
}

/**
 * Generate a key using the PBKDF2 with operatorId as password and terminal ID as salt.
 */
actual fun generateKey(
    secretKeys: HashMap<EndpointIdentity, SecretKey>,
    endpointIdentity: EndpointIdentity
): SecretKey = secretKeys.getOrPut(endpointIdentity) {
    val salt = endpointIdentity.identifier
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val password = endpointIdentity.domain.toCharArray()
    val spec: KeySpec = PBEKeySpec(password, salt, 1000, 128)
    SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}