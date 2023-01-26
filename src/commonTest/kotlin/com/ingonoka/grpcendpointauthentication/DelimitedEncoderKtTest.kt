/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DelimitedEncoderKtTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testEncode() {

        val token = EndpointIdentityAuthenticationToken(
            1,
            EndpointIdentity("GLOBAL", byteArrayOf(1,2,3,4,5)),
            byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6)
        )
        val buf = encodeDelimitedToByteArray(token).getOrThrow()

        println(buf.joinToString { "0x%02X".format(it) })

        val expected = byteArrayOf(
            0x25, 0x00, 0x08, 0x01, 0x12, 0x0F, 0x0A, 0x06, 0x47, 0x4C, 0x4F, 0x42, 0x41, 0x4C, 0x12, 0x05,
            0x01, 0x02, 0x03, 0x04, 0x05, 0x1A, 0x10, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06
        )

        assertArrayEquals(expected, buf)

        var decoded = decodeDelimitedFromByteArray<EndpointIdentityAuthenticationToken>(buf).getOrThrow()

        assertEquals(token, decoded)

        decoded = decodeDelimitedFromByteArray<EndpointIdentityAuthenticationToken>(buf + byteArrayOf(1, 2, 3)).getOrThrow()

        assertEquals(token, decoded)

        assertFailsWith<IllegalStateException> {
            decodeDelimitedFromByteArray<EndpointIdentityAuthenticationToken>(buf.dropLast(1).toByteArray()).getOrThrow()
        }
    }
}