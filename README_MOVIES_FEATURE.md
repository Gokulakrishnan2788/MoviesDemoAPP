# Movies Feature

The `feature:movies` module delivers two screens — a TV series list and a series detail view — built entirely on Server-Driven UI (SDUI). No layout code lives in Kotlin; every screen is declared in a JSON file and interpreted at runtime by the SDUI engine.

---

## Module Location

```
feature/movies/
├── build.gradle.kts
└── src/main/java/com/example/moviesdemoapp/feature/movies/
    ├── di/
    │   └── MoviesComponentModule.kt
    └── ui/
        ├── MoviesNavGraph.kt
        ├── list/
        │   ├── MoviesContract.kt
        │   ├── MoviesScreen.kt
        │   └── MoviesViewModel.kt
        └── detail/
            ├── SeriesDetailContract.kt
            ├── SeriesDetailScreen.kt
            └── SeriesDetailViewModel.kt
```

---

## Screens

### 1. Movies List (`MoviesScreen`)

Displays a scrollable list of TV series fetched from the OMDB API. Each card shows:
- Poster image
- Title, year, type
- IMDB rating and genre (fetched via a parallel enrichment call)

Tapping a card navigates to the Series Detail screen.

**Route:** `movies?screenId={screenId}` (default `screenId` = `tv_series_list`)

**Screen definition:** `core/network/src/main/assets/screens/tv_series_list.json`

---

### 2. Series Detail (`SeriesDetailScreen`)

Shows full metadata for a selected series:
- Poster image
- Title, year, genre
- IMDB rating badge
- Synopsis / plot
- Cast, writer, director
- Total seasons

A back button returns to the list.

**Route:** `series_detail/{seriesId}?screenId={screenId}` (default `screenId` = `series_detail`)

**Screen definition:** `core/network/src/main/assets/screens/series_detail.json`

---

## Architecture

This module follows MVI with SDUI rendering.

```
JSON Screen Config
       │
       ▼
ScreenRepository ──► LocalScreenSource (assets) or RemoteScreenSource (CMS)
       │
       ▼
MoviesViewModel / SeriesDetailViewModel
  • loads ScreenModel
  • executes DataSourceModel via DataSourceExecutor
  • emits State
       │
       ▼
MoviesScreen / SeriesDetailScreen (Composable)
  • collects State
  • forwards to SDUIRenderer
       │
       ▼
SDUIRenderEngine
  • walks ComponentNode tree
  • renders built-in or custom components
```

### MVI Contracts

Each screen has a dedicated contract file defining:

| Type | Purpose |
|------|---------|
| `State` | Current UI snapshot (`screenModel`, `isLoading`, `error`, `dataMap`, `listData`) |
| `Intent` | User actions (`LoadScreen`, `OnAction`, `NavigateBack`) |
| `Effect` | One-shot events (`Navigate(route)`, `GoBack`) |

### Data Flow

1. Screen enters composition → ViewModel receives `LoadScreen` / `Load` intent.
2. `ScreenRepository.loadScreen(screenId)` reads the JSON asset and parses it into a `ScreenModel`.
3. The `ScreenModel` contains a `DataSourceModel` that describes the OMDB API call.
4. `DataSourceExecutor.execute(dataSource, params)` fetches the main endpoint and, for list screens, enriches each item in parallel by calling the detail endpoint.
5. Mapped results are stored in `State.listData` (list screen) or `State.dataMap` (detail screen).
6. `SDUIRenderer` reads both the `ScreenModel` and the data maps to produce the Compose UI.

---

## SDUI Screen JSON Schema (key fields)

```json
{
  "screenId": "tv_series_list",
  "type": "scroll",
  "dataSource": {
    "type": "remote",
    "request": { "url": "...", "method": "GET" },
    "response": {
      "root": "Search",
      "fieldMapping": { "imdbID": "id", "Title": "title", "Poster": "posterURL" }
    },
    "enrichmentDataSource": { ... }
  },
  "children": [ { "type": "list", "listDataBinding": "series", ... } ]
}
```

Changing the JSON — locally or via a remote CMS — updates the screen without any Kotlin changes.

---

## Navigation

Routes are registered in `MoviesNavGraph.kt` and wired into the app's root nav graph.

| Destination | Route | Key params |
|---|---|---|
| Series list | `movies?screenId={screenId}` | `screenId` (default `tv_series_list`) |
| Series detail | `series_detail/{seriesId}?screenId={screenId}` | `seriesId`, `screenId` |

Navigation is triggered by SDUI `action` nodes in the JSON (`"type": "navigate"`, `"route": "series_detail/{{id}}"`). The ViewModel emits a `Navigate` effect; the screen's `LaunchedEffect` calls `navController.navigate(route)`.

---

## Dependency Injection

Hilt provides all dependencies to the ViewModels via constructor injection. No feature-level DI setup is required because all repositories are bound in `core:data`'s `DataModule`.

