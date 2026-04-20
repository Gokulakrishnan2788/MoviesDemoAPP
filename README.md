# MoviesDemoApp

A production-grade Android application demonstrating **Server-Driven UI (SDUI)** combined with **MVI architecture**, **Clean Architecture** per feature, and **Hilt dependency injection**. The app has two tabs — a TV series browser powered by the OMDb API and a banking dashboard served from local mock JSON.

---

## Table of Contents

1. [Module Graph](#module-graph)
2. [Module Responsibilities](#module-responsibilities)
3. [Clean Architecture Per Feature](#clean-architecture-per-feature)
4. [MVI Pattern](#mvi-pattern)
5. [Server-Driven UI Engine](#server-driven-ui-engine)
6. [API Integration & Data Binding Flow](#api-integration--data-binding-flow)
7. [Navigation](#navigation)
8. [Dependency Injection](#dependency-injection)
9. [Tech Stack](#tech-stack)

---

## Module Graph

```
:app
 ├── :feature:movies
 │    ├── :core:domain
 │    ├── :core:data
 │    ├── :core:network
 │    ├── :core:ui
 │    ├── :engine:sdui
 │    └── :engine:navigation
 ├── :feature:banking
 │    ├── :core:domain
 │    ├── :core:network
 │    ├── :core:ui
 │    ├── :engine:sdui
 │    └── :engine:navigation
 ├── :engine:sdui
 │    ├── :core:ui
 │    └── :core:network
 └── :engine:navigation
```

---

## Module Responsibilities

### `:core:domain`
Pure Kotlin. No Android dependencies. Defines the shared MVI base infrastructure.

```
core/domain/
└── BaseViewModel.kt      — abstract MVI base; owns state, effect channel, intent dispatch
    Result.kt             — sealed class: Success<T> | Error(code, message) | Loading
    UiContract.kt         — UiState, UiIntent, UiEffect marker interfaces
    BaseUseCase.kt        — optional generic use case base
```

`BaseViewModel` enforces unidirectional data flow:
```
UI  ──dispatch(intent)──▶  BaseViewModel.handleIntent()
                                │
                          viewModelScope.launch { reduce(intent) }
                                │
                    ┌───────────┴───────────┐
                    │                       │
              setState { }           setEffect(effect)
                    │                       │
              _state.update()       _effect.send()
                    │                       │
              StateFlow<S>          Flow<E> (one-shot)
                    │                       │
                   UI                      UI
```

---

### `:core:network`
All API surface definitions and network infrastructure.

```
core/network/
├── OmdbApiService.kt           — Retrofit interface for OMDb (search + detail)
├── BankingApiService.kt        — Retrofit interface for /banking/* endpoints
├── OmdbDtos.kt                 — OmdbListResponseDto, SeriesDto, SeriesDetailDto
├── BankingDtos.kt              — BankingHomeDto, TransactionDto
├── MockInterceptor.kt          — OkHttp interceptor: /banking/* → assets/mock/banking/
├── KotlinxSerializationConverterFactory.kt — custom Retrofit converter (kotlinx.serialization)
├── NetworkModule.kt            — Hilt: provides Json, OkHttpClient, Retrofit, both API services
└── assets/
    ├── screens/
    │   └── tv_series_list.json — SDUI screen definition for movies list
    └── mock/
        └── banking/
            └── home.json       — mock banking home response
```

**MockInterceptor** intercepts any request whose path starts with `/banking`:
```kotlin
if (!path.startsWith("/banking")) return chain.proceed(request)
val fileName = path.removePrefix("/banking/").ifEmpty { "home" }
// reads assets/mock/banking/{fileName}.json and returns it as HTTP 200
```

This means `BankingApiService.getBankingHome()` (`@GET("/banking/home")`) never hits the internet — the interceptor catches it first and returns local JSON.

---

### `:core:data`
Room database infrastructure only. No business logic.

```
core/data/
├── AppDatabase.kt       — @Database with WatchlistEntity
├── WatchlistDao.kt      — getAll(), isInWatchlist(id), insert(), deleteById()
├── WatchlistEntity.kt   — @Entity: imdbID, title, posterUrl, rating, year, genre
└── DataModule.kt        — Hilt: provides AppDatabase, WatchlistDao
```

Feature modules import `WatchlistDao` directly in their own `data/repository/` implementations.

---

### `:core:ui`
Shared Compose design tokens and reusable components.

```
core/ui/
├── DesignTokens.kt      — colors (ScreenBackground, CardBackground, Accent…),
│                          spacing (SpacingXs/Sm/Md/Lg), text sizes, radius constants
├── colorFromToken()     — maps SDUI color string token → Compose Color
├── MovieAppTheme.kt     — MaterialTheme wrapper using DesignTokens
├── AppButton.kt         — branded button
├── AppCard.kt           — branded card
├── AppTextField.kt      — branded text field
└── AppTopBar.kt         — branded top bar
```

---

### `:engine:sdui`
Generic Server-Driven UI engine. Knows nothing about movies or banking.

```
engine/sdui/
├── ScreenModel.kt           — data classes: ScreenModel, ComponentNode, StyleModel,
│                              DataSourceModel, RequestModel, ResponseModel, ActionModel
├── SDUIRepository.kt        — reads assets/screens/{screenId}.json → ScreenModel
├── DataSourceExecutor.kt    — executes remote dataSource (main + enrichment)
├── TemplateResolver.kt      — resolves {{key}} placeholders in strings
├── ComponentRegistry.kt     — registry for custom component factories
├── SDUIRenderer.kt          — top-level @Composable entry point + SDUIRenderEngine
└── components/
    └── SDUIComponents.kt    — built-in renderers: scroll, column, row, card,
                               text, header, image, icon, button, list
```

---

### `:engine:navigation`
Navigation contracts shared across all features.

```
engine/navigation/
├── Routes.kt              — SPLASH, MAIN, MOVIES, SERIES_DETAIL, BANKING constants
├── NavigationAction.kt    — data class: NavType(PUSH/REPLACE/POP/DEEP_LINK) + destination
└── NavigationEngine.kt    — object: navigate(NavController, NavigationAction)
```

---

### `:feature:movies`
TV series list + detail. Strictly layered.

```
feature/movies/
├── domain/
│   ├── model/
│   │   ├── Series.kt              — id, title, year, type, posterUrl, rating, genre
│   │   └── SeriesDetail.kt        — full detail model (plot, actors, director…)
│   ├── repository/
│   │   ├── SeriesRepository.kt    — interface: searchSeries(), getSeriesDetail()
│   │   └── WatchlistRepository.kt — interface: getWatchlist(), addToWatchlist()…
│   └── usecase/
│       ├── GetSeriesListUseCase.kt    — default list fetch (query = "game")
│       ├── SearchSeriesUseCase.kt     — user-initiated search
│       ├── GetSeriesDetailUseCase.kt  — detail by imdbId
│       └── WatchlistUseCases.kt       — Add / Remove / IsIn watchlist
├── data/
│   └── repository/
│       ├── SeriesRepositoryImpl.kt    — uses core:network OmdbApiService
│       └── WatchlistRepositoryImpl.kt — uses core:data WatchlistDao
├── di/
│   └── MoviesModule.kt    — binds interfaces → impls, provides all use cases
└── ui/
    ├── list/
    │   ├── MoviesContract.kt      — MoviesState, MoviesIntent, MoviesEffect
    │   ├── MoviesViewModel.kt     — loads SDUI JSON, executes dataSource
    │   └── MoviesScreen.kt        — SDUIRenderer wired to ViewModel
    ├── detail/
    │   ├── SeriesDetailContract.kt
    │   ├── SeriesDetailViewModel.kt
    │   └── SeriesDetailScreen.kt
    └── MoviesNavGraph.kt          — moviesGraph() NavGraphBuilder extension
```

---

### `:feature:banking`
Banking home with balance and transactions. Same layering.

```
feature/banking/
├── domain/
│   ├── model/
│   │   └── BankingModels.kt        — BankingHome, Transaction
│   ├── repository/
│   │   └── BankingRepository.kt    — interface: getBankingHome()
│   └── usecase/
│       └── GetBankingHomeUseCase.kt
├── data/
│   └── repository/
│       └── BankingRepositoryImpl.kt — uses core:network BankingApiService (mocked)
├── di/
│   └── BankingModule.kt
└── ui/
    ├── BankingContract.kt
    ├── BankingViewModel.kt
    ├── BankingScreen.kt
    └── BankingNavGraph.kt
```

---

## Clean Architecture Per Feature

Every feature follows the same three-layer rule:

```
UI layer         ──▶  knows about:  domain layer only
domain layer     ──▶  knows about:  nothing (pure Kotlin)
data layer       ──▶  knows about:  domain layer (implements interfaces)
                       uses:         core:network, core:data (infrastructure)
```

```
┌─────────────────────────────────────────────────┐
│  ui/  (Composables, ViewModels, Contracts)       │
│   │  dispatch(Intent) ──▶ ViewModel              │
│   │  collect(State)   ◀── ViewModel              │
│   │  collect(Effect)  ◀── ViewModel              │
└───┼─────────────────────────────────────────────┘
    │ calls UseCase
┌───▼─────────────────────────────────────────────┐
│  domain/  (UseCases, Repository interfaces,      │
│            domain Models)                        │
│   UseCase.invoke() ──▶ Repository.method()       │
└───┼─────────────────────────────────────────────┘
    │ implements
┌───▼─────────────────────────────────────────────┐
│  data/  (RepositoryImpl)                         │
│   SeriesRepositoryImpl  ──▶  OmdbApiService      │
│   WatchlistRepositoryImpl ─▶ WatchlistDao        │
│   BankingRepositoryImpl  ──▶ BankingApiService   │
└─────────────────────────────────────────────────┘
```

---

## MVI Pattern

```
                     ┌──────────────────────────┐
                     │        Composable UI      │
                     │                          │
                     │  collectAsState(state)   │
                     │  LaunchedEffect(effect)  │
                     │  onClick { dispatch() }  │
                     └────────┬────────▲────────┘
                              │        │
                         Intent       State / Effect
                              │        │
                     ┌────────▼────────┴────────┐
                     │       BaseViewModel       │
                     │                          │
                     │  handleIntent(intent)     │
                     │    └─▶ reduce(intent)     │  ← abstract, override per feature
                     │         ├─ setState { }   │  ← atomic state update
                     │         └─ setEffect()    │  ← one-shot event
                     │                          │
                     │  _state: MutableStateFlow │
                     │  _effect: Channel<E>      │
                     └──────────────────────────┘
```

**Contract file** (per screen):
```kotlin
// State — what the UI renders
data class MoviesState(
    val screenModel: ScreenModel? = null,  // SDUI layout (null while loading JSON)
    val isLoading: Boolean = false,
    val error: String? = null,
    val listData: Map<String, List<Map<String, String>>> = emptyMap(),
) : UiState

// Intent — everything the user can do
sealed interface MoviesIntent : UiIntent {
    data object LoadScreen : MoviesIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : MoviesIntent
}

// Effect — one-shot events the UI must handle exactly once
sealed interface MoviesEffect : UiEffect {
    data class Navigate(val route: String) : MoviesEffect
}
```

---

## Server-Driven UI Engine

The SDUI engine renders screens from JSON without any Kotlin UI code per screen. The JSON lives in `assets/screens/` and describes the full component tree, data sources, and actions.

### JSON Structure (`tv_series_list.json`)

```json
{
  "screenId": "tv_series_list",
  "type": "scroll",
  "dataSource": {
    "type": "remote",
    "request": { "method": "GET", "url": "https://www.omdbapi.com/?s=game&type=series&apikey=..." },
    "response": {
      "root": "Search",
      "type": "collection",
      "fieldMapping": { "id": "imdbID", "title": "Title", "year": "Year", "posterURL": "Poster" }
    },
    "enrichmentDataSource": {
      "type": "remote",
      "request": { "method": "GET", "url": "https://www.omdbapi.com/?i={{id}}&apikey=..." },
      "response": {
        "type": "object",
        "fieldMapping": { "rating": "imdbRating", "genre": "Genre" }
      }
    }
  },
  "children": [
    {
      "type": "column",
      "children": [
        { "type": "header", "titleTemplate": "{{headerTitle}}", "action": { "type": "search" } },
        {
          "type": "list",
          "listDataBinding": "series",
          "itemLayout": {
            "type": "row",
            "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" },
            "children": [
              { "type": "image", "dataBinding": "posterURL" },
              {
                "type": "column",
                "children": [
                  { "type": "text", "dataBinding": "title" },
                  { "type": "text", "template": "{{year}} • {{type}}" },
                  { "type": "text", "dataBinding": "genre" },
                  { "type": "text", "template": "IMDb {{rating}}" }
                ]
              }
            ]
          }
        }
      ]
    }
  ]
}
```

### Component Rendering

`SDUIRenderEngine` walks the JSON tree recursively:

```
ScreenModel.type == "scroll"
  └── Column(verticalScroll)
        └── for each child → RenderNode(child)
              │
              ├── "column"  → Column + verticalArrangement.spacedBy(spacing)
              ├── "row"     → Row + clickable(action) if action present
              ├── "header"  → Row: title Text + optional search IconButton
              ├── "list"    → Column over listData[listDataBinding]
              │               each item rendered via itemLayout template
              ├── "text"    → Text, resolving dataBinding or {{template}}
              ├── "image"   → AsyncImage(url from dataBinding)
              ├── "icon"    → Icon (star / search mapped by name)
              └── "button"  → clickable Text
```

**Custom components** can be registered in `ComponentRegistry` without touching the engine:
```kotlin
registry.register("myWidget") { node, data, onAction ->
    MyWidgetComposable(node, data, onAction)
}
```

---

## API Integration & Data Binding Flow

### Movies List — Full End-to-End

```
Step 1  MoviesViewModel.init
          └── handleIntent(LoadScreen)

Step 2  SDUIRepository.loadScreen("tv_series_list")
          └── reads assets/screens/tv_series_list.json
          └── decodes → ScreenModel
          └── setState(screenModel = ScreenModel, isLoading = true)
              ▶ UI renders immediately: scroll > column > header + empty list skeleton

Step 3  DataSourceExecutor.execute(dataSource)

   3a  Main fetch  ──────────────────────────────────────────────────────────
        OkHttpClient.GET  https://www.omdbapi.com/?s=game&type=series&apikey=...
        Response JSON:
        {
          "Search": [
            { "imdbID": "tt0903747", "Title": "Breaking Bad", "Year": "2008", "Poster": "..." },
            ...
          ]
        }
        Parse:  response.root = "Search"  →  extract array
        Apply fieldMapping per item:
          imdbID  →  id
          Title   →  title
          Year    →  year
          Poster  →  posterURL
        Result:  List<Map<String,String>> (10 items)

   3b  EnrichmentDataSource (launched in parallel for all 10 items)
        For each item, resolve URL template:
          "https://www.omdbapi.com/?i={{id}}&apikey=..."
          {{id}} replaced with item["id"]  e.g. "tt0903747"
        OkHttpClient.GET  https://www.omdbapi.com/?i=tt0903747&apikey=...
        Response JSON:
        { "imdbRating": "9.5", "Genre": "Crime, Drama, Thriller", ... }
        Apply fieldMapping:
          imdbRating  →  rating
          Genre       →  genre
        Merge into item map → item now has all 7 fields

Step 4  setState(listData = { "series": [10 enriched items] })
          ▶ SDUIRenderer re-renders list
          ▶ Each row binds per-item data:
              image.dataBinding   = "posterURL" → item["posterURL"]
              text.dataBinding    = "title"     → item["title"]
              text.template       = "{{year}} • {{type}}"
              text.dataBinding    = "genre"     → item["genre"]
              text.template       = "IMDb {{rating}}"

Step 5  User taps a row
          row.action = { type: "navigate", routeTemplate: "series_detail/{{id}}" }
          ActionModel.dispatch() resolves {{id}} → "series_detail/tt0903747"
          onAction("navigate", { route: "series_detail/tt0903747" })
          MoviesViewModel.handleAction → setEffect(Navigate("series_detail/tt0903747"))
          MoviesScreen LaunchedEffect → navController.navigate("series_detail/tt0903747")
```

### Series Detail Screen

```
SeriesDetailScreen receives seriesId = "tt0903747"
  └── handleIntent(Load("tt0903747"))
        ├── GetSeriesDetailUseCase("tt0903747")
        │     └── SeriesRepositoryImpl.getSeriesDetail("tt0903747")
        │           └── OmdbApiService.getSeriesDetail("tt0903747")
        │                 GET https://www.omdbapi.com/?i=tt0903747&plot=full&apikey=...
        │                 Response → SeriesDetailDto → SeriesDetail domain model
        ├── IsInWatchlistUseCase("tt0903747")
        │     └── WatchlistRepositoryImpl → WatchlistDao.isInWatchlist("tt0903747")
        └── setState(detail = SeriesDetail, isInWatchlist = true/false)
              ▶ UI renders: poster, title, year, genre, seasons, plot, cast, director, awards
```

### Banking Screen

```
BankingViewModel.init
  └── handleIntent(LoadHome)
        └── GetBankingHomeUseCase()
              └── BankingRepositoryImpl.getBankingHome()
                    └── BankingApiService.getBankingHome()
                          Retrofit: GET https://www.omdbapi.com/banking/home
                                        ↑ base URL, but intercepted before sending

                          MockInterceptor intercepts (path starts with /banking):
                            reads assets/mock/banking/home.json
                            returns HTTP 200 with local JSON body

                          Response → BankingHomeDto → BankingHome domain model
              setState(home = BankingHome)
                ▶ UI renders: balance card + transactions list
```

---

## Navigation

Navigation is graph-based with nested sub-graphs per feature.

```
NavHost(startDestination = "movies_graph")
  ├── moviesGraph (route = "movies_graph")
  │     ├── composable(Routes.MOVIES)         → MoviesScreen
  │     └── composable(Routes.SERIES_DETAIL)  → SeriesDetailScreen(seriesId)
  └── bankingGraph (route = "banking_graph")
        └── composable(Routes.BANKING)         → BankingScreen

Routes:
  MOVIES         = "movies"
  SERIES_DETAIL  = "series_detail/{seriesId}"
  BANKING        = "banking"
```

**Navigation from SDUI action:**
```
JSON:  "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" }
         │
         ▼
ActionModel.dispatch(itemData, onAction)
  resolves {{id}} → "series_detail/tt0903747"
  calls onAction("navigate", { "route": "series_detail/tt0903747" })
         │
         ▼
MoviesViewModel.handleAction("navigate", params)
  setEffect(MoviesEffect.Navigate("series_detail/tt0903747"))
         │
         ▼
MoviesScreen LaunchedEffect
  navController.navigate("series_detail/tt0903747")
```

Bottom navigation switches between `"movies_graph"` and `"banking_graph"` using `popUpTo + saveState + restoreState` for back-stack preservation.

---

## Dependency Injection

Hilt provides all singletons. Each module layer owns its bindings.

```
NetworkModule      (core:network)
  ├── Json                   @Singleton
  ├── OkHttpClient           @Singleton  (with logging + MockInterceptor)
  ├── Retrofit               @Singleton  (base: https://www.omdbapi.com/)
  ├── OmdbApiService         @Singleton
  └── BankingApiService      @Singleton

DataModule         (core:data)
  ├── AppDatabase            @Singleton
  └── WatchlistDao           @Singleton

MoviesModule       (feature:movies)
  ├── SeriesRepository       @Singleton  → SeriesRepositoryImpl
  ├── WatchlistRepository    @Singleton  → WatchlistRepositoryImpl
  ├── GetSeriesListUseCase
  ├── SearchSeriesUseCase
  ├── GetSeriesDetailUseCase
  ├── AddToWatchlistUseCase
  ├── RemoveFromWatchlistUseCase
  └── IsInWatchlistUseCase

BankingModule      (feature:banking)
  ├── BankingRepository      @Singleton  → BankingRepositoryImpl
  └── GetBankingHomeUseCase
```

ViewModels are injected via `@HiltViewModel` and obtained in composables via `hiltViewModel()`.

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| Build | AGP + Gradle | 8.13.2 / 8.13 |
| UI | Jetpack Compose + Material 3 | BOM 2024.09.00 |
| DI | Hilt | 2.51.1 |
| Navigation | Compose Navigation | 2.8.3 |
| Network | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Serialization | kotlinx.serialization | 1.7.3 |
| Local DB | Room | 2.6.1 |
| Image Loading | Coil 3 | 3.0.4 |
| Async | Kotlin Coroutines + Flow | 1.8.1 |
| Annotation Processing | KSP | 2.0.21-1.0.28 |
| Testing | JUnit + MockK + Turbine | 4.13.2 / 1.13.12 / 1.1.0 |
