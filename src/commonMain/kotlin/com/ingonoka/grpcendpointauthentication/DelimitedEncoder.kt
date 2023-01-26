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
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@ExperimentalSerializationApi


@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> encodeDelimitedToByteArray(value: T): Result<ByteArray> = try {
    val bytes = ProtoBuf.encodeToByteArray(value)
    val header = encodeInt(bytes.size).getOrThrow()

    Result.success(header + bytes)

} catch (e: Exception) {

    Result.failure(e)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> decodeDelimitedFromByteArray(bytes: ByteArray): Result<T> = try {
    val length = decodeInt(bytes).getOrThrow()
    check(bytes.size >= length + 2) {"Not enough bytes to decode."}

    val res: T = ProtoBuf.decodeFromByteArray(bytes.drop(2).take(length).toByteArray())

    Result.success(res)

} catch (e: Exception) {

    Result.failure(e)
}


fun encodeInt(value: Int): Result<ByteArray> = try {
    require(value <= 0xFFFF)
    val array = ByteArray(2)

    array[0] = (value and 0xFF).toByte()
    array[1] = ((value ushr 8) and 0xFF).toByte()

    Result.success(array)

} catch (e: Exception) {

    Result.failure(e)
}

fun decodeInt(bytes: ByteArray): Result<Int> = try {
    require(bytes.size >= 2)

    val res = (bytes[1].toInt() and 0xFF shl 8) or bytes[0].toInt() and 0xFF

    Result.success(res)

} catch (e: Exception) {

    Result.failure(e)
}