`MoviesComponentModule.kt` contains a commented-out template showing how to register a custom SDUI component (e.g., a `MovieCardComponent`) if the built-in component set needs extending for this feature.

---

## Network Layer

| Item | Value |
|------|-------|
| API | OMDB (`https://www.omdbapi.com/`) |
| List endpoint | `/?s=game&type=series&apikey=...` |
| Detail endpoint | `/?i={imdbID}&apikey=...` |
| HTTP client | OkHttp with body logging |
| Timeout | 10 seconds |
| Enrichment | Parallel `async` calls per list item |

Enrichment merges `imdbRating` and `Genre` from the detail endpoint into each list item so the card can display them without a second user-triggered request.

---

## Switching to a Remote Screen Source

Screens are currently loaded from bundled assets (`LocalScreenSource`). To serve them from a CMS or backend:

1. Implement `RemoteScreenSource.load(screenId)` (the class is already wired up, just needs the HTTP call).
2. In `DataModule`, change the `@Binds` for `ScreenSource` from `LocalScreenSource` to `RemoteScreenSource`.

No feature code changes needed.

---

## Module Dependencies

```
feature:movies
    ├── core:domain
    ├── core:ui
    ├── core:network
    ├── core:data
    ├── engine:sdui
    └── engine:navigation
```

---

## Testing

The module includes test dependencies for:
- **JUnit 4** — unit tests
- **Mockk** — mocking ViewModels and repositories
- **Turbine** — testing `StateFlow` / `SharedFlow` emissions

ViewModels can be tested in isolation by mocking `ScreenRepository` and `DataSourceExecutor` and asserting state transitions against emitted `State` values.

---

---

# Complete Feature Lifecycle — Technical Reference

This section documents the full end-to-end lifecycle of the Movies feature: from the moment the composable enters composition to pixels on screen. Every layer is traceable to the source file that owns it.

---

## Phase 1 — Screen Configuration Loading (JSON Parsing Pipeline)

### 1.1 Entry trigger

When `MoviesScreen` enters the Compose tree it calls `hiltViewModel()` which instantiates `MoviesViewModel`. The `init` block immediately dispatches `MoviesIntent.LoadScreen` via `handleIntent()`.

```
MoviesScreen (Composable)
    └── hiltViewModel<MoviesViewModel>()
            └── init { handleIntent(MoviesIntent.LoadScreen) }
```

`handleIntent` launches a coroutine on `viewModelScope` and calls `reduce(intent)`.

**Source:** `BaseViewModel.kt:37`, `MoviesViewModel.kt:22`

---

### 1.2 ScreenRepository — the JSON gateway

`MoviesViewModel.loadScreen()` calls `ScreenRepository.loadScreen(screenId)`.

```kotlin
// ScreenRepository.kt
suspend fun loadScreen(screenId: String): ScreenModel? = runCatching {
    val raw = source.load(screenId) ?: return@runCatching null
    json.decodeFromString<ScreenModel>(raw)
}.getOrNull()
```

`source` is bound by Hilt to `LocalScreenSource` (default) or `RemoteScreenSource` (CMS). The binding is a single `@Binds` line in `DataModule` — no feature code changes required to switch.

**Source:** `ScreenRepository.kt:21`, `DataModule.kt`

---

### 1.3 LocalScreenSource — asset loading

`LocalScreenSource.load(screenId)` opens the file at `assets/screens/{screenId}.json` from the application's bundled assets and returns it as a raw UTF-8 string.

```kotlin
// LocalScreenSource.kt
override suspend fun load(screenId: String): String? = runCatching {
    context.assets.open("screens/$screenId.json").bufferedReader().readText()
}.getOrNull()
```

For the Movies list, this resolves to `assets/screens/tv_series_list.json`.
For the Detail screen, it resolves to `assets/screens/series_detail.json`.

All I/O failures are swallowed by `runCatching` and return `null`, which the repository propagates as a `null` `ScreenModel`, causing the ViewModel to emit an error state.

**Source:** `LocalScreenSource.kt:18`

---

### 1.4 kotlinx.serialization — JSON-to-object deserialization

The raw JSON string is passed to `kotlinx.serialization`:

```kotlin
json.decodeFromString<ScreenModel>(raw)
```

The deserializer walks the JSON tree and populates the following object graph:

```
ScreenModel
├── screenId: String               — unique screen identifier
├── type: String                   — layout root ("scroll" | "column")
├── bindings: Map<String, BindingItem>?  — static/string/template bindings
├── dataSource: DataSourceModel?   — API configuration for this screen
│   ├── request: RequestModel      — url, method, headers, timeout
│   ├── response: ResponseModel    — root path + fieldMapping
│   └── enrichmentDataSource: DataSourceModel?  — secondary API call per item
└── children: List<ComponentNode>  — recursive UI tree
    └── ComponentNode
        ├── type: String           — "list" | "text" | "image" | "button" | etc.
        ├── dataBinding: String?   — key into the flat data map
        ├── listDataBinding: String? — key into the list data map
        ├── template: String?      — "{{title}} • {{year}}" interpolation string
        ├── action: ActionModel?   — "navigate" | "search" | "back"
        ├── style: StyleModel?     — colors, padding, fontSize, cornerRadius
        ├── visibility: VisibilityModel? — conditional render rule
        ├── accessibility: AccessibilityModel? — TalkBack overrides
        └── children: List<ComponentNode>  — recursive
```

