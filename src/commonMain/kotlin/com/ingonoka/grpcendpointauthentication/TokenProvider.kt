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


/**
 * Interface implemented by classes that verify the validity of a secret provided by an endpoint
 */
interface TokenProvider {
    /**
     * Return true of [endpointIdentityAuthenticationToken] is valid token.
     *
     * Return false otherwise
     */
    fun validateToken(endpointIdentityAuthenticationToken: EndpointIdentityAuthenticationToken): Result<Pair<ValidationResult, Instant?>>
    /**
     * Generate a token based on [endpointIdentity]
     */
    fun generateToken(endpointIdentity: EndpointIdentity): Result<EndpointIdentityAuthenticationToken>

}