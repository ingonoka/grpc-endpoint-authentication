/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

import io.grpc.CallCredentials
import io.grpc.Metadata
import io.grpc.Status
import java.util.concurrent.Executor

actual typealias CallCredentials = CallCredentials

val META_DATA_KEY_AUTHENTICATION: Metadata.Key<ByteArray> =
    Metadata.Key.of("AuthenticationToken${Metadata.BINARY_HEADER_SUFFIX}", Metadata.BINARY_BYTE_MARSHALLER)

/**
 * Client side authentication credentials that can be added to a gRpc call.
 *
 * Usage example:
 * ````
 * val credentials = BasicAuthenticationCallCredentials(authenticationService.generateToken(terminalIdentity))
 * val stub = HeartbeatServiceGrpc.newStub(channel).withCallCredentials(credentials)
 * ````
 */
actual class BasicAuthenticationCallCredentials actual constructor(val token: ByteArray) : CallCredentials() {

    override fun applyRequestMetadata(
        requestInfo: RequestInfo,
        executor: Executor,
        metadataApplier: MetadataApplier
    ) {
        executor.execute {
            try {
                val headers = Metadata()
                headers.put(META_DATA_KEY_AUTHENTICATION, token)
                metadataApplier.apply(headers)
            } catch (e: Throwable) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e))
            }
        }
    }

    override fun thisUsesUnstableApi() {
        // yes this is unstable :(
    }
}