# Movies Feature — UI Layer
# Module: :feature:movies → ui/

## Files to generate:
# feature/movies/ui/MoviesState.kt
# feature/movies/ui/MoviesViewModel.kt
# feature/movies/ui/MoviesScreen.kt
# feature/movies/ui/SeriesDetailState.kt
# feature/movies/ui/SeriesDetailViewModel.kt
# feature/movies/ui/SeriesDetailScreen.kt
# feature/movies/di/MoviesDiModule.kt

## Screen JSON assets (place in :core:network assets/screens/)
# assets/screens/tv_series_list.json   ← the provided tv_series_list_4.json
# assets/screens/series_detail.json    ← the provided series_detail_4.json

## MoviesState / MoviesIntent / MoviesEffect
data class MoviesState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val screenModel: ScreenModel? = null,
    val listData: Map<String, List<Map<String, String>>> = emptyMap()
    // listData["series"] = List of binding maps for each series row
) : UiState

sealed class MoviesIntent : UiIntent {
    object LoadScreen : MoviesIntent()
    data class HandleAction(val actionId: String, val params: Map<String, String> = emptyMap()) : MoviesIntent()
}

sealed class MoviesEffect : UiEffect {
    data class NavigateToDetail(val seriesId: String) : MoviesEffect()
    data class ShowToast(val message: String) : MoviesEffect()
}

## MoviesViewModel flow:
1. LoadScreen intent received
2. Load ScreenModel from assets/screens/tv_series_list.json via ScreenAssetLoader
3. setState { copy(screenModel = screenModel, isLoading = true) }
4. Read screenModel.dataSource.url → extract query param ("game")
5. Call GetSeriesListUseCase("game") → returns List<Map<String,String>>
6. setState { copy(isLoading = false, listData = mapOf("series" to result)) }
7. HandleAction("seriesTapped", params) → params["imdbID"] → setEffect(NavigateToDetail)

## MoviesScreen
@Composable
fun MoviesScreen(navController: NavController, viewModel: MoviesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.handleIntent(MoviesIntent.LoadScreen) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MoviesEffect.NavigateToDetail ->
                    navController.navigate(Routes.seriesDetail(effect.seriesId))
                is MoviesEffect.ShowToast -> { /* show toast */ }
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        listData = state.listData,
        onAction = { actionId, params ->
            viewModel.handleIntent(MoviesIntent.HandleAction(actionId, params))
        }
    )
}

## SeriesDetailState / SeriesDetailIntent / SeriesDetailEffect
data class SeriesDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val screenModel: ScreenModel? = null,
    val dataMap: Map<String, String> = emptyMap()
) : UiState

sealed class SeriesDetailIntent : UiIntent {
    data class LoadScreen(val seriesId: String) : SeriesDetailIntent()
    data class HandleAction(val actionId: String, val params: Map<String, String> = emptyMap()) : SeriesDetailIntent()
}

sealed class SeriesDetailEffect : UiEffect {
    object NavigateBack : SeriesDetailEffect()
}

## SeriesDetailViewModel flow:
1. LoadScreen(seriesId) intent received
2. Load ScreenModel from assets/screens/series_detail.json
3. setState { copy(screenModel = screenModel, isLoading = true) }
4. Build detail URL: replace {{seriesId}} in dataSource.url with actual seriesId
5. Call GetSeriesDetailMapUseCase(seriesId) → returns Map<String,String>
6. setState { copy(isLoading = false, dataMap = result) }
7. HandleAction("back", ...) → setEffect(NavigateBack)

## SeriesDetailScreen
@Composable
fun SeriesDetailScreen(
    navController: NavController,
    seriesId: String,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(seriesId) {
        viewModel.handleIntent(SeriesDetailIntent.LoadScreen(seriesId))
    }
    // collect state + effect, render SDUIRenderer with dataMap
}

## Visual Reference (from screenshots provided)
List screen:
- Dark background (0xFF0D0F14)
- Large title "Series Hub" + subtitle "Live OMDb data for the Game franchise"
- Each card: rounded 22dp, dark card bg (0xFF1A1D27)
- Poster 88x128dp, corner 16dp
- Title bold 22sp, Year+Type 14sp muted, Genre 15sp, IMDb star (red) + rating
- Drag handle icon (≡) on right side of each card

Detail screen:
- Back button top left (rounded circle)
- Header: large title + year•genre subtitle
- Hero card: poster 132x190dp + rating/runtime/seasons/awards column
- Synopsis card: dark surface bg, title + plot text
- Credits card: cast, writer, director
- Seasons section: generatedList rows with play.tv icon + "Season N" label
