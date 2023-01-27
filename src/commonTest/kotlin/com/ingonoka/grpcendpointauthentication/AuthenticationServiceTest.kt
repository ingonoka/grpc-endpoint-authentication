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
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration


class AuthenticationServiceTest {

    @Test
    fun testCreation() {

        val authService = AuthenticationService(TokenProviderV1Impl("s3cr3t", Duration.ZERO) { Instant.fromEpochSeconds(1674724963) })

        val token = authService.generateToken(EndpointIdentity("GLOBAL", byteArrayOf(1, 2, 3, 4, 5))).getOrThrow()

        assertContentEquals(
            byteArrayOf(
                37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2,
                3, 4, 5, 26, 16, -30, -61, 94, 117, 56, -78, -54, 17, 13, 98, 54, -68, 105, -51, 60, 119
            ),
            token
        )

    }

    @Test
    fun testValidation() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val authService = AuthenticationService(TokenProviderV1Impl("s3cr3t", Duration.ZERO) { expectedTokenTime }, TokenPolicy.REQUIRED)

        val expectedEndpointIdentity = EndpointIdentity("GLOBAL", byteArrayOf(1, 2, 3, 4, 5))
        val token = authService.generateToken(expectedEndpointIdentity).getOrThrow()

        val (endpointIdentity, validationResult, tokenTime) = authService.validateToken(token).getOrThrow()

        assertEquals(expectedEndpointIdentity, endpointIdentity)
        assertEquals(ValidationResult.VALID, validationResult)
        assertEquals(expectedTokenTime, tokenTime)
    }

    @Test
    fun testValidationNoToken() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val tokenProvider = TokenProviderV1Impl("s3cr3t", Duration.ZERO) { expectedTokenTime }
        val authServiceRequired = AuthenticationService(tokenProvider, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(tokenProvider, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(tokenProvider, TokenPolicy.NONE)

        assertEquals(ValidationResult.INVALID, authServiceRequired.validateToken(byteArrayOf()).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceOptional.validateToken(byteArrayOf()).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(byteArrayOf()).getOrThrow().second)

    }

    @Test
    fun testValidationWrongToken() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val tokenProvider = TokenProviderV1Impl("s3cr3t", Duration.ZERO) { expectedTokenTime }
        val authServiceRequired = AuthenticationService(tokenProvider, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(tokenProvider, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(tokenProvider, TokenPolicy.NONE)

        val token = byteArrayOf(
            37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2,
            3, 4, 5, 26, 16, -30, -61, 94, 117, 56, -78, -54, 17, 13, 98, 54, -68, 105, -51, 60, 118 /*Correct: 119*/
        )

        assertEquals(ValidationResult.INVALID, authServiceRequired.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.INVALID, authServiceOptional.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(token).getOrThrow().second)

    }

    @Test
    fun testValidationCorrectToken() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val tokenProvider = TokenProviderV1Impl("s3cr3t", Duration.ZERO) { expectedTokenTime }
        val authServiceRequired = AuthenticationService(tokenProvider, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(tokenProvider, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(tokenProvider, TokenPolicy.NONE)

        val token = byteArrayOf(
            37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2,
            3, 4, 5, 26, 16, -30, -61, 94, 117, 56, -78, -54, 17, 13, 98, 54, -68, 105, -51, 60, 119
        )

        assertEquals(ValidationResult.VALID, authServiceRequired.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.VALID, authServiceOptional.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(token).getOrThrow().second)

    }
}