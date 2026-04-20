package com.example.moviesdemoapp.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OMDb list search response wrapper. */
@Serializable
data class OmdbListResponseDto(
    @SerialName("Search") val search: List<SeriesDto>? = null,
    @SerialName("Response") val response: String = "False",
    @SerialName("totalResults") val totalResults: String? = null,
    @SerialName("Error") val error: String? = null,
)

/** Single series entry returned by the OMDb search endpoint. */
@Serializable
data class SeriesDto(
    @SerialName("Title") val title: String,
    @SerialName("Year") val year: String,
    @SerialName("imdbID") val imdbID: String,
    @SerialName("Type") val type: String,
    @SerialName("Poster") val poster: String,
)

/** Full series detail returned by the OMDb detail endpoint. */
@Serializable
data class SeriesDetailDto(
    @SerialName("Title") val title: String = "",
    @SerialName("Year") val year: String = "",
    @SerialName("Genre") val genre: String = "",
    @SerialName("imdbID") val imdbID: String = "",
    @SerialName("imdbRating") val rating: String = "",
    @SerialName("Poster") val poster: String = "",
    @SerialName("Runtime") val runtime: String = "",
    @SerialName("totalSeasons") val totalSeasons: String = "",
    @SerialName("Awards") val awards: String = "",
    @SerialName("Plot") val plot: String = "",
    @SerialName("Actors") val actors: String = "",
    @SerialName("Writer") val writer: String = "",
    @SerialName("Director") val director: String = "",
    @SerialName("Response") val response: String = "False",
)
