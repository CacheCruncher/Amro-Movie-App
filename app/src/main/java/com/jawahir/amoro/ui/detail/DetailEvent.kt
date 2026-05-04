package com.jawahir.amoro.ui.detail

/**
 * User actions for the Movie Detail screen.
 */
sealed interface DetailEvent {
    /** Triggers the fetch for the specific movie's details. */
    data object LoadDetail : DetailEvent
}