`@SerialName` annotations on `ComponentNode` map JSON snake_case keys (e.g., `dataBinding`, `listDataBinding`) to Kotlin camelCase fields. Unknown JSON keys are ignored — forward-compatibility is built in.

**Source:** `ScreenModel.kt:13`, `DataSourceModel.kt:7`

---

## Phase 2 — Data Fetching and Field Mapping

### 2.1 DataSourceExecutor — orchestrator

After the `ScreenModel` is set in state, `MoviesViewModel` extracts `screenModel.dataSource` and calls:

```kotlin
executeDataSource.execute(dataSource, params)
```

`DataSourceExecutor` runs entirely on the calling coroutine (already on `viewModelScope`). It owns parsing and field mapping only — it has no knowledge of OkHttp, Retrofit, or HTTP internals.

**Source:** `DataSourceExecutor.kt:23`

---

### 2.2 Primary fetch — `fetchAndMap`

```kotlin
// DataSourceExecutor.kt
private suspend fun fetchAndMap(ds: DataSourceModel, params: Map<String, String>): List<Map<String, String>> {
    val url = params.entries.fold(ds.effectiveUrl) { acc, (k, v) -> acc.replace("{{$k}}", v) }
    val rawJson = repository.fetch(url) ?: return emptyList()
    val root = json.parseToJsonElement(rawJson).jsonObject
    return when (ds.response?.type) {
        "collection" -> root[ds.effectiveRoot]?.jsonArray?.mapNotNull { ... mapFields(it, ds.fieldMapping) }
        else         -> listOf(mapFields(root, ds.fieldMapping))
    }
}
```

Steps:
1. **URL template substitution** — `{{seriesId}}` placeholders in the URL are replaced with values from `params` (e.g., for the detail screen, `params["seriesId"]` comes from the nav argument).
2. **HTTP fetch** — `SDUIDataRepository.fetch(url)` delegates to `OkHttpNetworkClient`, which executes the GET request on the IO dispatcher and returns the raw response body string.
3. **JSON parsing** — the response body is parsed to a `JsonObject` using `kotlinx.serialization`.
4. **Response routing** — if `response.type == "collection"`, the engine extracts the JSON array at `response.root` (e.g., `"Search"` for the OMDB list). Otherwise it treats the root object as a single item.
5. **Field mapping** — `mapFields(jsonObject, fieldMapping)` reads each `apiKey` from the JSON and stores it under the corresponding `sduiKey` in the result `Map<String, String>`. Example: `{ "title" to "Game of Thrones", "posterURL" to "https://..." }`.

**Source:** `DataSourceExecutor.kt:48–68`

---

### 2.3 Enrichment fetch — parallel secondary calls

If the `DataSourceModel` declares an `enrichmentDataSource`, the executor fans out one coroutine per primary item using `async` / `awaitAll`:

```kotlin
mainItems.map { item ->
    async {
        val enriched = fetchEnrichment(enrichment, item)
        item + enriched          // merge: primary fields + enrichment fields
    }
}.awaitAll()
```

Inside `fetchEnrichment`, the enrichment URL is resolved with `{{key}}` substitution using each item's own data. For the Movies list:

- Primary: `/?s=game&type=series` → returns `[{ imdbID, Title, Year, Poster }]`
- Enrichment per item: `/?i={imdbID}` → returns `{ imdbRating, Genre, ... }`

The merged result per item is: `{ id, title, year, type, posterURL, rating, genre }`.

All enrichment calls run in parallel — latency is bounded by the slowest single call, not the sum.

**Source:** `DataSourceExecutor.kt:36–43`, `DataSourceExecutor.kt:70–83`

---

### 2.4 ViewModel state update

The executor returns `List<Map<String, String>>`. The ViewModel stores this in state keyed by the SDUI binding name:

```kotlin
// MoviesViewModel.kt
setState { copy(isLoading = false, listData = mapOf("series" to items)) }
```

The key `"series"` matches `listDataBinding: "series"` on the list `ComponentNode` in the JSON — this is how the engine knows which data set to render inside which list component.

For the detail screen, the result is a single-item list; the ViewModel extracts `items.firstOrNull()` and stores it as `dataMap: Map<String, String>` in the state.

**Source:** `MoviesViewModel.kt:44–48`

---

## Phase 3 — Binding Resolution

### 3.1 Two-stage binding architecture

The engine separates bindings into two categories handled by different resolvers:

