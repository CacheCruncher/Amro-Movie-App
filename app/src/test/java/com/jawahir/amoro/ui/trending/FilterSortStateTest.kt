package com.jawahir.amoro.ui.trending

import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for FilterSortState.applyTo().
 */
class FilterSortStateTest {

    // ─── Test data ───────────────────────────────────────────────────────────

    private val action   = Genre(id = 28, name = "Action")
    private val comedy   = Genre(id = 35,  name = "Comedy")
    private val drama    = Genre(id = 18,  name = "Drama")

    private fun movie(
        id: Int,
        title: String,
        popularity: Double,
        releaseDate: String,
        genres: List<Genre> = emptyList(),
    ) = Movie(
        id = id,
        title = title,
        posterPath = "",
        genres = genres,
        popularity = popularity,
        releaseDate = releaseDate,
        voteAverage = 7.0,
    )

    private val movies = listOf(
        movie(1, "Cobra",      popularity = 80.0, releaseDate = "2024-03-01", genres = listOf(action)),
        movie(2, "Alpha",      popularity = 50.0, releaseDate = "2023-01-15", genres = listOf(action, comedy)),
        movie(3, "Zeta",       popularity = 90.0, releaseDate = "2025-06-20", genres = listOf(drama)),
        movie(4, "Beta",       popularity = 30.0, releaseDate = "2022-11-05", genres = listOf(comedy)),
        movie(5, "Delta",      popularity = 70.0, releaseDate = "2024-07-10", genres = listOf(action)),
    )

    // ─── Filter tests ─────────────────────────────────────────────────────────

    @Test
    fun `applyTo - no filter returns all movies`() {
        val state = FilterSortState(selectedGenre = null)
        val result = state.applyTo(movies)
        assertEquals(movies.size, result.size)
    }

    @Test
    fun `applyTo - filter by Action returns only Action movies`() {
        val state = FilterSortState(selectedGenre = action)
        val result = state.applyTo(movies)
        // Movies 1, 2, 5 have Action
        assertEquals(3, result.size)
        assertTrue(result.all { movie -> movie.genres.any { it.id == action.id } })
    }

    @Test
    fun `applyTo - filter by Comedy returns movies with Comedy genre`() {
        val state = FilterSortState(selectedGenre = comedy)
        val result = state.applyTo(movies)
        // Movies 2 and 4 have Comedy
        assertEquals(2, result.size)
        assertTrue(result.all { movie -> movie.genres.any { it.id == comedy.id } })
    }

    @Test
    fun `applyTo - filter by genre with no matches returns empty list`() {
        val sciFi = Genre(id = 878, name = "Science Fiction")
        val state = FilterSortState(selectedGenre = sciFi)
        val result = state.applyTo(movies)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyTo - filter preserves movies with multiple matching genres`() {
        // Movie 2 has both Action and Comedy — should appear in both filters
        val actionResult = FilterSortState(selectedGenre = action).applyTo(movies)
        val comedyResult = FilterSortState(selectedGenre = comedy).applyTo(movies)
        assertTrue(actionResult.any { it.id == 2 })
        assertTrue(comedyResult.any { it.id == 2 })
    }

    // ─── Sort by popularity ───────────────────────────────────────────────────

    @Test
    fun `applyTo - sort by popularity descending returns highest first`() {
        val state = FilterSortState(
            sortOption = SortOption.POPULARITY,
            sortAscending = false,
        )
        val result = state.applyTo(movies)
        // Expected: 90, 80, 70, 50, 30
        assertEquals(listOf(90.0, 80.0, 70.0, 50.0, 30.0), result.map { it.popularity })
    }

    @Test
    fun `applyTo - sort by popularity ascending returns lowest first`() {
        val state = FilterSortState(
            sortOption = SortOption.POPULARITY,
            sortAscending = true,
        )
        val result = state.applyTo(movies)
        // Expected: 30, 50, 70, 80, 90
        assertEquals(listOf(30.0, 50.0, 70.0, 80.0, 90.0), result.map { it.popularity })
    }

    // ─── Sort by title ────────────────────────────────────────────────────────

    @Test
    fun `applyTo - sort by title descending returns Z first`() {
        val state = FilterSortState(
            sortOption = SortOption.TITLE,
            sortAscending = false,
        )
        val result = state.applyTo(movies)
        // Expected: Zeta, Delta, Cobra, Beta, Alpha
        assertEquals(listOf("Zeta", "Delta", "Cobra", "Beta", "Alpha"), result.map { it.title })
    }

    @Test
    fun `applyTo - sort by title ascending returns A first`() {
        val state = FilterSortState(
            sortOption = SortOption.TITLE,
            sortAscending = true,
        )
        val result = state.applyTo(movies)
        // Expected: Alpha, Beta, Cobra, Delta, Zeta
        assertEquals(listOf("Alpha", "Beta", "Cobra", "Delta", "Zeta"), result.map { it.title })
    }

    // ─── Sort by release date ─────────────────────────────────────────────────

    @Test
    fun `applyTo - sort by release date descending returns newest first`() {
        val state = FilterSortState(
            sortOption = SortOption.RELEASE_DATE,
            sortAscending = false,
        )
        val result = state.applyTo(movies)
        assertEquals("2025-06-20", result.first().releaseDate)
        assertEquals("2022-11-05", result.last().releaseDate)
    }

    @Test
    fun `applyTo - sort by release date ascending returns oldest first`() {
        val state = FilterSortState(
            sortOption = SortOption.RELEASE_DATE,
            sortAscending = true,
        )
        val result = state.applyTo(movies)
        assertEquals("2022-11-05", result.first().releaseDate)
        assertEquals("2025-06-20", result.last().releaseDate)
    }

    // ─── Combined filter + sort ───────────────────────────────────────────────

    @Test
    fun `applyTo - filter by Action then sort by popularity descending`() {
        val state = FilterSortState(
            selectedGenre = action,
            sortOption = SortOption.POPULARITY,
            sortAscending = false,
        )
        val result = state.applyTo(movies)
        // Action movies: 1(80), 2(50), 5(70) → sorted desc: 80, 70, 50
        assertEquals(3, result.size)
        assertEquals(listOf(80.0, 70.0, 50.0), result.map { it.popularity })
    }

    @Test
    fun `applyTo - empty input returns empty list`() {
        val state = FilterSortState()
        val result = state.applyTo(emptyList())
        assertTrue(result.isEmpty())
    }
}