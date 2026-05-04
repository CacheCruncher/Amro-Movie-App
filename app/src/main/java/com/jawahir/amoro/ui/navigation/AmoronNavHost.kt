package com.jawahir.amoro.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jawahir.amoro.trending.TrendingScreen
import com.jawahir.amoro.ui.detail.DetailScreen

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
        composable<Detail> { backStackEntry ->
            val context = LocalContext.current
            DetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onImdbClick = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                }
            )
        }
    }
}