| Resolver | Responsibility | Input | Output |
|---|---|---|---|
| `BindingResolver` | Screen-level declarative bindings from `ScreenModel.bindings` | `BindingItem` map + API data | Merged `Map<String, String>` |
| `TemplateResolver` | Node-level `{{key}}` interpolation and visibility | Template string + data map | Resolved string / boolean |

---

### 3.2 BindingResolver — screen-level resolution

`BindingResolver` is instantiated once per `SDUIRenderer` composition and pre-resolves all entries in `ScreenModel.bindings` into the flat data map before rendering begins.

Resolution executes in a strict three-pass order to satisfy cross-source dependencies:

**Pass 1 — `source: "api"`**

Reads a raw API value already present in the incoming `apiData` map by `item.path` (preferred) or `item.key` (legacy fallback).

```kotlin
if (item.source == "api") {
    resolved[bindingKey] = apiData[item.path ?: item.key] ?: ""
}
```

Use case: aliasing an API field to a friendlier SDUI key. Example:
```json
"bindings": { "seriesTitle": { "source": "api", "path": "title" } }
```

**Pass 2 — `source: "string"`**

Fetches a localised string resource via `StringResolver`, converting dot-notation keys to Android resource name conventions.

```kotlin
if (item.source == "string") {
    resolved[bindingKey] = stringResolver.resolve(item.key)
}
```

Use case: binding a screen subtitle to a string resource so it respects locale without touching the JSON.

**Pass 3 — `source: "template"`**

Interpolates a `{{key}}` template string against the union of `apiData + pass-1 results + pass-2 results`. This ensures template bindings can reference both API values and string resources.

```kotlin
if (item.source == "template") {
    var result = item.template!!
    baseForTemplate.forEach { (k, v) -> result = result.replace("{{$k}}", v) }
    resolved[bindingKey] = result
}
```

Use case: composing display strings such as `"{{year}} • {{genre}}"` from individual API fields.

The fully resolved map is cached in `resolvedCache`. `resolve(key)` reads from cache first for O(1) lookup; it falls back to live resolution only for `"string"` and `"form"` sources in case `resolveAll` was not called prior to composable render.

**Source:** `BindingResolver.kt:40–71`

---

### 3.3 TemplateResolver — node-level interpolation

`TemplateResolver` operates at the individual `ComponentNode` level during rendering. It has two responsibilities:

**Token interpolation:**
```kotlin
fun resolve(template: String, data: Map<String, String>): String {
    var result = template
    data.forEach { (key, value) -> result = result.replace("{{$key}}", value) }
    return result
}
```

Called by `RenderText`, `RenderTopBar`, and `RenderHeader` for nodes that declare a `template`, `titleTemplate`, or `subtitleTemplate` field. Keys absent from `data` silently resolve to an empty string — the engine never crashes on a missing binding.

**Direct binding lookup:**
```kotlin
fun resolveBinding(key: String?, data: Map<String, String>): String? = key?.let { data[it] }
```

Called for nodes that declare a `dataBinding` key pointing directly to a pre-mapped value in the data map.

**Visibility evaluation:**
```kotlin
fun isVisible(node: ComponentNode, data: Map<String, String>): Boolean {
    val v = node.visibility ?: return true
    val value = v.dataBinding?.let { data[it] } ?: return true
    return if (v.isNotEmpty) value.isNotEmpty() else true
}
```

A `ComponentNode` with `visibility: { dataBinding: "plot", isNotEmpty: true }` is rendered only when `data["plot"]` is a non-empty string. The check runs once per node in `SDUIRenderEngine.RenderNode` before the component is instantiated — invisible nodes consume zero layout resources.

**Source:** `TemplateResolver.kt:18–36`

---

### 3.4 enrichedData — the unified data map

In `SDUIRenderer`, the three data sources are merged into a single `enrichedData` map before the render tree begins:

```kotlin
val enrichedData = remember(screenModel, dataMap) {
    bindingResolver.loadBindings(screenModel?.bindings ?: emptyMap())
    val resolved = bindingResolver.resolveAll(dataMap)
    dataMap + resolved
}
```

Precedence (right side wins on key collision):
```
dataMap (API data from ViewModel)
    + resolved (BindingResolver output: api aliases, string resources, templates)
= enrichedData
```

This single map is passed down through every level of the component tree. No component needs to know where a value originated — it simply reads `data["key"]`.

**Source:** `SDUIRenderer.kt:87–91`

---

## Phase 4 — Rendering Pipeline

### 4.1 SDUIRenderer — composition entry point

`SDUIRenderer` is a `@Composable` function that:
1. Acquires singleton dependencies (`ComponentRegistry`, `StringResolver`) from Hilt via `EntryPointAccessors` — the standard pattern for accessing Hilt singletons from composable scope.
2. Instantiates per-composition objects (`TemplateResolver`, `BindingResolver`, `SDUIComponentsDispatcher`, `SDUIRenderEngine`) wrapped in `remember` so they survive recomposition.
3. Computes `enrichedData` (see Phase 3.4).
4. Renders one of three states: loading indicator, error text, or delegates to `SDUIRenderEngine.Render`.

