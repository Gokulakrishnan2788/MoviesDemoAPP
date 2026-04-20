# Movies Feature — Domain Layer
# Module: :feature:movies → domain/ AND :core:domain for shared models

## Files to generate:
# core/domain/model/SeriesModel.kt
# core/domain/model/SeriesDetailModel.kt
# feature/movies/domain/repository/MoviesRepository.kt
# feature/movies/domain/usecase/GetSeriesListUseCase.kt
# feature/movies/domain/usecase/GetSeriesDetailUseCase.kt
# feature/movies/domain/usecase/GetSeriesDetailMapUseCase.kt

## Domain Models (pure Kotlin — no Android imports)

data class SeriesModel(
    val imdbID: String,
    val title: String,
    val year: String,
    val type: String,
    val posterUrl: String,
    val rating: String = "N/A",  ← populated after enrichment call
    val genre: String = "N/A"    ← populated after enrichment call
)

data class SeriesDetailModel(
    val imdbID: String,
    val title: String,
    val year: String,
    val genre: String,
    val rating: String,
    val posterUrl: String,
    val runtime: String,
    val totalSeasons: String,
    val awards: String,
    val plot: String,
    val actors: String,
    val writer: String,
    val director: String
)

## MoviesRepository (interface — in :feature:movies:domain)
interface MoviesRepository {
    suspend fun searchSeries(query: String): Result<List<SeriesModel>>
    suspend fun getSeriesDetail(imdbID: String): Result<SeriesDetailModel>
    suspend fun getSeriesDetailMap(imdbID: String): Result<Map<String, String>>
}

## GetSeriesListUseCase
// Fetches list from OMDb search + enriches each item with rating/genre via detail call
// query comes from SDUI JSON dataSource URL parameter extraction ("game" by default)
class GetSeriesListUseCase @Inject constructor(private val repository: MoviesRepository) {
    suspend operator fun invoke(query: String = "game"): Result<List<Map<String, String>>>
    // Returns list of binding maps ready for SDUI listData
}

## GetSeriesDetailUseCase
class GetSeriesDetailUseCase @Inject constructor(private val repository: MoviesRepository) {
    suspend operator fun invoke(imdbID: String): Result<SeriesDetailModel>
}

## GetSeriesDetailMapUseCase
class GetSeriesDetailMapUseCase @Inject constructor(private val repository: MoviesRepository) {
    suspend operator fun invoke(imdbID: String): Result<Map<String, String>>
    // Returns binding map ready for SDUIRenderer dataMap
}
