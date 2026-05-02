package com.jawahir.amoro.data.remote.dto

data class GenreDto(
    val id:Int?,
    val name:String?
)

data class GenreResponseDto(
    val genres: List<GenreDto>?
)