```kotlin
when {
    isLoading    -> CircularProgressIndicator(...)
    error != null -> Text(error, ...)
    screenModel != null -> engine.Render(screenModel, enrichedData, listData, onAction)
}
```

**Source:** `SDUIRenderer.kt:51–118`

---

### 4.2 SDUIRenderEngine — layout root

`SDUIRenderEngine.Render` reads `screenModel.type` to decide the root layout:

| `screenModel.type` | Root container |
|---|---|
| `"scroll"` | `Column` + `verticalScroll(rememberScrollState())` |
| anything else | `Column` (fixed, no scroll) |

It then iterates `screenModel.children` and calls `RenderNode` for each child.

```kotlin
when (screenModel.type) {
    "scroll" -> Column(modifier = base.verticalScroll(rememberScrollState())) {
        screenModel.children.forEach { RenderNode(screenModel.screenId, it, data, listData, onAction) }
    }
    else -> Column(modifier = base) { ... }
}
```

**Source:** `SDUIRenderer.kt:146–153`

---

### 4.3 RenderNode — the dispatch gate

`RenderNode` is the recursive entry point for every node in the tree. It does two things before delegating:

1. **Visibility gate** — calls `components.isVisible(node, data)`. If the node's `VisibilityModel` evaluates to false, `RenderNode` returns immediately. No composable is emitted.

2. **Custom component registry check** — calls `registry.resolve(node.type)`. If a feature module has registered a custom renderer for this type (via `@IntoSet` Hilt binding), it is invoked first. This is the extension point for feature-specific components.

3. **Built-in dispatch** — if no custom renderer is found, delegates to `SDUIComponentsDispatcher.RenderBuiltIn`.

```kotlin
fun RenderNode(screenName, node, data, listData, onAction) {
    if (!components.isVisible(node, data)) return        // visibility gate
    val custom = registry.resolve(node.type)
    if (custom != null) { custom(node, data, onAction); return }  // custom component
    components.RenderBuiltIn(screenName, node, data, listData, onAction, renderNode = ::RenderNode)
}
```

`renderNode` is passed as a lambda (`NodeRenderer` type alias) so that container components (`column`, `row`, `card`, `list`) can recursively call back into `RenderNode` without coupling to `SDUIRenderEngine` directly.

**Source:** `SDUIRenderer.kt:156–181`

---

### 4.4 SDUIComponentsDispatcher — built-in component router

`RenderBuiltIn` contains a `when` block that maps each `node.type` string to a dedicated composable function:

```kotlin
when (node.type) {
    "topBar"          -> RenderTopBar(node, data, onAction)
    "column"          -> RenderColumn(node, data, listData, onAction, renderNode)
    "row"             -> RenderRow(node, data, listData, onAction, renderNode)
    "card"            -> RenderCard(node, data, listData, onAction, renderNode)
    "text"            -> RenderText(node, data)
    "image"           -> RenderImage(node, data)
    "header"          -> RenderHeader(node, data, onAction)
    "button"          -> RenderButton(node, data, onAction)
    "list"            -> RenderList(node, data, listData, onAction, renderNode)
    "generatedList"   -> RenderGeneratedList(node, data, listData, onAction, renderNode)
    "spacer"          -> RenderSpacer(node)
    "divider"         -> HorizontalDivider(...)
    "textField"       -> RenderEditText(node, data)
    "dateField"       -> RenderDateField(node, data)
    "dropdown"        -> RenderDropDownField(node, data)
    "segmentedControl"-> RenderSwitchField(node, data)
    "slider"          -> RenderSlider(node, data, onAction)
    "stepperField"    -> RenderStepperField(node, data, onAction)
    "currencyField"   -> RenderCurrencyField(node, data, onAction)
    "toggle"          -> RenderToggleField(node, data, onAction)
    "icon"            -> RenderIcon(node)
    "summaryRow"      -> RenderSummeryRow(node, data, listData, onAction, renderNode)
    else -> Box { Text("[unknown: ${node.type}]", color = Accent) }   // never crash
}
```

Unknown types render a visible yellow placeholder — no crash, no silent failure.

**Source:** `SDUIComponentsDispatcher.kt:197–224`

---

## Phase 5 — Data Binding to Views

### 5.1 Text binding strategies

`RenderText` applies a priority chain to resolve the display string:

```kotlin
// Priority: template > dataBinding > text > props["text"]
val text = node.template?.let { resolver.resolve(it, data) }
    ?: node.dataBinding?.let { data[it] }
    ?: node.text
    ?: node.props["text"] ?: ""
```

| Field on `ComponentNode` | Resolved by | Example |
|---|---|---|
| `template` | `TemplateResolver.resolve(template, data)` — replaces all `{{key}}` | `"{{title}} ({{year}})"` → `"Breaking Bad (2008)"` |
| `dataBinding` | Direct `data[key]` lookup | `"title"` → `"Breaking Bad"` |
| `text` | Static string from JSON | `"No results found"` |
| `props["text"]` | Generic props map fallback | Legacy support |

