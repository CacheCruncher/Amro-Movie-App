package com.jawahir.amoro.domain.result

/**
 * Represents every possible outcome of a network operation.
 *
 * Use `out T` (Covariance) to allow subtyping. This enables a `NetworkResult<Nothing>`
 * (like errors) to be treated as a `NetworkResult<T>` for any type `T`.
 *
 * ### Usage Example:
 * ```kotlin
 * fun getUsername(): NetworkResult<String> {
 *     return if (isLoggedIn) {
 *         NetworkResult.Success("Alice") // Returns NetworkResult<String>
 *     } else {
 *         NetworkResult.HttpError(401, "Unauthorized") // Returns NetworkResult<Nothing>
 *     }
 * }
 */
sealed interface NetworkResult<out T>{
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class HttpError(val code: Int, val message: String) : NetworkResult<Nothing>
    data class NetworkError(val throwable: Throwable) : NetworkResult<Nothing>
    data class UnknownError(val throwable: Throwable) : NetworkResult<Nothing>
}