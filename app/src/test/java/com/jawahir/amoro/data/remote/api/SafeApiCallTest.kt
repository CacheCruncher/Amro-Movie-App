package com.jawahir.amoro.data.remote.api

import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.result.NetworkResult.HttpError
import com.jawahir.amoro.domain.result.NetworkResult.NetworkError
import com.jawahir.amoro.domain.result.NetworkResult.Success
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * Tests for safeApiCall().
 *
 * WHY TEST THIS?
 * safeApiCall is the single entry point for all network error handling.
 * If it maps exceptions incorrectly, every network operation in the app
 * returns the wrong error type. High value, low complexity to test.
 *
 */
class SafeApiCallTest {

    // Success cases

    @Test
    fun `safeApiCall - successful response with body returns Success`() = runTest {
        val response = mockk<Response<String>> {
            every { isSuccessful } returns true
            every { body() } returns "hello"
            every { code() } returns 200
        }

        val result = safeApiCall { response }

        assertTrue(result is Success)
        assertEquals("hello", (result as Success).data)
    }

    @Test
    fun `safeApiCall - successful response with null body returns HttpError 204`() = runTest {
        val response = mockk<Response<String>> {
            every { isSuccessful } returns true
            every { body() } returns null
            every { code() } returns 204
            every { message() } returns "No Content"
        }

        val result = safeApiCall { response }

        assertTrue(result is HttpError)
        assertEquals(204, (result as HttpError).code)
    }

    // HTTP error cases

    @Test
    fun `safeApiCall - 500 response returns HttpError with code 500`() = runTest {
        val response = mockk<Response<String>> {
            every { isSuccessful } returns false
            every { code() } returns 500
            every { message() } returns "Internal Server Error"
            every { body() } returns null
        }

        val result = safeApiCall { response }

        assertTrue(result is HttpError)
        assertEquals(500, (result as HttpError).code)
        assertEquals("Internal Server Error", result.message)
    }

    // Network error cases

    @Test
    fun `safeApiCall - IOException returns NetworkError`() = runTest {
        val result = safeApiCall<String> { throw IOException("No internet") }

        assertTrue(result is NetworkError)
        assertTrue((result as NetworkError).throwable is IOException)
    }

    @Test
    fun `safeApiCall - generic Exception returns NetworkError`() = runTest {
        val result = safeApiCall<String> { throw RuntimeException("Unexpected") }

        assertTrue(result is NetworkResult.UnknownError)
        assertTrue((result as NetworkResult.UnknownError).throwable is RuntimeException)
    }

    @Test
    fun `safeApiCall - null message in error response uses fallback`() = runTest {
        val response = mockk<Response<String>> {
            every { isSuccessful } returns false
            every { code() } returns 503
            every { body() } returns null
            every { message() } returns null
        }

        val result = safeApiCall { response }

        assertTrue(result is HttpError)
        assertEquals("Unknown server error", (result as HttpError).message)
    }
}