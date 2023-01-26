/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.grpcendpointauthentication

/**
 * Result of validating a token
 */
enum class ValidationResult {
    /**
     * Token wasn't validated (usually if policy is NONE anda token was provided)
     */
    NOT_VALIDATED,

    /**
     * Token is valid
     */
    VALID,

    /**
     * Token is invalid
     */
    INVALID
}