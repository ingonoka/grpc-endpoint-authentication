/*
 * Copyright (c) 2021. Ingo Noka
 * This file belongs to project grpc-user-authentication-mp.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

import io.grpc.*
import org.slf4j.LoggerFactory


/**
 * Server-side interceptor that extracts a metadata token with key [META_DATA_KEY_AUTHENTICATION] and uses
 * [authenticationService] to validate the token.
 *
 * Usage:
 * ```
 * val authenticationService = AuthenticationService(TokenProviderV1Impl(Duration.ZERO) { Clock.System.now() }, TokenPolicy.REQUIRED)
 * ServerBuilder.forPort(servicePort)
 *  .useTransportSecurity(serverCerts, serverKey)
 *  .intercept(AuthenticationInterceptor(authenticationService))
 *  .addService(heartBeatServiceImpl)
 *  .build()
 *  .start()
 *  ```
 */
class AuthenticationInterceptor(private val authenticationService: AuthenticationService) : ServerInterceptor {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    override fun <ReqT, RespT> interceptCall(
        serverCall: ServerCall<ReqT, RespT>,
        metadata: Metadata,
        serverCallHandler: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val token: ByteArray? = metadata.get(META_DATA_KEY_AUTHENTICATION)

        try {
            val (endpointIdentity, result, tokenTime) = authenticationService
                .validateToken(token)
                .getOrThrow()

            return when (result) {
                ValidationResult.NOT_VALIDATED -> {
                    if (token != null) logger.warn("Endpoint token exists, but was not validated.")
                    Contexts.interceptCall(Context.current(), serverCall, metadata, serverCallHandler)
                }

                ValidationResult.VALID -> {
                    logger.trace("Endpoint token valid: $endpointIdentity/$tokenTime")
                    Contexts.interceptCall(Context.current(), serverCall, metadata, serverCallHandler)
                }

                ValidationResult.INVALID -> {
                    throw Exception("Invalid token from endpoint: $endpointIdentity")
                }
            }

        } catch (e: Exception) {

            val exceptionMessages = collectExceptionMessages(e).joinToString(" => ")
            logger.error("Endpoint token validation failed: $exceptionMessages")

            serverCall.close(
                Status.UNAUTHENTICATED.withDescription("Rejected by Authentication Service: $exceptionMessages}"), metadata
            )
        }

        return object : ServerCall.Listener<ReqT>() {}
    }

    private fun collectExceptionMessages(e: Throwable): List<String> = buildList {
        var exception: Throwable? = e
        do {
            exception?.message?.let { add(it) } ?: add("No message")
            exception = exception?.cause
        } while (exception != null)
    }
}
