/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * This authentication service uses tokens that contain a protobuf encoded token
 *
 * The token contains a [EndpointIdentity] and a token.
 *
 * If [tokenPolicy] is NONE or OPTIONAL, calls without a token will return value of "0000000000.NONE".
 *
 * The [tokenProvider] generates and validates secrets.
 */
@OptIn(ExperimentalSerializationApi::class)
class AuthenticationService(
    private val tokenProvider: TokenProvider,
    private val tokenPolicy: TokenPolicy = TokenPolicy.OPTIONAL
) {
    /**
     * Validate a token. If validation was completed without exception,
     *  - return a [EndpointIdentity] identifying the terminal for which the token is valid.
     *  - return default [EndpointIdentity] with operatorId 0 and empty terminal ID if terminal was not successfully validated.
     *  - return default [EndpointIdentity] with operatorId 0 and empty terminal ID if no or empty token provided
     *  and [tokenPolicy] is NONE or OPTIONAL
     *  - return failure if token is invalid (only for policies OPTIONAL and REQUIRED)
     *
     *  Usage:
     *  ````
     *  ````
     */
    fun validateToken(buf: ByteArray?): Result<Triple<EndpointIdentity?, ValidationResult, Instant?>> = try {

        val result = when {
            buf == null || buf.isEmpty() -> {
                val validationResult = when (tokenPolicy) {
                    TokenPolicy.NONE,
                    TokenPolicy.OPTIONAL -> ValidationResult.NOT_VALIDATED

                    TokenPolicy.REQUIRED -> ValidationResult.INVALID
                }

                Triple(null, validationResult, null)
            }

            else -> {
                val decodedToken = decodeDelimitedFromByteArray<EndpointIdentityAuthenticationToken>(buf).getOrThrow()

                val (validationResult, tokenTime) = when (tokenPolicy) {
                    TokenPolicy.NONE -> Pair(ValidationResult.NOT_VALIDATED, null)
                    TokenPolicy.OPTIONAL,
                    TokenPolicy.REQUIRED ->
                        tokenProvider
                            .validateToken(decodedToken)
                            .recover { Pair(ValidationResult.INVALID, null) }
                            .getOrThrow()
                }

                Triple(decodedToken.endpointIdentity, validationResult, tokenTime)

            }
        }

        Result.success(result)

    } catch (e: Exception) {

        Result.failure(Exception("Failed token validation.", e))
    }

    /**
     * Generate a token based on [endpointIdentity]
     *
     */
    fun generateToken(endpointIdentity: EndpointIdentity): Result<ByteArray> = try {

        val token = tokenProvider.generateToken(endpointIdentity).getOrThrow()

        val buf = encodeDelimitedToByteArray(token).getOrThrow()

        check(buf.size <= 1024) { "Endpoint authentication token is longer than 1024 bytes" }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(Exception("Failed token generation.", e))
    }
}