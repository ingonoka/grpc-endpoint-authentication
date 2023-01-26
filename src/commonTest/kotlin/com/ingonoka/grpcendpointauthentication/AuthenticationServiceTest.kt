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

        val authService = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { Instant.fromEpochSeconds(1674724963) })

        val token = authService.generateToken(EndpointIdentity("GLOBAL", byteArrayOf(1, 2, 3, 4, 5))).getOrThrow()

        assertContentEquals(
            byteArrayOf(
                37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2, 3, 4, 5, 26, 16, 7, -59, 40, -67,
                -79, -22, 16, -91, -99, 18, 126, -89, -84, 10, -67, -93
            ),
            token
        )

    }

    @Test
    fun testValidation() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val authService = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.REQUIRED)

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
        val authServiceRequired = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.NONE)

        assertEquals(ValidationResult.INVALID, authServiceRequired.validateToken(byteArrayOf()).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceOptional.validateToken(byteArrayOf()).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(byteArrayOf()).getOrThrow().second)

    }

    @Test
    fun testValidationWrongToken() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val authServiceRequired = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.NONE)

        val token = byteArrayOf(
            37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2, 3, 4, 5, 26, 16, 7, -59, 40, -67,
            -79, -22, 16, -91, -99, 18, 126, -89, -84, 10, -67, -92 /*Correct: -93*/
        )

        assertEquals(ValidationResult.INVALID, authServiceRequired.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.INVALID, authServiceOptional.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(token).getOrThrow().second)

    }

    @Test
    fun testValidationCorrectToken() {

        val expectedTokenTime = Instant.fromEpochSeconds(1674724963)
        val authServiceRequired = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.REQUIRED)
        val authServiceOptional = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.OPTIONAL)
        val authServiceNone = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { expectedTokenTime }, TokenPolicy.NONE)

        val token = byteArrayOf(
            37, 0, 8, 1, 18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2, 3, 4, 5, 26, 16, 7, -59, 40, -67,
            -79, -22, 16, -91, -99, 18, 126, -89, -84, 10, -67, -93
        )

        assertEquals(ValidationResult.VALID, authServiceRequired.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.VALID, authServiceOptional.validateToken(token).getOrThrow().second)
        assertEquals(ValidationResult.NOT_VALIDATED, authServiceNone.validateToken(token).getOrThrow().second)

    }
}