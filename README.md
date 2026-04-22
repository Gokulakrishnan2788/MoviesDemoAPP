# MoviesDemoApp

A production-grade Android application built on a fully **Server-Driven UI (SDUI)** engine, **MVI architecture**, and **Clean Architecture** across all modules. Every screen — list and detail — is rendered entirely from JSON definitions. Zero hardcoded API calls or UI layouts exist at the feature level; the engine handles everything.

---

## Table of Contents

1. [Architecture Philosophy](#1-architecture-philosophy)
2. [Module Structure](#2-module-structure)
3. [Module Dependency Graph](#3-module-dependency-graph)
4. [Module Responsibilities](#4-module-responsibilities)
5. [MVI Architecture](#5-mvi-architecture)
6. [Server-Driven UI Engine](#6-server-driven-ui-engine)
7. [SDUI Component System](#7-sdui-component-system)
8. [Data Pipeline — End to End](#8-data-pipeline--end-to-end)
9. [Parallel API Execution & Enrichment](#9-parallel-api-execution--enrichment)
10. [URL Template Resolution](#10-url-template-resolution)
11. [Data Binding to View](#11-data-binding-to-view)
12. [Features in Detail](#12-features-in-detail)
13. [Drag-to-Reorder](#13-drag-to-reorder)
14. [TalkBack & Accessibility](#14-talkback--accessibility)
15. [Libraries & Versions](#15-libraries--versions)
16. [Project File Tree](#16-project-file-tree)

---

## 1. Architecture Philosophy

This project is built around one central principle: **the feature module should contain no knowledge of APIs, data models, or UI layouts**. All of that lives in the SDUI engine and its supporting infrastructure.

```
Traditional approach              This project
─────────────────────             ───────────────────────────────────────
Feature has:                      Feature has:
  - Retrofit calls                  - ViewModel (dispatches intents)
  - Domain models                   - Contract (state / intent / effect)
  - Mappers                         - Screen (SDUIRenderer wrapper)
  - Repository impls                - NavGraph
  - Use cases
  - Hardcoded Composables         Everything else lives in:
                                    - engine:sdui  (render engine + JSON)
                                    - core:data    (data executor)
                                    - core:network (HTTP + DTO models)
```

Adding a new screen in this architecture = **adding a JSON file**. No Kotlin UI code required.

---

## 2. Module Structure

9 Gradle modules in 4 groups, each with its own `build.gradle.kts` and strict dependency boundaries:

| Group | Module | Role |
|---|---|---|
| App shell | `:app` | Entry point, NavHost, bottom navigation |
| Features | `:feature:movies` | TV series list + detail (UI only) |
| Features | `:feature:banking` | Placeholder tab |
| Engines | `:engine:sdui` | JSON → Compose render engine |
| Engines | `:engine:navigation` | Shared routes and navigation contracts |
| Core | `:core:domain` | BaseViewModel, MVI contracts, Result type |
| Core | `:core:network` | HTTP client, Retrofit, DTOs, DataSourceModel |
| Core | `:core:data` | DataSourceExecutor, Room, SDUI data fetching |
| Core | `:core:ui` | DesignTokens, shared Compose components |

---

## 3. Module Dependency Graph

```
:app
 ├── :feature:movies
 │    ├── :core:domain        ← BaseViewModel, UiState/Intent/Effect
 │    ├── :core:data          ← DataSourceExecutor
 │    ├── :core:ui            ← DesignTokens (transitively via engine:sdui)
 │    ├── :engine:sdui        ← SDUIRenderer, LoadSDUIScreenUseCase
 │    └── :engine:navigation  ← Routes
 ├── :feature:banking
 │    └── :engine:navigation
 ├── :engine:sdui
 │    ├── :core:network       ← DataSourceModel
 │    ├── :core:data          ← DataSourceExecutor, SDUIDataRepository
 │    └── :core:ui            ← DesignTokens, colorFromToken()
 ├── :core:data
 │    └── :core:network       ← NetworkClient, DataSourceModel
 ├── :core:network            (no internal deps)
 ├── :core:domain             (no deps — pure Kotlin)
 └── :core:ui                 (Compose only)
```

**Hard rules enforced by module boundaries:**
- Feature modules never depend on each other.
- `:core:domain` has zero Android or third-party dependencies.
- Features never import from `:core:network` directly; they go through the engine.

---

## 4. Module Responsibilities

### `:core:domain`
Pure Kotlin. The MVI foundation used by every ViewModel.

```
core/domain/
├── BaseViewModel.kt    — abstract MVI base: StateFlow, Channel<Effect>, handleIntent()
├── UiContract.kt       — UiState, UiIntent, UiEffect marker interfaces
├── Result.kt           — sealed class: Success<T> | Error(code, message) | Loading
└── BaseUseCase.kt      — optional single-abstract-method use case base
```

`BaseViewModel` is the only shared ViewModel base across the entire project:

```kotlin
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    fun handleIntent(intent: I) = viewModelScope.launch { reduce(intent) }
    abstract suspend fun reduce(intent: I)
    protected fun setState(reducer: S.() -> S) = _state.update { it.reducer() }
    protected fun setEffect(e: E) = viewModelScope.launch { _effect.send(e) }
}
```

---

### `:core:network`
All HTTP infrastructure and DTO definitions.

```
core/network/
├── di/
│   └── NetworkModule.kt                        — Hilt: Json, OkHttpClient, Retrofit, OmdbApiService
├── model/
│   ├── DataSourceModel.kt                      — DataSourceModel, RequestModel, ResponseModel
│   └── OmdbDtos.kt                             — OmdbListResponseDto, SeriesDto, SeriesDetailDto
├── OmdbApiService.kt                           — Retrofit interface: /search + /detail
├── NetworkClient.kt                            — interface: suspend fun get(url): String?
├── NetworkClientImpl.kt                        — OkHttpClient implementation
└── KotlinxSerializationConverterFactory.kt     — custom Retrofit converter
```

`DataSourceModel` is the data contract between JSON screen definitions and the data executor. It lives in `:core:network` because it is fundamentally an HTTP descriptor, but both `:core:data` and `:engine:sdui` reference it.

```kotlin
data class DataSourceModel(
    val type: String,
    val request: RequestModel? = null,
    val response: ResponseModel? = null,
    val enrichmentDataSource: DataSourceModel? = null,
) {
    val effectiveUrl: String get() = request?.url ?: url ?: ""
    val fieldMapping: Map<String, String> get() = response?.fieldMapping ?: emptyMap()
}
```

---

### `:core:data`
Data execution layer: drives all SDUI data fetching and local persistence.

```
core/data/
├── remote/
│   ├── SDUIDataRepository.kt       — interface: suspend fun fetch(url): String?
│   ├── SDUIDataRepositoryImpl.kt   — OkHttp-backed implementation via NetworkClient
│   └── DataSourceExecutor.kt       — orchestrates main fetch + parallel enrichment
├── local/
│   ├── AppDatabase.kt              — @Database(WatchlistEntity)
│   ├── WatchlistDao.kt             — getAll(), insert(), deleteById(), isInWatchlist()
│   └── WatchlistEntity.kt          — @Entity: imdbID, title, posterUrl, rating, year, genre
└── di/
    └── DataModule.kt               — @Binds SDUIDataRepository, @Provides Room/Dao
```

`DataSourceExecutor` is the most critical class in the project. It accepts an optional `params` map to resolve URL templates (e.g. `{{seriesId}}`) before execution, enabling both list and detail screens to fetch data with no feature-level code:

```kotlin
suspend fun execute(
    dataSource: DataSourceModel,
    params: Map<String, String> = emptyMap(),   // resolves {{key}} in main URL
): List<Map<String, String>>
```

---

### `:core:ui`
Shared design tokens and Compose primitives.

```
core/ui/
├── DesignTokens.kt      — all colors, spacing, typography, radii
│                           ScreenBackground (#0D0F14), CardBackground (#1A1D27)
│                           Surface (#1E2132), Accent (#E05C5C)
│                           SpacingXs(4dp), SpacingSm(8dp), SpacingMd(16dp), SpacingLg(24dp)
│                           TextSm(12sp) → TextXxl(24sp)
├── colorFromToken()     — maps SDUI JSON color string → Compose Color at render time
└── MovieAppTheme.kt     — MaterialTheme wrapper
```

`colorFromToken()` is the bridge between declarative JSON style tokens and live Compose colors. Every `backgroundColor`, `foregroundColor` string in any JSON file is resolved through this function:

```kotlin
fun colorFromToken(token: String): Color = when (token) {
    "screenBackground" -> DesignTokens.ScreenBackground
    "cardBackground"   -> DesignTokens.CardBackground
    "surface"          -> DesignTokens.Surface
    "accent"           -> DesignTokens.Accent
    "primaryText"      -> DesignTokens.PrimaryText
    "secondaryText"    -> DesignTokens.SecondaryText
    else               -> Color.Unspecified
}
```

---

### `:engine:sdui`
The generic Server-Driven UI engine. It has no knowledge of movies, banking, or any domain.

```
engine/sdui/
├── ScreenModel.kt              — data classes: ScreenModel, ComponentNode, StyleModel,
│                                  ActionModel, VisibilityModel
├── SDUIRepository.kt           — reads assets/screens/{screenId}.json → ScreenModel
├── SDUIParser.kt               — JSON parsing helpers
├── TemplateResolver.kt         — resolves {{key}} placeholders + visibility evaluation
├── ComponentRegistry.kt        — extensible registry: register("type") { node, data → Composable }
├── SDUIRenderer.kt             — public @Composable entry point + SDUIRenderEngine
├── usecase/
│   └── LoadSDUIScreenUseCase.kt — loads + parses a screen JSON by ID
└── components/
    └── SDUIComponents.kt       — all built-in component renderers
```

---

### `:engine:navigation`
Navigation contracts shared across features.

```
engine/navigation/
├── Routes.kt              — MOVIES, SERIES_DETAIL, BANKING route constants
├── NavigationAction.kt    — NavType (PUSH/REPLACE/POP/DEEP_LINK) + destination
└── NavigationEngine.kt    — navigate(NavController, NavigationAction)
```

---

### `:feature:movies`
TV series list and detail. Contains **only UI code** — no API calls, no data models, no use cases.

```
feature/movies/
└── ui/
    ├── list/
    │   ├── MoviesContract.kt        — MoviesState, MoviesIntent, MoviesEffect
    │   ├── MoviesViewModel.kt       — loads screen JSON, executes dataSource, handles reorder
    │   └── MoviesScreen.kt          — SDUIRenderer wired to ViewModel state
    ├── detail/
    │   ├── SeriesDetailContract.kt  — SeriesDetailState, SeriesDetailIntent, SeriesDetailEffect
    │   ├── SeriesDetailViewModel.kt — loads series_detail JSON, fetches with seriesId param
    │   └── SeriesDetailScreen.kt    — SDUIRenderer wired to ViewModel state
    └── MoviesNavGraph.kt            — composable(MOVIES) + composable(SERIES_DETAIL)
```

This is the entire feature module. No `domain/`, `data/`, or `di/` packages. The ViewModel injects `LoadSDUIScreenUseCase` and `DataSourceExecutor` — both provided by `engine:sdui` and `core:data` respectively.

---

### `:feature:banking`
Placeholder tab — single `Box { Text("TODO") }` composable. No data layer.

---

## 5. MVI Architecture

Every screen follows the same unidirectional data flow: **Intent → reduce → State/Effect**.

```
┌──────────────────────────────────────────────────┐
│                   Composable UI                   │
│                                                   │
│   val state by vm.state.collectAsStateWithLifecycle│
│   LaunchedEffect { vm.effect.collectLatest { } }  │
│   onClick { vm.handleIntent(SomeIntent) }         │
└────────────────┬──────────────▲───────────────────┘
                 │              │
            Intent           State / Effect
                 │              │
┌────────────────▼──────────────┴───────────────────┐
│                   BaseViewModel                    │
│                                                    │
│   handleIntent(intent)                             │
│     └─ viewModelScope.launch { reduce(intent) }    │
│           ├─ setState { copy(...) }  → StateFlow   │
│           └─ setEffect(effect)      → Channel      │
└────────────────────────────────────────────────────┘
```

**State** is an immutable data class; `setState` uses `MutableStateFlow.update{}` — thread-safe and conflict-free.

**Effect** is a `Channel<E>` consumed once with `collectLatest` — used only for one-shot events (navigation, toasts) that must not replay on recomposition.

### Contract example — Series Detail

```kotlin
// What the UI renders
data class SeriesDetailState(
    val screenModel: ScreenModel? = null,   // SDUI layout tree
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: Map<String, String> = emptyMap(), // flat API response data
) : UiState

// What the user can do
sealed interface SeriesDetailIntent : UiIntent {
    data class Load(val seriesId: String) : SeriesDetailIntent
    data object NavigateBack : SeriesDetailIntent
}

// One-shot events the UI handles exactly once
sealed interface SeriesDetailEffect : UiEffect {
    data object GoBack : SeriesDetailEffect
}
```

---

## 6. Server-Driven UI Engine

The SDUI engine translates a JSON file into a live Compose tree. The feature module provides only three inputs: the screen ID, the loading state, and an action callback.

### JSON Screen Definition Structure

Every screen is a JSON file in `core/network/src/main/assets/screens/`:

```json
{
  "screenId": "series_detail",
  "type": "scroll",
  "dataSource": {
    "type": "remote",
    "request": {
      "method": "GET",
      "url": "https://www.omdbapi.com/?i={{seriesId}}&apikey=...",
      "timeout": 10
    },
    "response": {
      "type": "object",
      "fieldMapping": {
        "title": "Title", "year": "Year", "genre": "Genre",
        "rating": "imdbRating", "plot": "Plot", "totalSeasons": "totalSeasons"
      }
    }
  },
  "children": [
    {
      "type": "column",
      "style": { "padding": 16, "spacing": 14, "backgroundColor": "screenBackground" },
      "children": [
        {
          "type": "topBar",
          "props": { "leadingIcon": "back" },
          "style": { "padding": 0, "paddingTop": 4, "paddingBottom": 0 },
          "action": { "type": "back" }
        },
        {
          "type": "header",
          "titleTemplate": "{{title}}",
          "subtitleTemplate": "{{year}} • {{genre}}"
        },
        {
          "type": "generatedList",
          "countBinding": "totalSeasons",
          "style": { "spacing": 10 },
          "itemLayout": {
            "type": "row",
            "style": { "padding": 14, "backgroundColor": "cardBackground", "cornerRadius": 18 },
            "children": [
              { "type": "icon", "icon": "play.tv.fill", "style": { "foregroundColor": "accent" } },
              { "type": "text", "template": "Season {{seasonNumber}}", "style": { "fontWeight": "semibold" } }
            ]
          }
        }
      ]
    }
  ]
}
```

### Render Pipeline

```
assets/screens/{screenId}.json
        │   LoadSDUIScreenUseCase.invoke(screenId)
        ▼
  SDUIRepository.loadScreen()
        │   Json.decodeFromString<ScreenModel>()
        ▼
  ScreenModel (in-memory tree)
        │   setState { copy(screenModel = it) }
        ▼
  SDUIRenderer(screenModel, isLoading, error, dataMap, listData, onAction)
        │   SDUIRenderEngine.Render()
        ▼
  for each ComponentNode → RenderNode(node, data, listData, onAction)
        │
        ├── "scroll"        → Column(verticalScroll)
        ├── "column"        → Column(spacedBy(spacing)) + background + padding
        ├── "row"           → Row(spacedBy) + clickable(action)
        ├── "topBar"        → Row: back icon (Box+clickable) | title | search icon
        ├── "header"        → Row: title Text + optional subtitle
        ├── "text"          → Text (dataBinding or {{template}} or literal; native TalkBack)
        ├── "image"         → AsyncImage(url from data[dataBinding]; contentDescription from props or "Movie poster")
        ├── "icon"          → Icon (star/search/playCircle; contentDescription "Rating"/"Search"/"Play")
        ├── "list"          → Column with drag-to-reorder; items: mergeDescendants + "long press to reorder"
        ├── "generatedList" → repeat(data[countBinding].toInt()) { i → renderNode(itemLayout) }
        └── unknown         → visible placeholder "[unknown: type]"
```

---

## 7. SDUI Component System

### Component Types

| Type | Description | Key props/bindings | Accessibility (`AccessibilityModel`) |
|---|---|---|---|
| `scroll` | Root scrollable container | `children` | transparent — no model needed |
| `column` | Vertical stack | `style.spacing`, `style.padding`, `style.backgroundColor` | transparent — no model needed |
| `row` | Horizontal stack, tappable | `action`, `style.cornerRadius` | `{ mergeDescendants: true, role: "button" }` when tappable |
| `topBar` | App bar with optional back/search | `props.leadingIcon="back"`, `props.trailingIcon="search"` | Back + Search hardcoded (internal elements); outer bar uses node's model |
| `header` | Title + subtitle row | `titleTemplate`, `subtitleTemplate` | `{ role: "header" }` to mark as heading for screen-reader navigation |
| `text` | Single text label | `dataBinding`, `template`, `text` | announced natively; no model needed |
| `image` | Async image | `dataBinding` → URL string | `{ label: "…", role: "image" }` — no built-in fallback |
| `icon` | System icon | `icon` name: `"star"`, `"search"`, `"play.tv.fill"` | `{ label: "…" }` or `{ importantForAccessibility: false }` for decorative |
| `list` | Data-bound list with drag-to-reorder | `listDataBinding`, `itemLayout` | `{ mergeDescendants: true, hint: "long press to reorder" }` — applied to every item |
| `generatedList` | Count-driven repeated layout | `countBinding`, `itemLayout` | delegates to item layout's own model |
| `card` | Elevated container | `style.cornerRadius`, `action` | `{ mergeDescendants: true, role: "button", hint: "open detail" }` when tappable |
| `spacer` | Vertical gap | `style.spacing` or `props.height` | transparent — decorative |
| `divider` | Horizontal rule | — | transparent — decorative |
| `button` | Tappable text button | `template`, `action` | `{ role: "button" }` — label from child `Text` via merge |

### Style System

Every component node has an optional `style` object. All properties are optional and fall back to safe defaults:

| JSON key | Type | Compose mapping |
|---|---|---|
| `backgroundColor` | color token | `Modifier.background(colorFromToken(it), shape)` |
| `foregroundColor` | color token | text/icon color argument |
| `cornerRadius` | Float (dp) | `RoundedCornerShape(it.dp)` |
| `padding` | Float (dp) | `Modifier.padding(all)` |
| `paddingTop/Bottom` | Float (dp) | `Modifier.padding(top/bottom)` |
| `spacing` | Float (dp) | `Arrangement.spacedBy(it.dp)` |
| `frameWidth/Height` | Float (dp) | `Modifier.size(width, height)` |
| `fontSize` | Float (sp) | `it.sp` |
| `fontWeight` | String | `"bold"` → Bold, `"semibold"` → SemiBold, `"medium"` → Medium |
| `lineLimit` | Int | `maxLines = it` |

### Template Resolution

Any string containing `{{key}}` is a template, resolved at render time:

```kotlin
// JSON:  "template": "Season {{seasonNumber}}"
// data:  { "seasonNumber": "3" }
// output: "Season 3"

fun resolve(template: String, data: Map<String, String>): String =
    templateRegex.replace(template) { data[it.groupValues[1]] ?: it.value }
```

Templates work in: `text.template`, `header.titleTemplate`, `header.subtitleTemplate`, `action.routeTemplate`.

### Visibility Rules

A component can declare a conditional visibility guard:

```json
"visibility": { "dataBinding": "awards", "isNotEmpty": true }
```

The renderer evaluates this before composing the node. If `data["awards"]` is empty or `"N/A"`, the node is skipped. No placeholder is rendered — the space simply does not exist.

### Action Dispatch

Actions are resolved at tap time and forwarded to the ViewModel as `(actionId, params)`:

```kotlin
// JSON action on a list item row:
// "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" }

ActionModel.dispatch(data, onAction):
  1. Resolve routeTemplate: "series_detail/{{id}}" → "series_detail/tt1234567"
  2. Call: onAction("navigate", { "route": "series_detail/tt1234567" })
  3. ViewModel: setEffect(Navigate("series_detail/tt1234567"))
  4. Screen: navController.navigate("series_detail/tt1234567")
```

Supported action types: `navigate`, `back`, `search`, `reorder`.

### Extensibility

Register a custom component without touching the engine:

```kotlin
ComponentRegistry.register("ratingBadge") { node, data, listData, onAction ->
    RatingBadgeComposable(node.style, data["rating"])
}
```

Custom types take precedence over built-ins and are resolved before the built-in `when` branch.

---

## 8. Data Pipeline — End to End

### Series List (cold launch → rendered list)

```
1. MoviesScreen → hiltViewModel() → MoviesViewModel.init
       └── handleIntent(LoadScreen)

2. LoadSDUIScreenUseCase("tv_series_list")
       └── assets/screens/tv_series_list.json → ScreenModel
       └── setState(screenModel = model, isLoading = true)
           ▶ SDUIRenderer renders immediately: header + empty list skeleton

3. DataSourceExecutor.execute(dataSource)      ← no params needed for list

   Phase A — Main API call:
       GET https://www.omdbapi.com/?s=game&type=series&apikey=...
       Response: { "Search": [ {imdbID, Title, Year, Poster}, ... ] }
       Parse: root="Search" → extract array
       Apply fieldMapping: imdbID→id, Title→title, Year→year, Poster→posterURL
       Result: List<Map<String,String>> (10 items)

   Phase B — Parallel enrichment (see Section 9):
       10 concurrent calls → merge rating + genre into each item

4. setState(isLoading = false, listData = { "series": [10 enriched items] })
       ▶ SDUIRenderer re-renders with data bound to each row

5. User taps a row:
       action.routeTemplate = "series_detail/{{id}}"
       resolved → "series_detail/tt0903747"
       onAction("navigate", { route: "series_detail/tt0903747" })
       setEffect(Navigate("series_detail/tt0903747"))
       navController.navigate("series_detail/tt0903747")
```

### Series Detail (seriesId received from nav argument)

```
1. SeriesDetailScreen(seriesId = "tt0903747")
       └── handleIntent(Load("tt0903747"))

2. LoadSDUIScreenUseCase("series_detail")
       └── assets/screens/series_detail.json → ScreenModel
       └── setState(screenModel = model, isLoading = true)
           ▶ SDUIRenderer renders immediately: back button + skeleton

3. DataSourceExecutor.execute(dataSource, params = { "seriesId" → "tt0903747" })
       URL template: "https://www.omdbapi.com/?i={{seriesId}}&apikey=..."
       Resolved URL: "https://www.omdbapi.com/?i=tt0903747&apikey=..."
       Response: { "Title": "Game of Thrones", "imdbRating": "9.3", ... }
       Apply fieldMapping → Map<String,String>

4. setState(isLoading = false, data = { title, year, genre, rating, plot, ... })
       ▶ SDUIRenderer binds every field to its component via dataBinding/template
       ▶ generatedList reads data["totalSeasons"].toInt() → renders 8 season rows

5. User taps back:
       topBar action.type = "back"
       onAction("back", {})
       handleIntent(NavigateBack)
       setEffect(GoBack)
       navController.popBackStack()
```

---

## 9. Parallel API Execution & Enrichment

The movies list screen requires two API tiers:

- **Main call**: searches OMDb, returns 10 items with basic fields (id, title, year, posterURL)
- **Enrichment calls**: one detail call per item to fetch rating + genre

`DataSourceExecutor` fires all enrichment calls concurrently using `coroutineScope + async/awaitAll`:

```kotlin
suspend fun execute(
    dataSource: DataSourceModel,
    params: Map<String, String> = emptyMap(),
): List<Map<String, String>> = coroutineScope {

    // Resolve {{key}} placeholders in the main URL (used for detail screen)
    val mainItems = fetchAndMap(dataSource, params)

    val enrichment = dataSource.enrichmentDataSource
        ?: return@coroutineScope mainItems  // no enrichment defined → return as-is

    // Fire all enrichment calls at the same time
    mainItems.map { item ->
        async {
            val url = item.entries.fold(enrichment.effectiveUrl) { acc, (k, v) ->
                acc.replace("{{$k}}", v)   // e.g. {{id}} → "tt0903747"
            }
            val enrichedFields = fetchAndMap(enrichment.copy(request = ...), emptyMap())
            item + enrichedFields.firstOrNull().orEmpty()   // merge into single map
        }
    }.awaitAll()
}
```

**Performance impact:**

```
Sequential (10 items × 150ms):    1 500ms total
─────────────────────────────────────────────────
main ──150ms──┤
enrich[0]     ──150ms──┤
enrich[1]              ──150ms──┤
...
enrich[9]                                ──150ms──┤


Parallel (this implementation):     ~300ms total
─────────────────────────────────────────────────
main ──150ms──┤
enrich[0..9]  ──150ms──┤  (all fire simultaneously)
```

The enriched `List<Map<String, String>>` is stored in `MoviesState.listData["series"]` and the engine binds it directly by key name with no additional mapping.

---

## 10. URL Template Resolution

Both the main URL and enrichment URLs support `{{key}}` placeholders:

```
Main URL (resolved from ViewModel params):
  Template: "https://www.omdbapi.com/?i={{seriesId}}&apikey=..."
  params:   { "seriesId": "tt0903747" }
  Resolved: "https://www.omdbapi.com/?i=tt0903747&apikey=..."

Enrichment URL (resolved from item data):
  Template: "https://www.omdbapi.com/?i={{id}}&apikey=..."
  item:     { "id": "tt0903747", "title": "Game of Thrones", ... }
  Resolved: "https://www.omdbapi.com/?i=tt0903747&apikey=..."
```

`DataSourceExecutor.fetchAndMap()` applies param substitution before the HTTP call:

```kotlin
private suspend fun fetchAndMap(
    ds: DataSourceModel,
    params: Map<String, String>,
): List<Map<String, String>> {
    val url = params.entries.fold(ds.effectiveUrl) { acc, (k, v) ->
        acc.replace("{{$k}}", v)
    }.ifEmpty { return emptyList() }
    ...
}
```

This single mechanism powers both the list detail calls (enrichment with item data) and the detail screen call (ViewModel-provided `seriesId`).

---

## 11. Data Binding to View

Once data is in state, `SDUIRenderer` passes the flat `Map<String, String>` into the component tree. Each node resolves its value independently:

```
state.data = {
  "title":        "Game of Thrones",
  "year":         "2011–2019",
  "genre":        "Action, Adventure, Drama",
  "rating":       "9.3",
  "plot":         "Nine noble families ...",
  "totalSeasons": "8",
  "posterURL":    "https://...",
  "runtime":      "57 min",
  "actors":       "Emilia Clarke, Peter Dinklage ..."
}

JSON node                                Compose output
──────────────────────────────────────────────────────────────────
{ type:"header", titleTemplate:"{{title}}" }
  → Text("Game of Thrones", fontSize=24sp, fontWeight=Bold)

{ type:"header", subtitleTemplate:"{{year}} • {{genre}}" }
  → Text("2011–2019 • Action, Adventure, Drama", fontSize=14sp)

{ type:"image", dataBinding:"posterURL" }
  → AsyncImage(model="https://...", contentScale=Crop)

{ type:"text", template:"IMDb {{rating}}" }
  → Text("IMDb 9.3", fontWeight=SemiBold)

{ type:"generatedList", countBinding:"totalSeasons" }
  data["totalSeasons"].toInt() = 8
  → renders 8 season rows, each with { "seasonNumber": "1" } ... { "seasonNumber": "8" }
    { type:"text", template:"Season {{seasonNumber}}" } → Text("Season 1") ... Text("Season 8")

{ type:"text", dataBinding:"awards", visibility:{ isNotEmpty:true } }
  data["awards"] = "Won 59 Primetime Emmys"
  → Text("Won 59 Primetime Emmys") rendered
  (if data["awards"] == "N/A" → node skipped entirely)
```

There is no mapping code at any layer between the API response and the rendered text. The JSON `fieldMapping` maps API keys to SDUI keys once; the component tree binds those SDUI keys directly.

---

## 12. Features in Detail

### Series List Screen

**Entry**: `MoviesScreen.kt` — a 35-line file that does nothing but pass ViewModel state into `SDUIRenderer`.

**ViewModel** (`MoviesViewModel`):
```
init → LoadScreen intent
     → LoadSDUIScreenUseCase("tv_series_list")  → ScreenModel
     → DataSourceExecutor.execute(dataSource)    → 10 enriched items
     → setState(listData = { "series": items })
```

**JSON screen** (`tv_series_list.json`):
- `type: "list"` with `listDataBinding: "series"` — renders one row per item
- Each row has `action: { type: "navigate", routeTemplate: "series_detail/{{id}}" }`
- Row children: poster image + title + year•type + genre + IMDb rating
- `enrichmentDataSource` defined → triggers parallel enrichment for rating + genre

**Supported actions from ViewModel:**

| Action | Trigger | Result |
|---|---|---|
| `navigate` | Row tap | `setEffect(Navigate(route))` → `navController.navigate()` |
| `reorder` | Long-press drag end | `reorderList(binding, from, to)` in-memory state update |
| `search` | (Phase 3) | reserved |

---

### Series Detail Screen

**Entry**: `SeriesDetailScreen.kt` — a 40-line file. The ViewModel receives `seriesId` from the nav argument and does everything else.

**ViewModel** (`SeriesDetailViewModel`):
```
Load(seriesId) intent
  → LoadSDUIScreenUseCase("series_detail")       → ScreenModel
  → DataSourceExecutor.execute(dataSource,
       params = { "seriesId": seriesId })         → single detail object
  → setState(data = items.firstOrNull())
```

**JSON screen** (`series_detail.json`):
- Back button via `topBar` with `props.leadingIcon: "back"` → `action.type: "back"`
- Title + subtitle via `header` with `titleTemplate`/`subtitleTemplate`
- Hero card: poster image + rating + runtime + seasons count + awards (visibility-guarded)
- Synopsis card: plot text
- Credits card: cast, writer, director
- Seasons: `generatedList` with `countBinding: "totalSeasons"` — auto-generates N season rows from data, no loop code anywhere

**No Kotlin UI code** describes what the detail screen looks like. The layout, spacing, colors, and data bindings are all declared in `series_detail.json`.

---

### Banking Screen

Placeholder — `Box { Text("TODO") }`. No ViewModel, no network, no domain layer. Reserved for future implementation.

---

## 13. Drag-to-Reorder

The series list supports long-press drag-to-reorder. Implementation lives entirely in `SDUIComponents.RenderList` since the list is inside a `verticalScroll` Column (LazyColumn cannot be used inside a scrollable ancestor).

```
User long-presses a row
  └── detectDragGesturesAfterLongPress
        onDragStart: draggingIndex = index, dragOffsetY = 0f
        onDrag:      dragOffsetY += dragAmount.y
                     graphicsLayer { translationY = dragOffsetY }  ← visual only, no layout change
                     zIndex = 1f                                    ← float above siblings
                     scaleX/Y = 1.03f                              ← lift effect
        onDragEnd:
          targetIndex = (draggingIndex + dragOffsetY / avgItemHeight).roundToInt()
                          .coerceIn(list.indices)
          onAction("reorder", { binding, from, to })
          draggingIndex = -1, dragOffsetY = 0f

ViewModel.handleAction("reorder"):
  reorderList(binding, from, to):
    val list = listData[binding].toMutableList()
    list.add(to, list.removeAt(from))
    setState { copy(listData = listData + (binding to list)) }
```

Non-dragged items that should make room for the dragged item are visually shifted with the same `graphicsLayer { translationY }` approach — no layout recomposition, just a transform.

`key(itemData["id"])` ensures Compose tracks each item by its series ID rather than list position, so gesture state survives reorders correctly.

**Persistence**: The order lives in `MoviesState.listData` — a `StateFlow` in memory. It resets when the process is killed. This is intentional; no SharedPreferences or Room writes are performed.

---

## 14. TalkBack & Accessibility

Both the list and detail screens are fully compatible with Android TalkBack (screen reader) and all other accessibility services that consume the Compose semantics tree.

### Design principle

Accessibility is a **first-class field on every `ComponentNode`**, not a side-channel through `props`. Every component renderer calls a single extension function; the JSON screen definition drives what TalkBack announces.

```
JSON screen definition
  └── ComponentNode.accessibility: AccessibilityModel
        └── Modifier.applyAccessibility(model)   ← single utility
              └── Compose semantics tree
                    └── TalkBack / a11y services
```

### No permissions required

TalkBack is a system service toggled from **Settings → Accessibility**. No `<uses-permission>` entry is needed; `android.permission.INTERNET` (already declared) remains the only manifest permission.

---

### Data model — `AccessibilityModel`

Defined in `core/network/src/main/java/.../model/ScreenModel.kt` alongside all other node models:

```kotlin
@Serializable
data class AccessibilityModel(
    // Overrides what TalkBack announces.
    // Omit when child Text nodes already carry the full meaning.
    val label: String? = null,

    // Action label — TalkBack says "double-tap to <hint>".
    val hint: String? = null,

    // Semantic role: "button" | "image" | "checkbox" | "switch" | "tab" | "header"
    val role: String? = null,

    // Collapse all descendant nodes into one focus stop.
    val mergeDescendants: Boolean? = null,

    // false → node is removed from the accessibility tree entirely.
    val importantForAccessibility: Boolean? = null,
)
```

`ComponentNode` carries it as an optional field:

```kotlin
data class ComponentNode(
    ...
    val accessibility: AccessibilityModel? = null,
)
```

---

### The utility — `Modifier.applyAccessibility`

`engine/sdui/src/main/java/.../engine/sdui/AccessibilityUtils.kt`

```kotlin
fun Modifier.applyAccessibility(model: AccessibilityModel?): Modifier {
    model ?: return this                                          // null → no-op

    if (model.importantForAccessibility == false)
        return clearAndSetSemantics {}                           // remove from tree

    return semantics(mergeDescendants = model.mergeDescendants == true) {
        model.label?.let { contentDescription = it }

        model.role?.let { r ->
            when (r.lowercase()) {
                "button"   -> role = Role.Button
                "image"    -> role = Role.Image
                "checkbox" -> role = Role.Checkbox
                "switch"   -> role = Role.Switch
                "tab"      -> role = Role.Tab
                "header"   -> heading()                         // no Role.Header in Compose
            }
        }

        model.hint?.let { hint -> onClick(label = hint) { false } }
    }
}
```

**Decision table:**

| `AccessibilityModel` state | Result |
|---|---|
| `null` | No-op — component is transparent to the semantics tree |
| `importantForAccessibility = false` | `clearAndSetSemantics {}` — node removed from tree; short-circuits everything else |
| `mergeDescendants = true` | All descendant semantics collapse into one focus stop |
| `label` present | Sets `contentDescription`; omit when visible `Text` children are already sufficient |
| `role` present | Maps string → `Role.*`; `"header"` maps to `heading()` (Compose has no `Role.Header`) |
| `hint` present | Registers `onClick(label = hint)` — TalkBack announces "double-tap to `<hint>`" |

**label vs. visible text:**
Set `label` only when there is no visible text (image, icon-only button). For containers whose child `Text` nodes already carry the full meaning, set `mergeDescendants = true` and omit `label` — TalkBack merges and reads the visible text without duplication.

---

### Component-by-component breakdown

Every built-in component calls `.applyAccessibility(node.accessibility)` on its root modifier. Accessibility is fully driven by the JSON; no Kotlin change is needed when labels or roles change.

#### `image` (`ImageComponent.kt`)

```kotlin
val mod = ...clip(...).applyAccessibility(node.accessibility)

AsyncImage(
    model = url,
    contentDescription = null,  // semantics come from applyAccessibility via modifier
    modifier = mod,
)
```

`contentDescription = null` is intentional — the `AsyncImage` parameter is bypassed; semantics are owned exclusively by `applyAccessibility`.

#### `icon` (`IconComponent.kt`)

```kotlin
Icon(
    imageVector = icon,
    contentDescription = null,  // semantics come from applyAccessibility via modifier
    modifier = Modifier.size(size).applyAccessibility(node.accessibility),
)
```

#### `list` (`ListComponent.kt`)

The item `Box` applies the list node's `AccessibilityModel` (set once in JSON, applied to every item):

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .applyAccessibility(node.accessibility)   // e.g. mergeDescendants=true, hint="long press to reorder"
        ...
)
```

The drag-handle icon retains a hardcoded `clearAndSetSemantics {}` — it is a structural element, not a `ComponentNode`, and must always be excluded from the merged item node:

```kotlin
Icon(
    imageVector = Icons.Default.DragHandle,
    contentDescription = "Drag to reorder",
    modifier = Modifier...clearAndSetSemantics {},   // excluded from merged parent
)
```

#### `card` (`CardComponent.kt`)

```kotlin
var mod = Modifier
    .fillMaxWidth()
    .padding(...)
    .applyAccessibility(node.accessibility)
if (action != null) mod = mod.clickable { ... }
```

JSON for a tappable card:
```json
"accessibility": { "mergeDescendants": true, "role": "button", "hint": "open detail" }
```

#### `button` (`ButtonComponent.kt`)

```kotlin
Box(
    modifier = Modifier
        .padding(...)
        .applyAccessibility(node.accessibility)
        .clickable { ... },
) { Text(label) }
```

JSON: `"accessibility": { "role": "button" }` — `label` can be omitted because the child `Text` carries the announcement text.

#### `row` (`RowComponent.kt`)

```kotlin
var mod = Modifier.fillMaxWidth().applyAccessibility(node.accessibility)
if (action != null) mod = mod.clickable { ... }
```

JSON for a tappable season row:
```json
"accessibility": { "mergeDescendants": true, "role": "button" }
```

#### `topBar` (`TopBarComponent.kt`)

The outer `Column` receives the node's `AccessibilityModel` via `applyAccessibility`. The **back button** and **search icon** are internal hardcoded elements (not separate `ComponentNode`s), so their semantics are hardcoded directly — not via `props`:

| Element | How semantics are set |
|---|---|
| Outer `Column` | `applyAccessibility(node.accessibility)` |
| Back button `Box` | Hardcoded: `role = Role.Button`, `contentDescription = "Navigate back"` |
| Arrow `Icon` inside back | `contentDescription = null` — suppressed to prevent double-announcement |
| Search `IconButton` | Hardcoded: `contentDescription = "Search"` (Material sets role automatically) |
| Title `Text` | Announced natively |
| Subtitle `Text` | Announced natively |

#### `header` / `text`

Both render standard Compose `Text`. TalkBack announces visible text natively. Set `"accessibility": { "role": "header" }` on a `header` node to mark section headings for screen reader navigation.

---

### JSON authoring guide

```json
// Image with explicit label
{
  "type": "image",
  "dataBinding": "posterURL",
  "accessibility": { "label": "Series poster", "role": "image" }
}

// Tappable card — merge all child text into one focus stop
{
  "type": "card",
  "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" },
  "accessibility": { "mergeDescendants": true, "role": "button", "hint": "open detail" }
}

// Icon that decorates a rating — excluded from accessibility tree
{
  "type": "icon",
  "icon": "star",
  "accessibility": { "importantForAccessibility": false }
}

// Draggable list — each item merges descendants + announces drag hint
{
  "type": "list",
  "listDataBinding": "series",
  "accessibility": { "mergeDescendants": true, "hint": "long press to reorder" }
}

// Section header — navigable via TalkBack headings shortcut
{
  "type": "header",
  "titleTemplate": "Synopsis",
  "accessibility": { "role": "header" }
}
```

---

### Focus traversal — List screen

```
TalkBack swipe-right order:
  1. "Series Hub"                    ← header title (native Text)
  2. "Top rated series"              ← header subtitle (native Text)
  3. "Search" button                 ← IconButton, hardcoded contentDescription
  4–N. Each series item              ← merged node: all child text + "double-tap to open detail"
       drag handle excluded          ← clearAndSetSemantics {} suppresses it
```

### Focus traversal — Detail screen

```
TalkBack swipe-right order:
  1. "Navigate back" button          ← hardcoded role=Button
  2. "Game of Thrones"               ← header title (native Text)
  3. "2011–2019 • Action, Drama"     ← header subtitle (native Text)
  4. Hero card                       ← mergeDescendants: poster + rating + runtime + seasons
  5. "Won 59 Primetime Emmys"        ← conditional, skipped when empty
  6. "Synopsis"                      ← role=header, navigable via heading shortcut
  7. <plot text>
  8. "Cast: …"
  9–N. "Season 1" … "Season N"      ← role=button each, "double-tap to open"
```

---

## 15. Libraries & Versions

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.0.21 | Language |
| AGP | 8.13.2 | Android Gradle Plugin |
| KSP | 2.0.21-1.0.28 | Annotation processor (Hilt, Room) |
| Jetpack Compose BOM | 2024.09.00 | Compose UI toolkit |
| Material 3 | (BOM) | Design system |
| Hilt | 2.51.1 | Dependency injection |
| Compose Navigation | 2.8.3 | Type-safe screen navigation |
| Retrofit | 2.11.0 | REST adapter |
| OkHttp | 4.12.0 | HTTP engine + logging interceptor |
| kotlinx.serialization | 1.7.3 | JSON parsing (custom Retrofit converter) |
| Room | 2.6.1 | SQLite for local watchlist |
| Coil 3 | 3.0.4 | Async image loading |
| Kotlin Coroutines | 1.8.1 | async/await, Flow, StateFlow, Channel |

---

## 16. Project File Tree

```
MoviesDemoAPP/
├── app/
│   └── src/main/java/.../app/
│       ├── MainActivity.kt
│       ├── MainNavHost.kt
│       └── di/AppModule.kt
│
├── core/
│   ├── domain/src/main/java/.../core/domain/
│   │   ├── BaseViewModel.kt
│   │   ├── UiContract.kt
│   │   ├── Result.kt
│   │   └── BaseUseCase.kt
│   │
│   ├── network/src/main/
│   │   ├── assets/screens/
│   │   │   ├── tv_series_list.json      ← SDUI: movies list screen + data source
│   │   │   └── series_detail.json       ← SDUI: detail screen + data source
│   │   └── java/.../core/network/
│   │       ├── di/NetworkModule.kt
│   │       ├── model/DataSourceModel.kt
│   │       ├── model/ScreenModel.kt         ← ComponentNode, AccessibilityModel, StyleModel, ActionModel, VisibilityModel
│   │       ├── model/OmdbDtos.kt
│   │       ├── OmdbApiService.kt
│   │       ├── NetworkClient.kt
│   │       ├── NetworkClientImpl.kt
│   │       └── KotlinxSerializationConverterFactory.kt
│   │
│   ├── data/src/main/java/.../core/data/
│   │   ├── remote/
│   │   │   ├── SDUIDataRepository.kt        ← interface: fetch(url) → String?
│   │   │   ├── SDUIDataRepositoryImpl.kt    ← OkHttp impl
│   │   │   └── DataSourceExecutor.kt        ← main fetch + parallel enrichment
│   │   ├── local/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── WatchlistDao.kt
│   │   │   └── WatchlistEntity.kt
│   │   └── di/DataModule.kt
│   │
│   └── ui/src/main/java/.../core/ui/
│       ├── DesignTokens.kt
│       ├── MovieAppTheme.kt
│       └── colorFromToken.kt
│
├── engine/
│   ├── sdui/src/main/java/.../engine/sdui/
│   │   ├── SDUIRenderer.kt              ← public Composable + SDUIRenderEngine
│   │   ├── SDUIComponentsDispatcher.kt  ← routes type → built-in renderer
│   │   ├── AccessibilityUtils.kt        ← Modifier.applyAccessibility(AccessibilityModel?)
│   │   ├── TemplateResolver.kt          ← {{key}} resolution + visibility evaluation
│   │   ├── ComponentRegistry.kt         ← extensible custom component registry
│   │   ├── usecase/LoadSDUIScreenUseCase.kt
│   │   └── components/
│   │       ├── TopBarComponent.kt       ← back (role=Button) + search icon
│   │       ├── HeaderComponent.kt       ← title + subtitle + optional search
│   │       ├── ColumnComponent.kt       ← vertical stack
│   │       ├── RowComponent.kt          ← horizontal stack, role=Button when tappable
│   │       ├── CardComponent.kt         ← mergeDescendants, role=Button when tappable
│   │       ├── ListComponent.kt         ← drag-to-reorder, item mergeDescendants
│   │       ├── GeneratedListComponent.kt← count-driven repeated layout
│   │       ├── TextComponent.kt         ← text (native TalkBack)
│   │       ├── ImageComponent.kt        ← contentDescription from props or "Movie poster"
│   │       ├── IconComponent.kt         ← "Rating" / "Search" / "Play" descriptions
│   │       ├── ButtonComponent.kt       ← role=Button + label contentDescription
│   │       ├── SpacerComponent.kt       ← decorative gap (no semantics)
│   │       └── DividerComponent.kt      ← decorative rule (no semantics)
│   │
│   └── navigation/src/main/java/.../engine/navigation/
│       ├── Routes.kt
│       ├── NavigationAction.kt
│       └── NavigationEngine.kt
│
└── feature/
    ├── movies/src/main/java/.../feature/movies/ui/
    │   ├── list/
    │   │   ├── MoviesContract.kt        ← MoviesState, MoviesIntent, MoviesEffect
    │   │   ├── MoviesViewModel.kt       ← SDUI loader + reorder logic
    │   │   └── MoviesScreen.kt          ← SDUIRenderer wrapper (35 lines)
    │   ├── detail/
    │   │   ├── SeriesDetailContract.kt  ← SeriesDetailState, Intent, Effect
    │   │   ├── SeriesDetailViewModel.kt ← SDUI loader with seriesId param
    │   │   └── SeriesDetailScreen.kt    ← SDUIRenderer wrapper (40 lines)
    │   └── MoviesNavGraph.kt
    │
    └── banking/src/main/java/.../feature/banking/ui/
        ├── BankingScreen.kt             ← Box { Text("TODO") }
        └── BankingNavGraph.kt
```
