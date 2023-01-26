/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class TokenProviderV1ImplTest {

    @Test
    fun testCreateToken() {


        val tokenProvider = TokenProviderV1Impl(Duration.ZERO) { Instant.fromEpochSeconds(1674724963) }

        val token = tokenProvider.generateToken(EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))).getOrThrow()

        assertEquals(1, token.version)
        assertEquals("GLOBAL", token.endpointIdentity.domain)
        assertContentEquals(byteArrayOf(1,2,3,4,5), token.endpointIdentity.identifier)
        assertContentEquals(byteArrayOf(7,-59,40,-67,-79,-22,16,-91,-99,18,126,-89,-84,10,-67,-93), token.encryptedSecret)

    }

    @Test
    fun testValidateToken() {

        val tokenInstance = Instant.fromEpochSeconds(1674724963)

        val tokenProvider = TokenProviderV1Impl(Duration.ZERO) { tokenInstance }

        val token = tokenProvider.generateToken(EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))).getOrThrow()

        assertEquals(ValidationResult.VALID, tokenProvider.validateToken(token).getOrThrow().first)
        assertEquals(tokenInstance, tokenProvider.validateToken(token).getOrThrow().second)

    }

    @Test
    fun testValidateWithTimeDifference() {

        val tokenInstance = Clock.System.now() - 11.seconds

        val tokenProvider10Seconds = TokenProviderV1Impl(10.seconds) { tokenInstance }
        val tokenProvider20Seconds = TokenProviderV1Impl(20.seconds) { tokenInstance }

        val token = tokenProvider10Seconds.generateToken(EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))).getOrThrow()

        assertEquals(ValidationResult.INVALID, tokenProvider10Seconds.validateToken(token).getOrThrow().first)
        assertEquals(ValidationResult.VALID, tokenProvider20Seconds.validateToken(token).getOrThrow().first)
    }
}