package com.jawahir.amoro.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 */
@Serializable
data object Trending

@Serializable
data class Detail(val movieId:Int)
