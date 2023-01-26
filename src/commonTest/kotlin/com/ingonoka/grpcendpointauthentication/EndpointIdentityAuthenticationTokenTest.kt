/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.ingonoka.grpcendpointauthentication

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class EndpointIdentityAuthenticationTokenTest {

    @Test
    fun encodeComplete() {

        val id = EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))

        val tokenIn = EndpointIdentityAuthenticationToken(1, id, byteArrayOf(1,2,3))

        val bytes = ProtoBuf.encodeToByteArray(tokenIn)

        println(bytes.toList())

        val tokenOut = ProtoBuf.decodeFromByteArray<EndpointIdentityAuthenticationToken>(bytes)

        assertEquals(tokenIn, tokenOut)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun encodeWithoutRequiredFields() {

        val bytes = byteArrayOf(18, 15, 10, 6, 71, 76, 79, 66, 65, 76, 18, 5, 1, 2, 3, 4, 5, 26, 3, 1, 2, 3)

        assertFailsWith<MissingFieldException> { ProtoBuf.decodeFromByteArray<EndpointIdentityAuthenticationToken>(bytes) }
    }

}