**Source:** `SDUIComponentsDispatcher.kt:1247–1267`

---

### 5.2 Image binding

```kotlin
val url = node.dataBinding?.let { data[it] } ?: node.props["url"] ?: ""
AsyncImage(model = url, contentScale = ContentScale.Crop, modifier = mod)
```

`dataBinding` resolves the image URL from the runtime data map. `AsyncImage` (Coil 3) handles async loading, caching, and placeholder rendering. Width, height, and corner radius are all driven by `StyleModel` fields on the node.

**Source:** `SDUIComponentsDispatcher.kt:1317–1327`

---

### 5.3 List binding — fan-out rendering

`RenderList` is the most complex binding point. It reads the list data by key and renders each item using the node's `itemLayout` as a template:

```kotlin
val binding = node.listDataBinding ?: return           // e.g. "series"
val items   = listData[binding] ?: emptyList()         // List<Map<String,String>>
val layout  = node.itemLayout ?: return                // ComponentNode template

items.forEachIndexed { index, itemData ->
    // Merge item-specific data into the parent data map.
    // Item fields shadow parent fields on collision.
    renderNode(layout, data + itemData, listData, onAction)
}
```

The key insight is `data + itemData`: the item's flat map is merged over the screen-level data map. Each iteration of `renderNode` receives a fully enriched data map scoped to that list item. The `itemLayout` node is recursively rendered — it can contain any built-in component, including nested `column`/`row` containers.

**Drag-to-reorder** is built directly into `RenderList` using `detectDragGesturesAfterLongPress`. On drag-end, it emits `onAction("reorder", mapOf("binding" to binding, "from" to fromIndex, "to" to toIndex))` which the ViewModel intercepts to mutate the list order in state.

Compose identity stability during reorder is maintained via `key(itemData["id"] ?: index)` — this prevents composable state from being misassigned when the list order changes.

**Source:** `SDUIComponentsDispatcher.kt:1494–1608`

---

### 5.4 GeneratedList — count-driven rendering

`RenderGeneratedList` renders a fixed number of identical item layouts driven by a count from the data map:

```kotlin
val count = node.countBinding?.let { data[it]?.toIntOrNull() } ?: 0
(1..count).forEach { i ->
    renderNode(layout, data + mapOf("seasonNumber" to i.toString(), "index" to i.toString()), listData, onAction)
}
```

Used on the detail screen to render one row per season without the server returning a full seasons array.

**Source:** `SDUIComponentsDispatcher.kt:1611–1628`

---

### 5.5 Header binding — multi-source resolution

`RenderHeader` uses all three binding strategies in priority order for both title and subtitle:

```kotlin
var title = node.titleTemplate?.let { resolver.resolve(it, data) }  // 1. template
    ?: node.props["title"]                                           // 2. props
if (title.isEmpty() && !node.titleBinding.isNullOrEmpty()) {
    title = bindingResolver.resolve(node.titleBinding)               // 3. BindingResolver cache
}
```

This allows the header to source its title from: a composed template string (`"{{title}} — {{genre}}"`), a static props value, or a pre-resolved screen-level binding (e.g., a localised string resource).

**Source:** `SDUIComponentsDispatcher.kt:1275–1312`

---

### 5.6 Action binding and dispatch

Actions are declared on `ComponentNode.action: ActionModel`. The `ActionModel.dispatch` extension function resolves any `{{key}}` in the route template and invokes the `onAction` callback:

```kotlin
// ActionModel.dispatch extension
val resolvedRoute = routeTemplate?.let { tpl ->
    var r = tpl
    data.forEach { (k, v) -> r = r.replace("{{$k}}", v) }
    r
} ?: route

val params = buildMap {
    resolvedRoute?.let { put("route", it) }
    putAll(this@dispatch.params)
    destination?.let { put("route", it) }
}
onAction(type, params)
```

Example: a card action `routeTemplate: "series_detail/{{id}}"` with `data["id"] = "tt0944947"` resolves to `"series_detail/tt0944947"` and emits `onAction("navigate", mapOf("route" to "series_detail/tt0944947"))`.

The ViewModel receives this via `MoviesIntent.OnAction("navigate", params)` and emits `MoviesEffect.Navigate(route)`. The screen's `LaunchedEffect` consumes the effect and calls `navController.navigate(route)`.

**Source:** `SDUIComponentsDispatcher.kt:1637–1655`

---

### 5.7 Style binding

Every component reads styling from `node.style: StyleModel?` using safe defaults:

```kotlin
val bg      = node.style?.backgroundColor?.let { colorFromToken(it) }
val pad     = node.style?.padding?.dp ?: 0.dp
val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
val radius  = node.style?.cornerRadius?.dp ?: DesignTokens.RadiusMd
val fontSize= node.style?.fontSize?.sp ?: DesignTokens.TextMd
```

