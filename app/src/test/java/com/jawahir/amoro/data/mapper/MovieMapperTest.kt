package com.jawahir.amoro.data.mapper

import com.jawahir.amoro.data.remote.dto.GenreDto
import com.jawahir.amoro.data.remote.dto.MovieDetailDto
import com.jawahir.amoro.data.remote.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for MovieDto.toDomainOrNull() and MovieDetailDto.toDomainOrNull().
 *
 * The mapper is the boundary between untrusted API data and trusted domain
 * models. If it silently accepts null IDs or empty titles, invalid data
 * reaches the UI. These tests verify the null-safety contract.
 */
class MovieMapperTest {

    private val genreMap = mapOf(28 to "Action", 35 to "Comedy", 18 to "Drama")

    // ─── MovieDto ─────────────────────────────────────────────────────────────

    @Test
    fun `toDomainOrNull - valid dto maps all fields correctly`() {
        val dto = MovieDto(
            id = 1,
            title = "Apex",
            posterPath = "/poster.jpg",
            genreIds = listOf(28, 35),
            popularity = 72.5,
            releaseDate = "2026-04-24",
            voteAverage = 6.5,
        )

        val result = dto.toDomainOrNull(genreMap)

        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals("Apex", result.title)
        assertEquals("/poster.jpg", result.posterPath)
        assertEquals(72.5, result.popularity, 0.01)
        assertEquals("2026-04-24", result.releaseDate)
        assertEquals(6.5, result.voteAverage, 0.01)
    }

    @Test
    fun `toDomainOrNull - genres are resolved from genreMap`() {
        val dto = MovieDto(
            id = 1,
            title = "Apex",
            genreIds = listOf(28, 35),
            popularity = null,
            releaseDate = null,
            voteAverage = null,
            posterPath = null
        )

        val result = dto.toDomainOrNull(genreMap)!!

        assertEquals(2, result.genres.size)
        assertEquals("Action", result.genres.find { it.id == 28 }?.name)
        assertEquals("Comedy", result.genres.find { it.id == 35 }?.name)
    }

    @Test
    fun `toDomainOrNull - unknown genre ids are dropped`() {
        val dto = MovieDto(
            id = 1,
            title = "Apex",
            genreIds = listOf(28, 999),
            popularity = null,
            releaseDate = null,
            voteAverage = null,
            posterPath = null
        )

        val result = dto.toDomainOrNull(genreMap)!!

        // Only Action (28) mapped — 999 dropped
        assertEquals(1, result.genres.size)
        assertEquals(28, result.genres.first().id)
    }

    @Test
    fun `toDomainOrNull - null id returns null`() {
        val dto = MovieDto(
            id = null,
            title = "Apex",
            popularity = null,
            releaseDate = null,
            voteAverage = null,
            posterPath = null,
            genreIds = null
        )
        assertNull(dto.toDomainOrNull(genreMap))
    }

    @Test
    fun `toDomainOrNull - null title returns null`() {
        val dto = MovieDto(
            id = 1, title = null,
            popularity = null,
            releaseDate = null,
            voteAverage = null,
            posterPath = null,
            genreIds = null
        )
        assertNull(dto.toDomainOrNull(genreMap))
    }

    @Test
    fun `toDomainOrNull - null optional fields default to safe values`() {
        val dto = MovieDto(
            id = 1,
            title = "Apex",
            posterPath = null,
            genreIds = null,
            popularity = null,
            releaseDate = null,
            voteAverage = null,
        )

        val result = dto.toDomainOrNull(genreMap)!!

        assertEquals("", result.posterPath)
        assertTrue(result.genres.isEmpty())
        assertEquals(0.0, result.popularity, 0.01)
        assertEquals("", result.releaseDate)
        assertEquals(0.0, result.voteAverage, 0.01)
    }

    @Test
    fun `toDomainOrNull - empty genre map returns movie with empty genres`() {
        val dto = MovieDto(
            id = 1,
            title = "Apex",
            genreIds = listOf(28, 35),
            popularity = null,
            releaseDate = null,
            voteAverage = null,
            posterPath = null
        )
        val result = dto.toDomainOrNull(emptyMap())!!
        assertTrue(result.genres.isEmpty())
    }

    // ─── MovieDetailDto ───────────────────────────────────────────────────────

    @Test
    fun `MovieDetailDto toDomainOrNull - valid dto maps all fields`() {
        val dto = MovieDetailDto(
            id = 1,
            title = "Apex",
            tagline = "Prey or be preyed",
            overview = "A thriller set in the wild.",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            genres = listOf(GenreDto(id = 28, name = "Action")),
            releaseDate = "2026-04-24",
            runtime = 98,
            status = "Released",
            voteAverage = 6.5,
            voteCount = 464,
            budget = 15_000_000L,
            revenue = 28_000_000L,
            imdbId = "tt1234567",
            popularity = 72.5,
        )

        val result = dto.toDomainOrNull()!!

        assertEquals(1, result.id)
        assertEquals("Apex", result.title)
        assertEquals("tt1234567", result.imdbId)
        assertEquals(15_000_000L, result.budget)
        assertEquals(28_000_000L, result.revenue)
        assertEquals(98, result.runtime)
    }

    @Test
    fun `MovieDetailDto toDomainOrNull - null id returns null`() {
        val dto = MovieDetailDto(
            id = null,
            title = "Apex",
            tagline = null,
            overview = null,
            posterPath = null,
            backdropPath = null,
            genres = null,
            releaseDate = null,
            runtime = null,
            status = null,
            voteAverage = null,
            voteCount = null,
            budget = null,
            revenue = null,
            imdbId = null,
            popularity = null
        )
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `MovieDetailDto toDomainOrNull - null title returns null`() {
        val dto = MovieDetailDto(
            id = 1,
            title = null,
            tagline = null,
            overview = null,
            posterPath = null,
            backdropPath = null,
            genres = null,
            releaseDate = null,
            runtime = null,
            status = null,
            voteAverage = null,
            voteCount = null,
            budget = null,
            revenue = null,
            imdbId = null,
            popularity = null
        )
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `MovieDetailDto toDomainOrNull - null optional fields default safely`() {
        val dto = MovieDetailDto(
            id = 1,
            title = "Apex",
            tagline = null,
            overview = null,
            posterPath = null,
            backdropPath = null,
            genres = null,
            releaseDate = null,
            runtime = null,
            status = null,
            voteAverage = null,
            voteCount = null,
            budget = null,
            revenue = null,
            imdbId = null,
            popularity = null
        )
        val result = dto.toDomainOrNull()!!

        assertEquals("", result.tagline)
        assertEquals("", result.overview)
        assertEquals("", result.posterPath)
        assertEquals("", result.backdropPath)
        assertTrue(result.genres.isEmpty())
        assertEquals(0, result.runtime)
        assertEquals(0L, result.budget)
        assertEquals(0L, result.revenue)
        assertEquals("", result.imdbId)
    }

    // ─── GenreDto ─────────────────────────────────────────────────────────────

    @Test
    fun `GenreDto toDomainOrNull - valid dto maps correctly`() {
        val dto = GenreDto(id = 28, name = "Action")
        val result = dto.toDomainOrNull()!!
        assertEquals(28, result.id)
        assertEquals("Action", result.name)
    }

    @Test
    fun `GenreDto toDomainOrNull - null id returns null`() {
        assertNull(GenreDto(id = null, name = "Action").toDomainOrNull())
    }

    @Test
    fun `GenreDto toDomainOrNull - null name returns null`() {
        assertNull(GenreDto(id = 28, name = null).toDomainOrNull())
    }
}