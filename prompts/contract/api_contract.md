# API Contract
# Follow this pattern for ALL network layer files

## Retrofit Service Interface
- One interface per feature domain
- All functions are suspend
- Return types are Response<T> or direct DTO
- Base URL configured in NetworkModule via Hilt
- OkHttp interceptors: HttpLoggingInterceptor + (MockInterceptor for banking only)

## OMDb API (Movies feature — LIVE, no mock)
Base URL: https://www.omdbapi.com/
API Key: 8170cd9d

### Endpoints
GET /?s={query}&type=series&apikey={key}   → OmdbListResponseDto
GET /?i={imdbID}&apikey={key}              → SeriesDetailDto

### OmdbListResponseDto
data class OmdbListResponseDto(
    @SerialName("Search") val search: List<SeriesDto>? = null,
    @SerialName("Response") val response: String,
    @SerialName("totalResults") val totalResults: String? = null
)

### SeriesDto (list item — partial data)
data class SeriesDto(
    @SerialName("Title") val title: String,
    @SerialName("Year") val year: String,
    @SerialName("imdbID") val imdbID: String,
    @SerialName("Type") val type: String,
    @SerialName("Poster") val poster: String
)

### SeriesDetailDto (full detail)
data class SeriesDetailDto(
    @SerialName("Title") val title: String,
    @SerialName("Year") val year: String,
    @SerialName("Genre") val genre: String,
    @SerialName("imdbID") val imdbID: String,
    @SerialName("imdbRating") val rating: String,
    @SerialName("Poster") val poster: String,
    @SerialName("Runtime") val runtime: String,
    @SerialName("totalSeasons") val totalSeasons: String,
    @SerialName("Awards") val awards: String,
    @SerialName("Plot") val plot: String,
    @SerialName("Actors") val actors: String,
    @SerialName("Writer") val writer: String,
    @SerialName("Director") val director: String,
    @SerialName("Response") val response: String
)

## NetworkModule (Hilt — in :core:network)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://www.omdbapi.com/")
        .client(client)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideOmdbApiService(retrofit: Retrofit): OmdbApiService =
        retrofit.create(OmdbApiService::class.java)
}

## SDUI Screen Loader (in :core:network)
- ScreenAssetLoader: reads JSON from assets/screens/{screenId}.json
- Returns raw JSON string to be parsed by SDUIParser
- No network call — purely local asset reading

## Banking Mock (in :core:network)
- MockInterceptor intercepts calls matching "/banking/*"
- Reads from assets/mock/banking/{endpoint}.json
- Returns mock response with 200 OK