`colorFromToken(string)` converts JSON color values (hex strings or design-token keys) to Compose `Color` instances. `DesignTokens` provides system-wide defaults, so a node without styling picks up the app's design system automatically.

**Source:** `SDUIComponentsDispatcher.kt:633–645`

---

### 5.8 Adaptive layout

`SDUIComponentsDispatcher.rememberAdaptiveConfig()` reads `LocalDensity` and `LocalConfiguration` to produce an `AdaptiveConfig` (font scale, tablet flag, landscape flag). Container components can use `AdaptiveLayout` to switch between row and column arrangements based on device class or accessibility settings — all driven by JSON-declared structure, no manual breakpoints in feature code.

**Source:** `SDUIComponentsDispatcher.kt:130–182`

---

## Phase 6 — Action Handling and Navigation

### 6.1 Action types

| Action type | ViewModel handler | Outcome |
|---|---|---|
| `"navigate"` | `setEffect(MoviesEffect.Navigate(params["route"]))` | NavController navigates to route |
| `"back"` | `setEffect(SeriesDetailEffect.GoBack)` | NavController pops back stack |
| `"search"` | Reserved — Phase 3 implementation | No-op |
| `"reorder"` | `reorderList(binding, from, to)` | Mutates `listData` in state |

### 6.2 Navigation flow

```
User taps tappable component
    └── ActionModel.dispatch(data, onAction)
            └── onAction("navigate", { "route": "series_detail/tt0944947" })
                    └── viewModel.handleIntent(MoviesIntent.OnAction("navigate", params))
                            └── setEffect(MoviesEffect.Navigate("series_detail/tt0944947"))
                                    └── LaunchedEffect in MoviesScreen collects effect
                                            └── navController.navigate("series_detail/tt0944947")
                                                    └── MoviesNavGraph routes to SeriesDetailScreen
                                                            └── SeriesDetailViewModel init { handleIntent(Load) }
```

The `seriesId` is extracted from the nav back-stack entry by `SavedStateHandle` in `SeriesDetailViewModel`, then passed as a param to `DataSourceExecutor.execute(dataSource, mapOf("seriesId" to seriesId))`.

**Source:** `MoviesViewModel.kt:54–56`, `MoviesScreen.kt:19–25`, `MoviesNavGraph.kt`

---

## Phase 7 — State Lifecycle and Recomposition

### 7.1 StateFlow-driven recomposition

`BaseViewModel` exposes state as `StateFlow<S>`. `MoviesScreen` collects it with `collectAsStateWithLifecycle()`, which:
- Stops collecting when the lifecycle is below `STARTED` (Activity is backgrounded) — no wasted CPU.
- Resumes collecting on `STARTED` — no missed updates on foreground return.

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()
```

Every call to `setState { ... }` in the ViewModel atomically updates `MutableStateFlow` via `update { it.block() }`, which emits a new state object and triggers a recomposition of `MoviesScreen`.

**Source:** `BaseViewModel.kt:26–46`

---

### 7.2 Effect channel

One-shot effects (navigation, toasts) use a `Channel<E>(BUFFERED)` exposed as `Flow<E>` via `receiveAsFlow()`. The buffered channel ensures effects are not dropped if the collector is momentarily paused. `collectLatest` in the screen's `LaunchedEffect` processes effects sequentially.

```kotlin
val _effect = Channel<E>(Channel.BUFFERED)
val effect: Flow<E> = _effect.receiveAsFlow()
```

**Source:** `BaseViewModel.kt:31–34`

---

### 7.3 remember and recomposition stability

In `SDUIRenderer`, all engine objects are wrapped in `remember` to survive recomposition without reinstantiation:

```kotlin
val resolver        = remember { TemplateResolver() }
val analyticsEngine = remember { GlobalContext.get().get<AnalyticsEngine>() }
val bindingResolver = remember { BindingResolver(stringResolver) }
val components      = remember { SDUIComponentsDispatcher(resolver, analyticsEngine, bindingResolver, context) }
val engine          = remember(registry) { SDUIRenderEngine(registry, components) }
```

`engine` uses `remember(registry)` — it is only reinstantiated if `registry` itself changes (which only happens at app start). `enrichedData` uses `remember(screenModel, dataMap)` — it recomputes only when the screen definition or API data changes, not on every recomposition triggered by scroll or focus events.

**Source:** `SDUIRenderer.kt:63–91`

---

## Complete End-to-End Lifecycle Summary

The following numbered sequence traces one user session from app launch to a rendered detail screen.

```
 1. App launches → Hilt graph built → singletons created
    (ScreenRepository, DataSourceExecutor, SDUIComponentsDispatcher, ComponentRegistry)

 2. NavController routes to "movies?screenId=tv_series_list"
    → MoviesNavGraph.kt creates MoviesScreen composable

 3. hiltViewModel() creates MoviesViewModel
    → SavedStateHandle["screenId"] = "tv_series_list"
    → init { handleIntent(LoadScreen) }

 4. BaseViewModel.handleIntent launches coroutine on viewModelScope
    → reduce(LoadScreen) → loadScreen()

 5. ScreenRepository.loadScreen("tv_series_list")
    → LocalScreenSource.load("tv_series_list")
    → opens assets/screens/tv_series_list.json
    → returns raw JSON string

 6. json.decodeFromString<ScreenModel>(raw)
    → ScreenModel { screenId, type="scroll", dataSource, bindings, children[] }

 7. setState { copy(screenModel = screenModel, isLoading = true) }
    → MoviesScreen recomposes → SDUIRenderer shows CircularProgressIndicator

 8. DataSourceExecutor.execute(dataSource)
    → fetchAndMap: GET https://omdbapi.com/?s=game&type=series&apikey=...
    → OkHttpNetworkClient executes on IO dispatcher
    → response body: { "Search": [ { "imdbID", "Title", "Year", "Poster" }, ... ] }
    → mapFields: [ { "id", "title", "year", "posterURL" }, ... ]

 9. Enrichment fan-out (parallel async per item):
    → GET https://omdbapi.com/?i={imdbID}&apikey=... for each item
    → mapFields: { "rating", "genre" }
    → item merged: { id, title, year, type, posterURL, rating, genre }

