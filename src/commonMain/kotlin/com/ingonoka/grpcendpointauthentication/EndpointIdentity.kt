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
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@Serializable
data class EndpointIdentity(
    @ProtoNumber(1)
    val domain: String,
    @ProtoNumber(2)
    val identifier: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EndpointIdentity

        if (domain != other.domain) return false
        if (!identifier.contentEquals(other.identifier)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = domain.hashCode()
        result = 31 * result + identifier.contentHashCode()
        return result
    }

    override fun toString(): String =  "$domain: ${identifier.joinToString("", limit = 40) { "%02X".format(it) }}"

}