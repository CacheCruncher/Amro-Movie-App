package com.jawahir.amoro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jawahir.amoro.trending.TrendingScreen

/**
 * Central navigation graph for the application.
 */
@Composable
fun AmoronNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Trending
    ) {
        composable<Trending> {
            TrendingScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Detail(movieId))
                }
            )
        }
    }
}