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
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
class EndpointIdentityAuthenticationToken @OptIn(ExperimentalSerializationApi::class) constructor(
    // Version identifies the algorithm used to derive and verify the secret
    @ProtoNumber(1)
    val version: Int,
    @ProtoNumber(2)
    val endpointIdentity: EndpointIdentity,
    @ProtoNumber(3)
    val encryptedSecret: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EndpointIdentityAuthenticationToken

        if (version != other.version) return false
        if (endpointIdentity != other.endpointIdentity) return false
        if (!encryptedSecret.contentEquals(other.encryptedSecret)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + endpointIdentity.hashCode()
        result = 31 * result + encryptedSecret.contentHashCode()
        return result
    }
}