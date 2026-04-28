# MoviesDemoApp

> **A production-grade Android application** built on a fully **Server-Driven UI (SDUI)** engine, **MVI architecture**, and **Clean Architecture** across all modules. Every screen — list, detail, and form — is rendered entirely from JSON definitions with zero hardcoded UI layouts at the feature level. Navigation structure, bottom tabs, labels, and localization are all driven by JSON configuration.

---

## Table of Contents

1. [Architecture Philosophy](#1-architecture-philosophy)
2. [Module Structure](#2-module-structure)
3. [Module Dependency Graph](#3-module-dependency-graph)
4. [Module Responsibilities](#4-module-responsibilities)
5. [MVI Architecture](#5-mvi-architecture)
6. [Server-Driven UI Engine](#6-server-driven-ui-engine)
7. [SDUI Component System](#7-sdui-component-system)
8. [Binding & Localization System](#8-binding--localization-system)
9. [Data Pipeline — End to End](#9-data-pipeline--end-to-end)
10. [Parallel API Execution & Enrichment](#10-parallel-api-execution--enrichment)
11. [URL Template Resolution](#11-url-template-resolution)
12. [Data Binding to View](#12-data-binding-to-view)
13. [Dynamic Tab Navigation](#13-dynamic-tab-navigation)
14. [Features in Detail](#14-features-in-detail)
15. [Drag-to-Reorder](#15-drag-to-reorder)
16. [TalkBack & Accessibility](#16-talkback--accessibility)
17. [Libraries & Versions](#17-libraries--versions)
18. [Project File Tree](#18-project-file-tree)

---

## 1. Architecture Philosophy

This project is built around a single central principle: **the feature module should contain no knowledge of APIs, data models, UI layouts, or display strings**. All of that lives in the SDUI engine, the binding resolver, and their supporting infrastructure.

```
Traditional Android approach          This project
────────────────────────────          ──────────────────────────────────────────
Feature has:                          Feature has:
  - Retrofit calls                      - ViewModel (dispatches intents)
  - Domain models                       - Contract (state / intent / effect)
  - Mappers                             - Screen (SDUIRenderer wrapper)
  - Repository implementations          - NavGraph
  - Use cases
  - Hardcoded Composables             Everything else lives in:
  - Hardcoded string references         - engine:sdui     (render engine)
  - Hardcoded navigation tabs           - core:data       (data executor)
                                        - core:network    (HTTP + models)
                                        - assets/screens/ (JSON definitions)
```

**Adding a new screen** = adding a JSON file. No Kotlin UI code required.  
**Adding a new tab** = editing `tab_config.json`. No Kotlin change required.  
**Changing a label** = editing a string resource key in JSON. No recompile required in future remote-config scenarios.

---

## 2. Module Structure

11 Gradle modules in 5 groups, each with its own `build.gradle.kts` and strict dependency boundaries:

| Group | Module | Role |
|---|---|---|
| App shell | `:app` | Entry point, NavHost, SDUI-driven bottom navigation |
| Features | `:feature:movies` | TV series list + detail (UI only) |
| Features | `:feature:banking` | Multi-step form: personal details, address, financials, review |
| Features | `:feature:deeplink` | Deep link entry handling |
| Features | `:feature:analytics` | Firebase/analytics event tracking |
| Engines | `:engine:sdui` | JSON → Compose render engine + binding resolution |
| Engines | `:engine:navigation` | Shared routes and navigation contracts |
| Core | `:core:domain` | BaseViewModel, MVI contracts, Result type |
| Core | `:core:network` | HTTP client, models, `StringResolver` interface, `ScreenSource` |
| Core | `:core:data` | `DataSourceExecutor`, Room, screen loading |
| Core | `:core:ui` | `DesignTokens`, shared Compose components |

---

## 3. Module Dependency Graph

```
:app
 ├── :feature:movies
 │    ├── :core:domain        ← BaseViewModel, UiState/Intent/Effect
 │    ├── :core:data          ← DataSourceExecutor
 │    ├── :core:ui            ← DesignTokens
 │    ├── :engine:sdui        ← SDUIRenderer, BindingResolver
 │    └── :engine:navigation  ← Routes
 ├── :feature:banking
 │    ├── :core:domain
 │    ├── :core:data
 │    ├── :engine:sdui
 │    └── :engine:navigation
 ├── :feature:deeplink
 ├── :feature:analytics
 ├── :engine:sdui
 │    ├── :core:network       ← ScreenModel, BindingItem, StringResolver
 │    ├── :core:data          ← DataSourceExecutor, SDUIDataRepository
 │    └── :core:ui            ← DesignTokens, colorFromToken()
 ├── :core:data
 │    └── :core:network       ← NetworkClient, DataSourceModel, ScreenSource
 ├── :core:network            (no internal project deps)
 ├── :core:domain             (no deps — pure Kotlin)
 └── :core:ui                 (Compose only)
```

**Hard rules enforced by module boundaries:**
- Feature modules never depend on each other.
- `:core:domain` has zero Android or third-party dependencies.
- `:core:network` defines all shared interfaces and models — both data and engine depend on it, never the reverse.
- Features never import from `:core:network` directly; they go through the engine and data layers.

---

## 4. Module Responsibilities

### `:core:domain`
Pure Kotlin. The MVI foundation used by every ViewModel across all features.

```
core/domain/
├── BaseViewModel.kt    — abstract MVI base: StateFlow, Channel<Effect>, handleIntent()
├── UiContract.kt       — UiState, UiIntent, UiEffect marker interfaces
└── Result.kt           — sealed class: Success<T> | Error(code, message) | Loading
```

`BaseViewModel` is the single shared ViewModel base:

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

All HTTP infrastructure, shared model definitions, and core interfaces.

```
core/network/
├── di/
│   └── NetworkModule.kt                     — Hilt: Json, OkHttpClient
├── model/
│   ├── ScreenModel.kt                       — ScreenModel, ComponentNode, BindingItem,
│   │                                          StyleModel, ActionModel, AccessibilityModel,
│   │                                          VisibilityModel, DataSourceModel
│   └── DataSourceModel.kt                   — DataSourceModel, RequestModel, ResponseModel
├── StringResolver.kt                        — interface: fun resolve(key: String): String
├── ScreenSource.kt                          — interface: suspend fun load(screenId): String?
├── NetworkClient.kt                         — interface: suspend fun get(url): String?
└── OkHttpNetworkClient.kt                   — OkHttp implementation, Dispatchers.IO
```

`StringResolver` is the clean-architecture boundary for localized strings. It lives in `:core:network` (no Android dependency) so both engine and data layers can reference it without importing Android framework types.

```kotlin
interface StringResolver {
    fun resolve(key: String): String
}
```

`DataSourceModel` is the data contract between JSON screen definitions and the data executor:

```kotlin
@Serializable
data class DataSourceModel(
    val type: String,
    val request: RequestModel? = null,
    val response: ResponseModel? = null,
    val enrichmentDataSource: DataSourceModel? = null,
) {
    val effectiveUrl: String get() = request?.url ?: ""
    val fieldMapping: Map<String, String> get() = response?.fieldMapping ?: emptyMap()
    val effectiveRoot: String? get() = response?.root
}
```

---

### `:core:data`

Data execution layer — drives all SDUI data fetching and local persistence.

```
core/data/
├── remote/
│   ├── SDUIDataRepository.kt       — interface: suspend fun fetch(url): String?
│   ├── SDUIDataRepositoryImpl.kt   — NetworkClient-backed implementation
│   └── DataSourceExecutor.kt       — main fetch + parallel enrichment
├── local/
│   ├── AppDatabase.kt              — @Database(WatchlistEntity)
│   ├── WatchlistDao.kt             — getAll(), insert(), deleteById(), isInWatchlist()
│   ├── WatchlistEntity.kt          — @Entity: imdbID, title, posterUrl, rating, year, genre
│   └── LocalScreenSource.kt        — reads assets/screens/{screenId}.json
└── di/
    └── DataModule.kt               — @Binds SDUIDataRepository, ScreenSource; @Provides Room
```

`DataSourceExecutor.execute()` is the most critical class in the project. It accepts an optional `params` map to resolve URL templates before execution, enabling both list and detail screens to fetch data without any feature-level code:

```kotlin
suspend fun execute(
    dataSource: DataSourceModel,
    params: Map<String, String> = emptyMap(),   // resolves {{key}} placeholders in URL
): List<Map<String, String>>
```

---

### `:core:ui`

Shared design tokens and Compose primitives.

```
core/ui/
├── DesignTokens.kt      — all colors, spacing, typography, radii as Compose constants
│                          ScreenBackground (#0D0F14), CardBackground (#1A1D27)
│                          Surface (#1E2132), Accent (#E05C5C)
│                          SpacingXs(4dp) → SpacingXl(32dp)
│                          TextSm(12sp) → TextXxl(24sp)
├── colorFromToken()     — maps SDUI JSON color string → Compose Color at render time
├── AndroidStringResolver.kt  — NOT here (lives in engine:sdui — see Section 8)
└── MovieAppTheme.kt     — MaterialTheme wrapper using DesignTokens
```

`colorFromToken()` bridges JSON style tokens and live Compose colors. Every `backgroundColor` and `foregroundColor` in any JSON file is resolved through this function:

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

The generic Server-Driven UI engine. Contains no knowledge of movies, banking, or any domain.

```
engine/sdui/
├── SDUIRenderer.kt              — public @Composable entry point + SDUIRenderEngine
│                                  Resolves bindings, merges enrichedData, renders tree
├── SDUIComponentsDispatcher.kt  — routes ComponentNode.type → built-in renderer
├── AccessibilityUtils.kt        — Modifier.applyAccessibility(AccessibilityModel?, data)
│                                  resolveTokens() extension for {{key}} in a11y labels
├── TemplateResolver.kt          — {{key}} placeholder resolution + visibility evaluation
├── BindingResolver.kt           — resolveAll(apiData): api → string → template pipeline
├── AndroidStringResolver.kt     — Context-based StringResolver implementation
├── StringResolverModule.kt      — Hilt @Provides StringResolver → AndroidStringResolver
├── FormDataStorage.kt           — singleton form state for multi-step banking forms
├── ComponentRegistry.kt         — extensible registry for feature-specific components
├── SduiComponentProvider.kt     — functional interface for Hilt @IntoSet contributions
└── components/
    ├── TopBarComponent.kt
    ├── HeaderComponent.kt
    ├── ColumnComponent.kt
    ├── RowComponent.kt
    ├── CardComponent.kt
    ├── ListComponent.kt
    ├── GeneratedListComponent.kt
    ├── TextComponent.kt
    ├── ImageComponent.kt
    ├── IconComponent.kt
    ├── ButtonComponent.kt
    ├── SpacerComponent.kt
    └── DividerComponent.kt
```

---

### `:engine:navigation`

Navigation contracts shared across all features.

```
engine/navigation/
├── Routes.kt              — MOVIES, SERIES_DETAIL, BANKING_* route constants
├── NavigationAction.kt    — NavType (PUSH/REPLACE/POP) + destination
└── NavigationEngine.kt    — navigate(NavController, NavigationAction)
```

---

### `:feature:movies`

TV series list and detail. Contains **only UI code** — no API calls, no domain models, no use cases.

```
feature/movies/
├── src/main/java/.../feature/movies/ui/
│   ├── MoviesContract.kt        — MoviesState, MoviesIntent, MoviesEffect
│   ├── MoviesViewModel.kt       — loads tv_series_list JSON, executes dataSource, reorder
│   ├── MoviesScreen.kt          — SDUIRenderer wrapper (~35 lines)
│   ├── SeriesDetailContract.kt  — SeriesDetailState, SeriesDetailIntent, SeriesDetailEffect
│   ├── SeriesDetailViewModel.kt — loads series_detail JSON, fetches with seriesId param
│   ├── SeriesDetailScreen.kt    — SDUIRenderer wrapper (~40 lines)
│   └── MoviesNavGraph.kt        — composable(MOVIES) + composable(SERIES_DETAIL/{id})
└── src/main/res/values/strings.xml
    — tv_series_list_* and series_detail_* string resources (resolved via binding system)
```

---

### `:feature:banking`

Multi-step onboarding form. All four steps are fully SDUI-driven — no hardcoded form layouts.

```
feature/banking/
├── src/main/java/.../feature/banking/ui/
│   ├── BankingContract.kt
│   ├── BankingViewModel.kt
│   ├── BankingScreen.kt         — SDUIRenderer wrapper
│   └── BankingNavGraph.kt       — step-to-step navigation
└── src/main/res/values/strings.xml
```

Screens defined as JSON assets:
- `personal_details.json` — name, DOB, gender, mobile, email
- `address_details.json` — street, city, state, ZIP, residence type
- `financial_information.json` — employment, income, loan amount/tenure, existing loans
- `review_submit.json` — read-only summary with edit and submit actions

---

### `:feature:deeplink` and `:feature:analytics`

- **deeplink** — `DeepLinkActivity` + `DeepLinkScreen`: handles URI-based navigation into any graph
- **analytics** — Koin-provided `AnalyticsEngine` wrapping Firebase; fires events from SDUI `analytics` node fields

---

### `:app`

Entry point, navigation host, and SDUI-driven bottom navigation shell.

```
app/
├── SplashActivity.kt             — 1.5s branded splash → MainActivity
├── MainActivity.kt               — deep link handling + setContent { MainScreen }
├── DeepLinkActivity.kt           — URI intent handler → routes to MainActivity
├── MainScreen.kt                 — Scaffold + dynamic NavigationBar from TabBarViewModel
├── ArchitectNavHost.kt           — NavHost(moviesGraph, bankingGraph)
├── IconMapper.kt                 — icon name string → Material ImageVector
├── tab/
│   ├── TabBarConfig.kt           — @Serializable: TabBarConfig, TabItem, ResolvedTab
│   ├── TabConfigLoader.kt        — @Singleton: loads tab_config.json, resolves bindings
│   └── TabBarViewModel.kt        — @HiltViewModel: exposes tabs + startDestination
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## 5. MVI Architecture

Every screen follows the same unidirectional data flow: **Intent → reduce → State / Effect**.

```
┌──────────────────────────────────────────────────────┐
│                     Composable UI                     │
│                                                       │
│   val state by vm.state.collectAsStateWithLifecycle() │
│   LaunchedEffect { vm.effect.collectLatest { ... } }  │
│   onClick { vm.handleIntent(SomeIntent) }             │
└──────────────────┬───────────────▲────────────────────┘
                   │               │
               Intent          State / Effect
                   │               │
┌──────────────────▼───────────────┴────────────────────┐
│                    BaseViewModel                       │
│                                                        │
│   handleIntent(intent)                                 │
│     └─ viewModelScope.launch { reduce(intent) }        │
│           ├─ setState { copy(...) }   → StateFlow       │
│           └─ setEffect(effect)        → Channel        │
└────────────────────────────────────────────────────────┘
```

- **State** — immutable `data class`; `setState` uses `MutableStateFlow.update{}` — thread-safe.
- **Effect** — `Channel<E>` consumed exactly once with `collectLatest` — used for navigation, toast messages, and other one-shot events that must not replay on recomposition.

### Contract example — Series Detail

```kotlin
// What the UI renders
data class SeriesDetailState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: Map<String, String> = emptyMap(),
) : UiState

// What the user can do
sealed interface SeriesDetailIntent : UiIntent {
    data class Load(val seriesId: String) : SeriesDetailIntent
    data object NavigateBack : SeriesDetailIntent
}

// One-shot events handled exactly once
sealed interface SeriesDetailEffect : UiEffect {
    data object GoBack : SeriesDetailEffect
}
```

---

## 6. Server-Driven UI Engine

The SDUI engine translates a JSON file into a live Compose tree. The feature module provides only three inputs: the screen ID, the loading/error state, and an action callback.

### Render Pipeline

```
assets/screens/{screenId}.json
        │   LocalScreenSource.load(screenId)
        ▼
  raw JSON string
        │   Json.decodeFromString<ScreenModel>()
        ▼
  ScreenModel (in-memory node tree)
        │   setState { copy(screenModel = it) }
        ▼
  SDUIRenderer(screenModel, isLoading, error, dataMap, listData, onAction)
        │
        │   ① BindingResolver.resolveAll(dataMap)
        │       api bindings   → look up raw API field by path
        │       string bindings → StringResolver.resolve(key)
        │       template bindings → interpolate {{key}} from combined map
        │   ② enrichedData = dataMap + resolvedBindings
        │
        ▼
  SDUIRenderEngine.Render(screenModel, enrichedData, listData, onAction)
        │
        ▼
  for each ComponentNode → RenderNode(node, enrichedData, ...)
        │
        ├── "scroll"        → Column(verticalScroll)
        ├── "column"        → Column(spacedBy) + background + padding
        ├── "row"           → Row(spacedBy) + optional clickable(action)
        ├── "topBar"        → Row: back icon | title | search icon
        ├── "header"        → Row: title Text + optional subtitle
        ├── "text"          → Text (dataBinding | {{template}} | literal)
        ├── "image"         → Box wrapper → AsyncImage (Coil)
        ├── "icon"          → Icon (mapped by name string)
        ├── "list"          → Column with drag-to-reorder
        ├── "generatedList" → repeat(data[countBinding].toInt()) { i → renderNode(itemLayout) }
        ├── "button"        → Material Button
        ├── "textField"     → OutlinedTextField with validation
        ├── "dateField"     → DatePicker dialog
        ├── "dropdown"      → DropdownMenu with options
        ├── "segmentedControl" → Switch/chip group
        ├── "slider"        → Material Slider
        ├── "stepperField"  → increment/decrement counter
        ├── "currencyField" → formatted currency input
        ├── "toggle"        → Material Switch
        ├── "card"          → elevated container, optional action
        ├── "summaryRow"    → label + value pair for review screens
        └── unknown         → visible placeholder "[unknown: type]"
```

### JSON Screen Definition Structure

```json
{
  "screenId": "series_detail",
  "type": "scroll",

  "bindings": {
    "synopsisTitle":  { "source": "string",   "key": "series_detail.synopsis_title" },
    "ratingText":     { "source": "template",  "template": "{{imdbPrefix}} {{rating}}" },
    "title":          { "source": "api",        "path": "Title" },
    "rating":         { "source": "api",        "path": "imdbRating" }
  },

  "dataSource": {
    "type": "remote",
    "request": {
      "method": "GET",
      "url": "https://www.omdbapi.com/?i={{seriesId}}&apikey=...",
      "timeout": 10
    },
    "response": {
      "type": "object",
      "fieldMapping": {}
    }
  },

  "children": [
    {
      "type": "header",
      "titleTemplate": "{{title}}",
      "subtitleTemplate": "{{year}} • {{genre}}"
    },
    {
      "type": "text",
      "dataBinding": "synopsisTitle",
      "style": { "fontSize": 18, "fontWeight": "bold" }
    },
    {
      "type": "generatedList",
      "countBinding": "totalSeasons",
      "itemLayout": {
        "type": "row",
        "accessibility": { "label": "Season {{seasonNumber}}", "role": "button" },
        "children": [
          { "type": "text", "template": "{{seasonLabel}} {{seasonNumber}}" }
        ]
      }
    }
  ]
}
```

### Visibility Rules

A component declares a conditional guard:

```json
"visibility": { "dataBinding": "awards", "isNotEmpty": true }
```

Evaluated before the node is composed. If `data["awards"]` is empty or `"N/A"`, the node is skipped entirely — no placeholder, no layout space.

### Action Dispatch

```
JSON action on a list item row:
  "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" }

Runtime resolution:
  1. routeTemplate substituted: "series_detail/{{id}}" → "series_detail/tt0903747"
  2. onAction("navigate", { "route": "series_detail/tt0903747" }) called
  3. ViewModel: setEffect(Navigate("series_detail/tt0903747"))
  4. Screen: navController.navigate("series_detail/tt0903747")
```

Supported action types: `navigate`, `back`, `search`, `reorder`.

### Custom Component Registration

Feature modules register custom components via Hilt multibinding without touching the engine:

```kotlin
@Module @InstallIn(SingletonComponent::class)
object MoviesComponentModule {
    @Provides @IntoSet
    fun provideRatingBadge(): SduiComponentProvider = SduiComponentProvider { node, data, _, _ ->
        // key "type" in when block
        if (node.type == "ratingBadge") { RatingBadgeComposable(data["rating"]) }
    }
}
```

Custom components take precedence over all built-ins.

---

## 7. SDUI Component System

### Built-in Component Reference

| Type | Description | Key fields |
|---|---|---|
| `scroll` | Root scrollable container | `children` |
| `column` | Vertical stack | `style.spacing`, `style.padding`, `style.backgroundColor` |
| `row` | Horizontal stack, optional tap | `action`, `style.cornerRadius` |
| `topBar` | App bar: back + title + search | `props.leadingIcon`, `action.type` |
| `header` | Title + subtitle row | `titleTemplate`, `subtitleTemplate`, `titleBinding`, `subtitleBinding` |
| `text` | Single label | `dataBinding`, `template`, `text` |
| `image` | Async image (Coil) | `dataBinding` → URL |
| `icon` | System vector icon | `icon` name string |
| `list` | Data-bound list, drag-to-reorder | `listDataBinding`, `itemLayout` |
| `generatedList` | Count-driven repeated layout | `countBinding`, `itemLayout` |
| `card` | Elevated container | `style.cornerRadius`, `action` |
| `button` | Tappable text button | `titleBinding`, `action` |
| `textField` | Text input with validation | `dataBinding`, `validation.required`, `validation.minLength` |
| `dateField` | Date picker dialog | `dataBinding` |
| `dropdown` | Select from options | `dataBinding`, `options[]` |
| `segmentedControl` | Multi-choice chip group | `dataBinding`, `options[]` |
| `slider` | Range slider | `dataBinding`, `minValue`, `maxValue`, `step` |
| `stepperField` | Inc/dec counter | `dataBinding`, `minValue`, `maxValue` |
| `currencyField` | Formatted currency input | `dataBinding`, `validation.min` |
| `toggle` | Boolean switch | `dataBinding` |
| `summaryRow` | Label + value pair | `label`, `dataBinding` |
| `spacer` | Vertical gap | `style.spacing` |
| `divider` | Horizontal rule | — |

### Style System

| JSON key | Type | Compose mapping |
|---|---|---|
| `backgroundColor` | color token | `Modifier.background(colorFromToken(it), RoundedCornerShape)` |
| `foregroundColor` | color token | text/icon color |
| `cornerRadius` | Float dp | `RoundedCornerShape(it.dp)` |
| `padding` | Float dp | `Modifier.padding(all = it.dp)` |
| `paddingTop / paddingBottom` | Float dp | `Modifier.padding(top/bottom)` |
| `paddingStart / paddingEnd` | Float dp | `Modifier.padding(start/end)` |
| `spacing` | Float dp | `Arrangement.spacedBy(it.dp)` |
| `frameWidth / frameHeight` | Float dp | `Modifier.size(width, height)` |
| `fontSize` | Float sp | `it.sp` |
| `fontWeight` | String | `"bold"` → Bold, `"semibold"` → SemiBold, `"medium"` → Medium |
| `lineLimit` / `maxLines` | Int | `maxLines = it` |
| `weight` | Float | `Modifier.weight(it)` |

---

## 8. Binding & Localization System

The binding system resolves three types of dynamic values into the flat data map before any component renders, ensuring all components work through the standard `data[key]` lookup with zero additional logic.

### Architecture

```
JSON "bindings" block
        │
        ▼
BindingItem(source, key, path, template)
        │
        ▼
BindingResolver.resolveAll(apiData)
   ┌────────────────────────────────────┐
   │  Step 1 — "api" source             │
   │    apiField = item.path ?: item.key │
   │    resolved[bindingKey] = apiData[apiField] ?: "" │
   ├────────────────────────────────────┤
   │  Step 2 — "string" source          │
   │    resolved[bindingKey] =          │
   │      StringResolver.resolve(item.key) │
   ├────────────────────────────────────┤
   │  Step 3 — "template" source        │
   │    base = apiData + resolved        │
   │    resolved[bindingKey] =          │
   │      item.template.replace({{k}},base[k]) │
   └────────────────────────────────────┘
        │
        ▼
enrichedData = apiData + resolved
        │
        ▼
SDUIRenderEngine.Render(enrichedData)
— components use data[key] as always, no extra logic
```

### Binding Source Types

#### `"api"` — raw API field remapping

Maps a raw API response field (using `path`) to a friendly binding key. Used when `fieldMapping` is empty — the binding layer becomes the field mapper.

```json
"bindings": {
  "title":  { "source": "api", "path": "Title" },
  "rating": { "source": "api", "path": "imdbRating" }
}
```

Resolution: `resolved["title"] = apiData["Title"]`

#### `"string"` — localized string resource

Fetches a value from Android string resources. The JSON key uses dot notation; `AndroidStringResolver` converts to underscore notation before calling `getIdentifier`.

```json
"bindings": {
  "synopsisTitle": { "source": "string", "key": "series_detail.synopsis_title" }
}
```

Resolution chain:
```
"series_detail.synopsis_title"
        │  replace('.' → '_')
        ▼
"series_detail_synopsis_title"
        │  context.resources.getIdentifier(key, "string", packageName)
        ▼
 R.string.series_detail_synopsis_title
        │  context.getString(resId)
        ▼
"Synopsis"
```

#### `"template"` — interpolated composite string

Builds a display string by substituting `{{key}}` placeholders from the combined map of API data and previously resolved bindings. Template bindings are always resolved last (step 3) so they can reference both API values and string-resolved values.

```json
"bindings": {
  "imdbPrefix": { "source": "string",   "key": "series_detail.imdb_prefix" },
  "ratingText": { "source": "template", "template": "{{imdbPrefix}} {{rating}}" }
}
```

Resolution:
```
Step 2 → resolved["imdbPrefix"] = "IMDb"
Step 3 → base = apiData + { "imdbPrefix": "IMDb" }
       → "{{imdbPrefix}} {{rating}}" → "IMDb 9.3"
       → resolved["ratingText"] = "IMDb 9.3"
```

### `StringResolver` — Clean Architecture Interface

```
core:network                 engine:sdui
──────────────────────       ──────────────────────────────
StringResolver               AndroidStringResolver
  interface                    implements StringResolver
  fun resolve(key): String     - replaces '.' with '_'
                               - calls context.getIdentifier()
                               - returns key on miss (never throws)

                             StringResolverModule
                               @Provides @Singleton
                               StringResolver → AndroidStringResolver(@ApplicationContext)
```

`StringResolver` in `:core:network` has no Android imports. `AndroidStringResolver` in `:engine:sdui` has the Android-specific implementation. `core` never depends on the Android framework — only on `StringResolver`.

### String Resource Placement

| Scope | Location | Example key |
|---|---|---|
| App-level (tabs) | `app/src/main/res/values/strings.xml` | `tab_movies_title` |
| Movies feature | `feature/movies/src/main/res/values/strings.xml` | `tv_series_list_header_title` |
| Series detail | `feature/movies/src/main/res/values/strings.xml` | `series_detail_synopsis_title` |
| Banking feature | `feature/banking/src/main/res/values/strings.xml` | feature-specific labels |
| Common | `core/ui/src/main/res/values/strings.xml` | `common_back`, `common_continue` |

All are merged into the final APK. `AndroidStringResolver` looks them up by the unified package name — module boundaries are transparent at runtime.

### `BindingResolver` — Backward Compatibility

- Screens **without** a `bindings` block: `resolveAll()` returns an empty map; the existing `dataBinding` / `template` logic in all components is unchanged.
- `resolve(key)` (called by existing `SDUIComponentsDispatcher` for `titleBinding` and button labels) reads from the pre-built cache first, then falls back to live resolution for form-source values.
- All existing component rendering paths are unaffected.

---

## 9. Data Pipeline — End to End

### Series List (cold launch → rendered list)

```
1. MoviesScreen → hiltViewModel() → MoviesViewModel.init
       └── handleIntent(LoadScreen)

2. LocalScreenSource.load("tv_series_list")
       └── assets/screens/tv_series_list.json → ScreenModel
       └── setState(screenModel = model, isLoading = true)
           ▶ SDUIRenderer renders immediately with loading indicator

3. BindingResolver.resolveAll(emptyMap())          ← no API data yet
       string bindings: "headerTitle" → "Series Hub"
       string bindings: "imdbPrefix"  → "IMDb"
       enrichedData = { "headerTitle": "Series Hub", "imdbPrefix": "IMDb" }
           ▶ Header shows "Series Hub" and subtitle immediately

4. DataSourceExecutor.execute(dataSource)
   Phase A: GET https://www.omdbapi.com/?s=game&type=series
       Response: { "Search": [ {imdbID, Title, Year, Poster}, ... ] }
       fieldMapping: imdbID→id, Title→title, Year→year, Poster→posterURL
       Result: List<Map<String,String>> (10 items)

   Phase B: Parallel enrichment (10 concurrent calls)
       GET https://www.omdbapi.com/?i={{id}}&apikey=...
       Merges: rating, genre into each item

5. setState(isLoading = false, listData = { "series": [10 enriched items] })
       ▶ SDUIRenderer re-renders; each row binds from per-item data + enrichedData

6. User taps a row:
       routeTemplate = "series_detail/{{id}}" → "series_detail/tt0903747"
       onAction("navigate", { "route": "series_detail/tt0903747" })
       setEffect(Navigate(...)) → navController.navigate(...)
```

### Series Detail (seriesId from nav argument)

```
1. SeriesDetailScreen(seriesId = "tt0903747")
       └── handleIntent(Load("tt0903747"))

2. LocalScreenSource.load("series_detail")
       └── assets/screens/series_detail.json → ScreenModel
       └── setState(screenModel = model, isLoading = true)

3. DataSourceExecutor.execute(dataSource, params = { "seriesId" → "tt0903747" })
       URL resolved: "https://www.omdbapi.com/?i=tt0903747&apikey=..."
       fieldMapping: {} (empty) → pass-through, raw API field names preserved
       Response fields in apiData: "Title", "imdbRating", "Runtime", "Awards", etc.

4. BindingResolver.resolveAll(apiData)
       api step:      "title"  = apiData["Title"]  = "Game of Thrones"
                      "rating" = apiData["imdbRating"] = "9.3"
       string step:   "imdbPrefix"    = "IMDb"
                      "synopsisTitle" = "Synopsis"
                      "runtimeLabel"  = "Runtime:"
       template step: "ratingText"    = "IMDb 9.3"
                      "runtimeText"   = "Runtime: 57 min"
                      "seasonCountText" = "Seasons: 8"
       enrichedData = apiData + all resolved values

5. setState(isLoading = false, data = enrichedData)
       ▶ SDUIRenderer binds every field via dataBinding/template
       ▶ generatedList reads enrichedData["totalSeasons"].toInt() = 8
         → renders 8 season rows automatically

6. User taps back:
       topBar action.type = "back"
       setEffect(GoBack) → navController.popBackStack()
```

---

## 10. Parallel API Execution & Enrichment

The movies list screen requires two API tiers:

- **Main call** — searches OMDb, returns 10 items with basic fields
- **Enrichment calls** — one detail call per item to fetch rating and genre

`DataSourceExecutor` fires all enrichment calls concurrently:

```kotlin
suspend fun execute(
    dataSource: DataSourceModel,
    params: Map<String, String> = emptyMap(),
): List<Map<String, String>> = coroutineScope {

    val mainItems = fetchAndMap(dataSource, params)

    val enrichment = dataSource.enrichmentDataSource
        ?: return@coroutineScope mainItems

    mainItems.map { item ->
        async {
            val enriched = fetchEnrichment(enrichment, item)
            item + enriched        // merge into single map per item
        }
    }.awaitAll()
}
```

**Performance impact:**

```
Sequential (10 items × ~150ms avg):       ~1 500ms
─────────────────────────────────────────────────────
main ──150ms──┤
enrich[0]     ──150ms──┤
enrich[1]              ──150ms──┤
...
enrich[9]                                 ──150ms──┤


Parallel (this implementation):            ~300ms
─────────────────────────────────────────────────────
main      ──150ms──┤
enrich[0..9]        ──150ms──┤  (all fire simultaneously)
```

---

## 11. URL Template Resolution

Both the main URL and enrichment URLs support `{{key}}` placeholders resolved at execution time:

```
Main URL (resolved from ViewModel params):
  Template:  "https://www.omdbapi.com/?i={{seriesId}}&apikey=..."
  params:    { "seriesId": "tt0903747" }
  Resolved:  "https://www.omdbapi.com/?i=tt0903747&apikey=..."

Enrichment URL (resolved from each item's data):
  Template:  "https://www.omdbapi.com/?i={{id}}&apikey=..."
  item data: { "id": "tt0903747", "title": "Game of Thrones", ... }
  Resolved:  "https://www.omdbapi.com/?i=tt0903747&apikey=..."
```

The same mechanism powers detail screen fetches (ViewModel params) and enrichment fetches (item data). No separate resolution code paths exist.

---

## 12. Data Binding to View

Once data is in `enrichedData`, `SDUIRenderer` passes the flat `Map<String, String>` into the component tree. Each node resolves its value independently:

```
enrichedData = {
  "title":           "Game of Thrones",
  "year":            "2011–2019",
  "genre":           "Action, Adventure, Drama",
  "rating":          "9.3",
  "ratingText":      "IMDb 9.3",          ← template binding
  "runtimeText":     "Runtime: 57 min",   ← template binding
  "seasonCountText": "Seasons: 8",        ← template binding
  "synopsisTitle":   "Synopsis",          ← string binding
  "creditsTitle":    "Credits",           ← string binding
  "plot":            "Nine noble families...",
  "totalSeasons":    "8",
  "posterURL":       "https://...",
  "actors":          "Emilia Clarke...",
  "actorsText":      "Cast: Emilia Clarke..."  ← template binding
}

JSON node                                    Compose output
────────────────────────────────────────────────────────────────────
{ type:"header", titleTemplate:"{{title}}" }
  → Text("Game of Thrones", fontSize=24sp, fontWeight=Bold)

{ type:"text", dataBinding:"ratingText" }
  → Text("IMDb 9.3", fontWeight=SemiBold)

{ type:"text", dataBinding:"runtimeText" }
  → Text("Runtime: 57 min")

{ type:"text", dataBinding:"synopsisTitle", fontWeight:"bold" }
  → Text("Synopsis")

{ type:"image", dataBinding:"posterURL" }
  → AsyncImage(model="https://...", contentScale=Crop)

{ type:"generatedList", countBinding:"totalSeasons" }
  enrichedData["totalSeasons"].toInt() = 8
  → renders 8 rows with { "seasonNumber": "1" } ... { "seasonNumber": "8" }
  → { type:"text", template:"{{seasonLabel}} {{seasonNumber}}" }
  → Text("Season 1") ... Text("Season 8")

{ type:"text", dataBinding:"awardsText",
  visibility:{ dataBinding:"awards", isNotEmpty:true } }
  awards = "Won 59 Primetime Emmys" → rendered
  (if awards = "N/A" → node skipped entirely)
```

There is no mapping code at any layer between the API response and the rendered text. Bindings map API fields once; component nodes reference those binding keys directly.

---

## 13. Dynamic Tab Navigation

The bottom navigation bar is fully driven by `tab_config.json`. No tab data is hardcoded anywhere in the app.

### Configuration File

`core/network/src/main/assets/screens/tab_config.json`:

```json
{
  "type": "tabBar",
  "defaultSelectedTab": "movies",
  "bindings": {
    "moviesTabTitle":  { "source": "string", "key": "tab.movies.title" },
    "bankingTabTitle": { "source": "string", "key": "tab.banking.title" }
  },
  "tabs": [
    {
      "id": "movies",
      "titleBinding": "moviesTabTitle",
      "icon": "film",
      "rootScreenId": "tv_series_list"
    },
    {
      "id": "banking",
      "titleBinding": "bankingTabTitle",
      "icon": "building.columns",
      "rootScreenId": "personal_details"
    }
  ]
}
```

### Resolution Architecture

```
tab_config.json
        │ Json.decodeFromString<TabBarConfig>()
        ▼
TabBarConfig { defaultSelectedTab, bindings, tabs[] }
        │
        ▼
TabConfigLoader (@Singleton)
  ├── lazy { load + parse }           ← file read happens exactly once
  ├── resolveTitle(cfg, titleBinding)
  │     binding.source = "string"
  │     → stringResolver.resolve("tab.movies.title")
  │     → "tab_movies_title" → R.string.tab_movies_title → "Movies"
  ├── resolvedTabs: List<ResolvedTab>
  │     ResolvedTab(id="movies", title="Movies",
  │                 icon="film", graphRoute="movies_graph")
  └── defaultTabRoute: "movies_graph"   ← "${defaultSelectedTab}_graph"
        │
        ▼
TabBarViewModel (@HiltViewModel)
  ├── tabs: List<ResolvedTab>
  └── startDestination: String
        │
        ▼
MainScreen
  ├── hiltViewModel<TabBarViewModel>()
  ├── NavigationBar → tabs.forEach { NavigationBarItem(...) }
  └── ArchitectNavHost(startDestination = viewModel.startDestination)
```

### Graph Route Convention

Tab `id` maps to navigation graph route as `"${id}_graph"`:

| JSON `id` | Derived `graphRoute` | Navigation graph |
|---|---|---|
| `"movies"` | `"movies_graph"` | `moviesGraph(navController)` in `ArchitectNavHost` |
| `"banking"` | `"banking_graph"` | `bankingGraph(navController)` in `ArchitectNavHost` |

**Adding a new tab** requires:
1. Add an entry in `tab_config.json`
2. Add a string resource for the title
3. Register the navigation graph in `ArchitectNavHost`

No changes to `MainScreen`, `TabConfigLoader`, or `TabBarViewModel`.

### Icon Mapping

`IconMapper` maps JSON icon names (SF Symbol–style) to Material3 `ImageVector`:

| JSON `icon` | Material icon | Usage |
|---|---|---|
| `"film"` | `Icons.Default.VideoLibrary` | Movies tab |
| `"building.columns"` | `Icons.Default.AccountBalance` | Banking tab |
| `"movie"` | `Icons.Default.Movie` | Legacy |
| `"account_balance"` | `Icons.Default.AccountBalance` | Legacy |

---

## 14. Features in Detail

### Movies — Series List Screen

**Entry**: `MoviesScreen.kt` — ~35 lines, purely a ViewModel-state-to-`SDUIRenderer` bridge.

**JSON** (`tv_series_list.json`):
- `type: "list"` with `listDataBinding: "series"` — one row per enriched API item
- Row action: `routeTemplate: "series_detail/{{id}}"` — navigates to detail on tap
- Row children: poster image + title + year•type + genre + IMDb rating (from binding)
- Header uses `titleBinding` / `subtitleBinding` — resolved from string resources

**Supported ViewModel actions:**

| Action | Source | Result |
|---|---|---|
| `navigate` | Row tap | `setEffect(Navigate(route))` → `navController.navigate()` |
| `reorder` | Long-press drag end | `reorderList(binding, from, to)` in-memory update |
| `search` | Search icon | reserved for Phase 3 |

---

### Movies — Series Detail Screen

**Entry**: `SeriesDetailScreen.kt` — ~40 lines. `seriesId` arrives as a nav argument.

**JSON** (`series_detail.json`):
- `bindings` block with 7 "api" bindings, 11 "string" bindings, 7 "template" bindings
- Empty `fieldMapping` — API field remapping handled entirely by bindings
- Back button: `topBar` with `action.type: "back"`
- Header: `titleTemplate: "{{title}}"`, `subtitleTemplate: "{{year}} • {{genre}}"`
- Hero card: poster + ratingText + runtimeText + seasonCountText + awardsText (visibility-guarded)
- Synopsis section: `dataBinding: "synopsisTitle"` + `dataBinding: "plot"`
- Credits section: `actorsText`, `writerText`, `directorText`
- Seasons: `generatedList` + `countBinding: "totalSeasons"` → dynamic season rows

---

### Banking — Multi-Step Form

Four SDUI screens forming a guided onboarding flow. All form components use `FormDataStorage` (singleton in `engine:sdui`) to hold cross-step state and run validation.

| Step | JSON screen | Key components |
|---|---|---|
| 1 | `personal_details.json` | `textField`, `dateField`, `segmentedControl`, `dropdown` |
| 2 | `address_details.json` | `textField`, `dropdown` |
| 3 | `financial_information.json` | `dropdown`, `currencyField`, `slider`, `stepperField` |
| 4 | `review_submit.json` | `summaryRow` (read-only), `button` (edit + submit) |

Validation is declarative in JSON:

```json
{
  "type": "textField",
  "dataBinding": "fullName",
  "label": "Full Name",
  "validation": { "required": true, "minLength": 2 }
}
```

The `button` with `titleBinding: "Submit"` triggers `FormDataStorage.validateForm()` before firing the navigation action.

---

### Deep Link Handling

`DeepLinkActivity` receives URI intents, extracts the `lastPathSegment` and query parameters, and routes to `MainActivity` with a `"route"` extra. `MainActivity` navigates to the appropriate graph before handing off to `MainScreen`.

Supported deep link patterns: any route constant defined in `Routes.kt`.

---

## 15. Drag-to-Reorder

The series list supports long-press drag-to-reorder. Implementation lives in `SDUIComponentsDispatcher.RenderList` (or `ListComponent.kt`) because the list is inside a `verticalScroll` Column — `LazyColumn` cannot be nested in a scrollable ancestor.

```
User long-presses a row
  └── detectDragGesturesAfterLongPress
        onDragStart: draggingIndex = index, dragOffsetY = 0f
        onDrag:      dragOffsetY += dragAmount.y
                     graphicsLayer { translationY = dragOffsetY }  ← visual float
                     zIndex = 1f, scaleX/Y = 1.03f                 ← lift effect
        onDragEnd:
          targetIndex = (draggingIndex + dragOffsetY / avgItemHeight)
                          .roundToInt().coerceIn(list.indices)
          onAction("reorder", { binding, from, to })

ViewModel.handleAction("reorder"):
  val list = listData[binding].toMutableList()
  list.add(to, list.removeAt(from))
  setState { copy(listData = listData + (binding to list)) }
```

- Non-dragged items use the same `graphicsLayer { translationY }` approach for visual shift — no layout recomposition.
- `key(itemData["id"])` ensures Compose tracks each item by series ID through reorders.
- **Persistence**: order lives in `MoviesState.listData` (a `StateFlow`). Resets on process kill — intentional design choice.

---

## 16. TalkBack & Accessibility

Both the list and detail screens are fully compatible with Android TalkBack and all accessibility services that consume the Compose semantics tree. No `<uses-permission>` entry is required.

### Design Principle

Accessibility is a **first-class field on every `ComponentNode`**, not a side-channel through `props`. Every component renderer calls a single Modifier extension; the JSON screen definition drives what TalkBack announces.

```
JSON screen definition
  └── ComponentNode.screenAccessibility: AccessibilityModel
        └── Modifier.applyAccessibility(model, data)
              ├── Token resolution: "{{title}} poster" → "Game of Thrones poster"
              ├── clearAndSetSemantics {}   (importantForAccessibility = false)
              ├── semantics(mergeDescendants = true)
              ├── contentDescription = resolvedLabel
              ├── role = Role.Button / Role.Image / heading() / ...
              └── onClick(label = hint)     (TalkBack "double-tap to ...")
```

### `AccessibilityModel` Data Class

```kotlin
@Serializable
data class AccessibilityModel(
    val label: String? = null,                    // overrides TalkBack announcement
    val hint: String? = null,                     // "double-tap to <hint>"
    val role: String? = null,                     // "button"|"image"|"header"|...
    val mergeDescendants: Boolean? = null,         // collapse descendants into one focus stop
    val importantForAccessibility: Boolean? = null // false → removed from a11y tree
)
```

### `Modifier.applyAccessibility`

```kotlin
fun Modifier.applyAccessibility(
    model: AccessibilityModel?,
    data: Map<String, String> = emptyMap(),
): Modifier {
    model ?: return this

    if (model.importantForAccessibility == false)
        return this.clearAndSetSemantics {}

    val resolvedLabel = model.label?.resolveTokens(data)  // {{key}} substitution
    val resolvedHint  = model.hint?.resolveTokens(data)

    return semantics(mergeDescendants = model.mergeDescendants == true) {
        resolvedLabel?.let { contentDescription = it }
        model.role?.let {
            when (it.lowercase()) {
                "button"   -> role = Role.Button
                "image"    -> role = Role.Image
                "checkbox" -> role = Role.Checkbox
                "switch"   -> role = Role.Switch
                "tab"      -> role = Role.Tab
                "header"   -> heading()
            }
        }
        resolvedHint?.let { hint -> onClick(label = hint) { false } }
    }
}
```

### Decision Table

| `AccessibilityModel` state | Compose result |
|---|---|
| `null` | No-op — transparent to semantics tree |
| `importantForAccessibility = false` | `clearAndSetSemantics {}` — node and all descendants removed |
| `mergeDescendants = true` | All descendant semantics collapsed into one TalkBack focus stop |
| `label` present with `{{key}}` tokens | Tokens resolved from current `data` map at render time |
| `role = "header"` | `heading()` — Compose has no `Role.Header` |
| `hint` present | `onClick(label = hint)` — TalkBack: "double-tap to `<hint>`" |

### Image Accessibility Pattern

```kotlin
// Box owns the semantics; AsyncImage is silenced entirely
Box(modifier = sizeMod.semantics {
    contentDescription = resolvedLabel
    role = Role.Image
}) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.matchParentSize().clearAndSetSemantics {},
    )
}
```

Coil 3 adds its own internal semantics modifier. Without `clearAndSetSemantics {}` on `AsyncImage`, two competing semantic nodes exist and TalkBack reads both.

### Duplicate Reading Prevention

Components that sit inside a `mergeDescendants = true` parent and have `importantForAccessibility = false` set:

- `TextComponent` — `.applyAccessibility(node.screenAccessibility, data)` on the Text modifier → `clearAndSetSemantics {}` prevents Text's internal `text` property from duplicating the parent's `contentDescription`
- `ColumnComponent`, `RowComponent`, `HeaderComponent`, `GeneratedListComponent` — same pattern on root modifier

Without this, a season row with `label: "Season 1"` and `mergeDescendants: true` would produce: **"Season 1 Season 1"** (parent's `contentDescription` + child Text's `text` property merged together).

### JSON Authoring Guide

```json
// Image with label containing data token
{
  "type": "image",
  "dataBinding": "posterURL",
  "accessibility": { "label": "{{title}} poster", "role": "image" }
}

// Tappable row — merge all child text into one focus stop
{
  "type": "row",
  "action": { "type": "navigate", "routeTemplate": "series_detail/{{id}}" },
  "accessibility": {
    "mergeDescendants": true,
    "role": "button",
    "hint": "open detail"
  }
}

// Decorative icon — excluded from accessibility tree
{
  "type": "icon",
  "icon": "star.fill",
  "accessibility": { "importantForAccessibility": false }
}

// Section heading — navigable via TalkBack heading shortcut
{
  "type": "text",
  "dataBinding": "synopsisTitle",
  "accessibility": { "role": "header" }
}

// Generated season row with dynamic label
{
  "type": "row",
  "accessibility": {
    "label": "Season {{seasonNumber}}",
    "role": "button",
    "hint": "view season details",
    "mergeDescendants": true
  }
}
```

### TalkBack Focus Traversal — List Screen

```
Swipe-right order:
  1. "Series Hub"                   ← header title (native Text)
  2. "Live OMDB data..."            ← header subtitle (native Text)
  3. "Search" button                ← hardcoded contentDescription
  4–N. Each series item (merged)    ← title + year + genre + rating merged
       "double-tap to open detail"  ← from hint
       drag handle excluded         ← clearAndSetSemantics {}
```

### TalkBack Focus Traversal — Detail Screen

```
Swipe-right order:
  1. "Navigate back" button         ← hardcoded role=Button
  2. "Game of Thrones"              ← header title
  3. "2011–2019 • Action, Drama"    ← header subtitle
  4. Hero card (merged)             ← title + IMDb rating + runtime + seasons
  5. "Won 59 Primetime Emmys"       ← conditional, skipped when empty
  6. "Synopsis" heading             ← role=header, navigable via heading shortcut
  7. <plot text>
  8. "Credits" heading
  9. "Cast: Emilia Clarke..."
  10. "Writer: ..."
  11. "Director: ..."
  12. "Seasons" heading
  13–N. "Season 1" button ... "Season N" button
```

---

## 17. Libraries & Versions

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.0.21 | Language |
| Android Gradle Plugin | 8.x | Build toolchain |
| KSP | 2.0.21-1.0.28 | Annotation processor (Hilt, Room) |
| Jetpack Compose BOM | 2024.09.00 | Compose UI toolkit |
| Material 3 | (via BOM) | Design system |
| Hilt | 2.51.1 | Dependency injection |
| hilt-navigation-compose | — | `hiltViewModel()` in Compose |
| Compose Navigation | 2.8.3 | Type-safe screen navigation |
| OkHttp | 4.12.0 | HTTP engine + logging interceptor |
| kotlinx.serialization | 1.7.3 | JSON parsing, `@Serializable` models |
| Room | 2.6.1 | SQLite for local watchlist |
| Coil 3 | 3.0.4 | Async image loading |
| Koin | 3.5.6 | DI for analytics module (alongside Hilt) |
| Kotlin Coroutines | 1.8.1 | `async`/`awaitAll`, `Flow`, `StateFlow`, `Channel` |
| Material Icons Extended | — | Full Material icon set for SDUI icon mapping |

---

## 18. Project File Tree

```
MoviesDemoAPP/
│
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/example/moviesdemoapp/app/
│       │   ├── App.kt                         — @HiltAndroidApp + Koin init
│       │   ├── MainActivity.kt                — deep link routing + setContent
│       │   ├── SplashActivity.kt              — 1.5s branded splash
│       │   ├── DeepLinkActivity.kt            — URI intent → MainActivity
│       │   ├── MainScreen.kt                  — Scaffold + dynamic NavigationBar
│       │   ├── ArchitectNavHost.kt            — NavHost(startDestination, graphs)
│       │   ├── IconMapper.kt                  — icon name string → ImageVector
│       │   └── tab/
│       │       ├── TabBarConfig.kt            — TabBarConfig, TabItem, ResolvedTab
│       │       ├── TabConfigLoader.kt         — @Singleton JSON loader + binding resolver
│       │       └── TabBarViewModel.kt         — @HiltViewModel: tabs + startDestination
│       ├── res/values/strings.xml             — app_name, tab_movies_title, tab_banking_title
│       └── AndroidManifest.xml
│
├── core/
│   ├── domain/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/.../core/domain/
│   │       ├── BaseViewModel.kt               — abstract MVI base
│   │       ├── UiContract.kt                  — UiState, UiIntent, UiEffect markers
│   │       └── Result.kt                      — Success | Error | Loading
│   │
│   ├── network/
│   │   ├── build.gradle.kts
│   │   ├── src/main/assets/screens/
│   │   │   ├── tab_config.json                — SDUI: bottom nav tab configuration
│   │   │   ├── tv_series_list.json            — SDUI: movies list screen + bindings
│   │   │   ├── series_detail.json             — SDUI: detail screen + bindings
│   │   │   ├── personal_details.json          — SDUI: banking step 1
│   │   │   ├── address_details.json           — SDUI: banking step 2
│   │   │   ├── financial_information.json     — SDUI: banking step 3
│   │   │   └── review_submit.json             — SDUI: banking step 4
│   │   └── src/main/java/.../core/network/
│   │       ├── di/NetworkModule.kt            — Hilt: Json, OkHttpClient
│   │       ├── model/ScreenModel.kt           — ScreenModel, ComponentNode, BindingItem,
│   │       │                                    AccessibilityModel, StyleModel,
│   │       │                                    ActionModel, VisibilityModel
│   │       ├── model/DataSourceModel.kt       — DataSourceModel, RequestModel, ResponseModel
│   │       ├── StringResolver.kt              — interface (no Android dependency)
│   │       ├── ScreenSource.kt                — interface: load(screenId): String?
│   │       ├── NetworkClient.kt               — interface: get(url): String?
│   │       └── OkHttpNetworkClient.kt         — OkHttp implementation
│   │
│   ├── data/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/.../core/data/
│   │       ├── remote/
│   │       │   ├── SDUIDataRepository.kt      — interface: fetch(url): String?
│   │       │   ├── SDUIDataRepositoryImpl.kt  — NetworkClient-backed impl
│   │       │   └── DataSourceExecutor.kt      — main fetch + parallel enrichment
│   │       ├── local/
│   │       │   ├── AppDatabase.kt             — @Database(WatchlistEntity)
│   │       │   ├── WatchlistDao.kt
│   │       │   ├── WatchlistEntity.kt
│   │       │   └── LocalScreenSource.kt       — assets/screens/{id}.json → String
│   │       └── di/DataModule.kt               — @Binds repos + @Provides Room
│   │
│   └── ui/
│       ├── build.gradle.kts
│       └── src/main/
│           ├── java/.../core/ui/
│           │   ├── DesignTokens.kt            — all colors, spacing, typography, radii
│           │   ├── colorFromToken.kt          — token string → Compose Color
│           │   └── MovieAppTheme.kt           — MaterialTheme wrapper
│           └── res/values/strings.xml         — common_back, common_continue, common_currency_symbol
│
├── engine/
│   ├── sdui/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/.../engine/sdui/
│   │       ├── SDUIRenderer.kt                — @Composable entry point + SDUIRenderEngine
│   │       │                                    SduiEntryPoint (Hilt entry point)
│   │       │                                    enrichedData = dataMap + resolveAll()
│   │       ├── SDUIComponentsDispatcher.kt    — routes type → component renderer
│   │       ├── AccessibilityUtils.kt          — Modifier.applyAccessibility(model, data)
│   │       │                                    String.resolveTokens(data)
│   │       ├── TemplateResolver.kt            — {{key}} resolution + isVisible()
│   │       ├── BindingResolver.kt             — resolveAll(): api→string→template pipeline
│   │       │                                    resolve(key): cache + fallback
│   │       ├── AndroidStringResolver.kt       — Context-based StringResolver impl
│   │       │                                    dot→underscore key conversion
│   │       ├── StringResolverModule.kt        — @Provides @Singleton StringResolver
│   │       ├── FormDataStorage.kt             — singleton cross-step form state
│   │       ├── ComponentRegistry.kt           — extensible custom component registry
│   │       ├── SduiComponentProvider.kt       — functional interface for @IntoSet
│   │       └── components/
│   │           ├── TopBarComponent.kt         — back (role=Button) + search icon
│   │           ├── HeaderComponent.kt         — title + subtitle, titleBinding support
│   │           ├── ColumnComponent.kt         — vertical stack + applyAccessibility
│   │           ├── RowComponent.kt            — horizontal stack + applyAccessibility
│   │           ├── CardComponent.kt           — elevated container + applyAccessibility
│   │           ├── ListComponent.kt           — drag-to-reorder + item applyAccessibility
│   │           ├── GeneratedListComponent.kt  — count-driven repeat + applyAccessibility
│   │           ├── TextComponent.kt           — text + applyAccessibility (prevents dup)
│   │           ├── ImageComponent.kt          — Box+AsyncImage pattern (Coil silenced)
│   │           ├── IconComponent.kt           — Icon(contentDescription=null) + applyAccessibility
│   │           ├── ButtonComponent.kt         — Material Button + applyAccessibility
│   │           ├── SpacerComponent.kt         — decorative gap
│   │           └── DividerComponent.kt        — decorative rule
│   │
│   └── navigation/
│       ├── build.gradle.kts
│       └── src/main/java/.../engine/navigation/
│           ├── Routes.kt                      — MOVIES, SERIES_DETAIL, BANKING_* constants
│           ├── NavigationAction.kt            — NavType + destination
│           └── NavigationEngine.kt            — navigate(NavController, NavigationAction)
│
└── feature/
    ├── movies/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── java/.../feature/movies/ui/
    │       │   ├── MoviesContract.kt          — MoviesState, MoviesIntent, MoviesEffect
    │       │   ├── MoviesViewModel.kt         — SDUI loader + enrichment + reorder
    │       │   ├── MoviesScreen.kt            — SDUIRenderer wrapper (~35 lines)
    │       │   ├── SeriesDetailContract.kt    — SeriesDetailState, Intent, Effect
    │       │   ├── SeriesDetailViewModel.kt   — SDUI loader with seriesId param
    │       │   ├── SeriesDetailScreen.kt      — SDUIRenderer wrapper (~40 lines)
    │       │   └── MoviesNavGraph.kt          — composable(MOVIES) + SERIES_DETAIL/{id}
    │       └── res/values/strings.xml
    │           — tv_series_list_header_title, series_detail_synopsis_title, etc.
    │
    ├── banking/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── java/.../feature/banking/ui/
    │       │   ├── BankingContract.kt
    │       │   ├── BankingViewModel.kt
    │       │   ├── BankingScreen.kt           — SDUIRenderer wrapper (multi-step)
    │       │   └── BankingNavGraph.kt
    │       └── res/values/strings.xml
    │
    ├── deeplink/
    │   └── src/main/java/.../feature/deeplink/
    │       └── DeepLinkScreen.kt
    │
    └── analytics/
        └── src/main/java/.../feature/analytics/
            ├── AnalyticsEngine.kt             — Koin-provided Firebase wrapper
            └── di/AnalyticsModule.kt
```

---

*This document reflects the full production state of the MoviesDemoApp codebase.*  
*For questions or contributions, open an issue or pull request on the project repository.*
