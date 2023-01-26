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


class EndpointIdentityTest {

    @Test
    fun encodeComplete() {

        val idIn =EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))

        val bytes = ProtoBuf.encodeToByteArray(idIn)

        val idOut = ProtoBuf.decodeFromByteArray<EndpointIdentity>(bytes)

        assertEquals(idIn, idOut)
    }

    @Test
    fun encodeWithoutOptionalFields() {

        val idIn = EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5))

        val bytes = ProtoBuf.encodeToByteArray(idIn)

        println(bytes.toList())

        val idOut = ProtoBuf.decodeFromByteArray<EndpointIdentity>(bytes)

        assertEquals(idIn, idOut)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun encodeWithoutRequiredFields() {

        val bytes = byteArrayOf(/*10, 6, 71, 76, 79, 66, 65, 76, */18, 5, 1, 2, 3, 4, 5)

        assertFailsWith<MissingFieldException> { ProtoBuf.decodeFromByteArray<EndpointIdentity>(bytes) }
    }

}