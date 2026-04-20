# Movies Feature — API Layer
# Module: :feature:movies → data/

## Files to generate:
# feature/movies/data/remote/OmdbApiService.kt
# feature/movies/data/remote/dto/OmdbListResponseDto.kt
# feature/movies/data/remote/dto/SeriesDto.kt
# feature/movies/data/remote/dto/SeriesDetailDto.kt
# feature/movies/data/remote/mapper/SeriesMapper.kt
# feature/movies/data/repository/MoviesRepositoryImpl.kt
# feature/movies/di/MoviesNetworkModule.kt

## OmdbApiService
interface OmdbApiService {
    @GET("/")
    suspend fun searchSeries(
        @Query("s") query: String,
        @Query("type") type: String = "series",
        @Query("apikey") apiKey: String = "8170cd9d"
    ): OmdbListResponseDto

    @GET("/")
    suspend fun getSeriesDetail(
        @Query("i") imdbID: String,
        @Query("apikey") apiKey: String = "8170cd9d"
    ): SeriesDetailDto
}

## DTOs — use @SerialName matching exact OMDb API response field names
## See api_contract.md for full DTO definitions

## SeriesMapper
- toSeriesModel(dto: SeriesDto): SeriesModel
- toSeriesDetailModel(dto: SeriesDetailDto): SeriesDetailModel
- toBindingMap(model: SeriesModel): Map<String, String>  ← for SDUI binding
- toBindingMap(model: SeriesDetailModel): Map<String, String>

## toBindingMap for SeriesModel (list item)
mapOf(
    "title" to model.title,
    "year" to model.year,
    "type" to model.type,
    "posterURL" to model.posterUrl,
    "imdbID" to model.imdbID
    // rating and genre come from enrichment detail call
)

## toBindingMap for SeriesDetailModel (detail screen)
mapOf(
    "title" to model.title,
    "year" to model.year,
    "genre" to model.genre,
    "posterURL" to model.posterUrl,
    "rating" to model.rating,
    "runtime" to model.runtime,
    "totalSeasons" to model.totalSeasons,
    "awards" to model.awards,
    "plot" to model.plot,
    "actors" to model.actors,
    "writer" to model.writer,
    "director" to model.director
)

## MoviesRepositoryImpl
class MoviesRepositoryImpl @Inject constructor(
    private val api: OmdbApiService
) : MoviesRepository {
    override suspend fun searchSeries(query: String): Result<List<SeriesModel>>
    override suspend fun getSeriesDetail(imdbID: String): Result<SeriesDetailModel>
    override suspend fun getSeriesDetailMap(imdbID: String): Result<Map<String, String>>
}
