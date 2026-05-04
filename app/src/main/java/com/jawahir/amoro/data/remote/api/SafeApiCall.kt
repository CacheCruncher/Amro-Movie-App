package com.jawahir.amoro.data.remote.api

import com.jawahir.amoro.domain.result.NetworkResult
import retrofit2.Response
import java.io.IOException

/**
 * Executes a network request safely and transforms the [Response] into a [NetworkResult].
 *
 * @param T The expected type of the response body.
 * @param block A suspend lambda that executes the Retrofit API call.
 * @return A [NetworkResult] representing the final outcome.
 */
suspend fun <T> safeApiCall(
    block: suspend () -> Response<T>,
): NetworkResult<T> = try {
    val response = block()
    val body = response.body()

    if (response.isSuccessful) {
        if (body != null) {
            NetworkResult.Success(body)
        } else {
            // Success status (e.g., 204 No Content) but body is null
            NetworkResult.HttpError(
                code = response.code(),
                message = "Response body was null",
            )
        }
    } else {
        // Map 4xx/5xx errors
        NetworkResult.HttpError(
            code = response.code(),
            message = response.message()?.takeIf { it.isNotBlank() } ?: "Unknown server error"
        )
    }
} catch (e: IOException) {
    // Network connectivity issues (timeout, DNS, etc.)
    NetworkResult.NetworkError(e)
} catch (e: Exception) {
    // Serialization errors or unexpected logic crashes
    NetworkResult.UnknownError(e)
}