10. setState { copy(isLoading = false, listData = mapOf("series" to items)) }
    → MoviesScreen recomposes

11. SDUIRenderer receives (screenModel, isLoading=false, error=null, dataMap={}, listData)
    → BindingResolver.loadBindings(screenModel.bindings)
    → BindingResolver.resolveAll(dataMap): pass1→api, pass2→string, pass3→template
    → enrichedData = dataMap + resolved

12. SDUIRenderEngine.Render(screenModel, enrichedData, listData, onAction)
    → screenModel.type = "scroll" → Column + verticalScroll
    → iterates children: [topBar, header, list]

13. RenderNode(topBar)
    → isVisible → true
    → no custom component in registry
    → SDUIComponentsDispatcher.RenderBuiltIn → RenderTopBar
    → title resolved via titleTemplate + enrichedData
    → Column { Row { Text(title), Text(subtitle) } }

14. RenderNode(list)
    → SDUIComponentsDispatcher.RenderList
    → listData["series"] → 10 items
    → itemLayout = ComponentNode{ type="card", children=[image, column[text, text, row[icon,text]]] }
    → forEachIndexed item:
        data + itemData = { ..., title="Game of Thrones", posterURL="...", rating="9.3", ... }
        → RenderNode(card) → RenderCard
            → RenderNode(image) → RenderImage → AsyncImage(url=data["posterURL"])
            → RenderNode(column) → RenderColumn
                → RenderNode(text) → RenderText → Text("Game of Thrones")
                → RenderNode(text) → template "{{year}} • {{type}}" → Text("2011 • series")
                → RenderNode(row) → RenderRow
                    → RenderNode(icon) → RenderIcon(star)
                    → RenderNode(text) → Text("9.3")
        → card.action = { type="navigate", routeTemplate="series_detail/{{id}}" }

15. User taps card
    → ActionModel.dispatch(data, onAction)
    → resolvedRoute = "series_detail/tt0944947"
    → onAction("navigate", { "route": "series_detail/tt0944947" })
    → MoviesViewModel.handleIntent(OnAction("navigate", params))
    → setEffect(MoviesEffect.Navigate("series_detail/tt0944947"))
    → LaunchedEffect in MoviesScreen → navController.navigate("series_detail/tt0944947")

16. NavGraph creates SeriesDetailScreen
    → SeriesDetailViewModel: screenId="series_detail", seriesId="tt0944947"
    → Repeat steps 5–14 for series_detail.json
    → DataSourceExecutor substitutes {{seriesId}} = "tt0944947" in URL
    → Single-item response mapped to dataMap
    → SDUIRenderEngine renders topBar + image + header + text sections
```

---

## Architectural Invariants

The following constraints are enforced by the design and must not be violated when extending the feature:

| Invariant | Rationale |
|---|---|
| No layout code in `feature:movies` Kotlin files | All layout is owned by the JSON screen config; feature code only wires ViewModel → Screen |
| `DataSourceExecutor` owns only parsing and mapping | HTTP transport belongs to `NetworkClient`; no OkHttp imports in the executor |
| Visibility is evaluated exactly once per node in `RenderNode` | Custom and built-in components share the same visibility contract; no component can bypass it |
| `enrichedData` is computed once per screen load, not per recomposition | Prevents redundant `BindingResolver.resolveAll` calls on scroll or focus recompositions |
| Effects use `Channel.BUFFERED`, not `Channel.CONFLATED` | Navigation events must never be dropped even if the collector is briefly paused |
| `remember(registry)` on `SDUIRenderEngine` | Registry is a singleton; engine must not be re-created on ordinary recompositions |
| `key(itemData["id"])` in `RenderList` | Compose composable identity must track item identity, not position, to prevent state misassignment during reorder |
