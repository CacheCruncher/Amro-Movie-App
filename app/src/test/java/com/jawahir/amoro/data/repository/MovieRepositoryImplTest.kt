package com.jawahir.amoro.data.repository

import app.cash.turbine.test
import com.google.gson.Gson
import com.jawahir.amoro.data.remote.api.TmdbApiService
import com.jawahir.amoro.domain.result.NetworkResult.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
class MovieRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        val apiService = retrofit.create(TmdbApiService::class.java)
        repository = MovieRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun readJson(fileName: String): String =
        javaClass.classLoader!!.getResourceAsStream(fileName)!!
            .bufferedReader()
            .readText()

    private fun enqueueSuccess(fileName: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(readJson(fileName))
        )
    }

    private fun enqueueError(code: Int) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status_code":$code,"status_message":"error"}""")
        )
    }

    // ─── Trending ────────────────────────────────────────────────────────────

    @Test
    fun `getTrendingMovies - emits progressively`() = runTest {
        enqueueSuccess("genres_response.json")
        enqueueSuccess("trending_page1_response.json")
        enqueueSuccess("trending_page2_response.json")

        repository.getTrendingMovies().test {
            val first = awaitItem() as Success
            assertEquals(2, first.data.size)

            val second = awaitItem() as Success
            assertTrue(second.data.size >= 2) // cumulative growth

            cancelAndIgnoreRemainingEvents() // IMPORTANT
        }
    }

    @Test
    fun `getTrendingMovies - maps genres correctly`() = runTest {
        enqueueSuccess("genres_response.json")

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"page":1,"total_pages":1,"results":[
                        {"id":1,"title":"Apex","genre_ids":[28],
                         "popularity":1.0,"release_date":"2026","vote_average":7.0}
                    ]}"""
                )
        )

        repository.getTrendingMovies().test {
            val result = awaitItem() as Success
            assertEquals("Action", result.data.first().genres.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTrendingMovies - http error`() = runTest {
        enqueueError(500)

        repository.getTrendingMovies().test {
            val result = awaitItem()
            assertTrue(result is HttpError)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTrendingMovies - network error`() = runTest {
        enqueueSuccess("genres_response.json")

        mockWebServer.enqueue(
            MockResponse().setSocketPolicy(
                okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START
            )
        )

        repository.getTrendingMovies().test {
            val result = awaitItem()
            assertTrue(result is NetworkError)

            cancelAndIgnoreRemainingEvents()
        }
    }
}*/
