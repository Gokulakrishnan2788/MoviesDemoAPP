# MovieApp — Claude Code Workflow
# Pin this. Reference every session.
# Package: com.example.moviesdemoapp

## ─── PHASE 0: Project Setup (run once) ───────────────────────────────────

# Create new Android project in Android Studio with:
# Package: com.example.moviesdemoapp
# Min SDK: 24
# Language: Kotlin
# Template: Empty Activity

# Then run setup prompt:
cat prompts/CONTEXT.md prompts/ARCHITECTURE.md prompts/DECISIONS.md \
    prompts/contract/naming.md prompts/contract/constraints.md | claude

# Expected output:
# - settings.gradle.kts with all 8 modules included
# - libs.versions.toml with all dependencies
# - build.gradle.kts per module
# - :core:domain with BaseViewModel, UiState, UiIntent, UiEffect, Result
# - :core:ui with MovieAppTheme, DesignTokens, base components
# - :core:network with NetworkModule, ScreenAssetLoader, OmdbApiService skeleton
# - :core:data with AppDatabase, WatchlistEntity, WatchlistDao, DataModule
# - :engine:sdui with SDUIParser, ScreenModel, ComponentNode, ComponentRegistry, SDUIRenderer, TemplateResolver
# - :engine:navigation with NavigationEngine, NavigationAction, Routes
# - :app with SplashActivity, MainActivity, ArchitectNavHost, MainScreen (2 tabs)

./gradlew build
git add . && git commit -m "chore: project setup — all modules scaffolded"


## ─── PHASE 1: Core + Engine (run once) ────────────────────────────────────

# Step 1 — Core domain models + BaseViewModel
cat prompts/CONTEXT.md prompts/contract/mvi.md | claude
./gradlew :core:domain:compileDebugKotlin

# Step 2 — SDUI Engine (parser + registry + renderer)
cat prompts/CONTEXT.md prompts/contract/sdui_contract.md \
    prompts/contract/sdui_engine.md | claude
./gradlew :engine:sdui:compileDebugKotlin

# Step 3 — Navigation engine
cat prompts/CONTEXT.md prompts/contract/navigation.md | claude
./gradlew :engine:navigation:compileDebugKotlin

# Step 4 — Network module (Retrofit + OMDb + ScreenAssetLoader)
cat prompts/CONTEXT.md prompts/contract/api_contract.md | claude
./gradlew :core:network:compileDebugKotlin

# Step 5 — Database (Room)
cat prompts/CONTEXT.md prompts/contract/db_contract.md | claude
./gradlew :core:data:compileDebugKotlin

# Step 6 — App shell (SplashActivity + MainActivity + BottomNav)
cat prompts/CONTEXT.md prompts/contract/navigation.md | claude
./gradlew :app:compileDebugKotlin

git add . && git commit -m "feat: core + engine + app shell ✅"


## ─── PHASE 2: Movies Feature ──────────────────────────────────────────────

# Step 1 — API layer (DTOs + Retrofit service + mapper + repository impl)
cat prompts/CONTEXT.md prompts/contract/api_contract.md \
    prompts/features/movies/api.md | claude
./gradlew :feature:movies:compileDebugKotlin

# Step 2 — Domain layer (models + repository interface + use cases)
cat prompts/CONTEXT.md prompts/contract/mvi.md \
    prompts/features/movies/domain.md | claude
./gradlew :feature:movies:compileDebugKotlin

# Step 3 — UI layer (ViewModels + Screens + State/Intent/Effect)
# Screenshots are in assets/screenshots/ — Claude Code will read them
cat prompts/CONTEXT.md prompts/contract/mvi.md \
    prompts/contract/sdui_contract.md \
    prompts/contract/sdui_engine.md \
    prompts/features/movies/ui.md | claude
git diff
./gradlew :feature:movies:compileDebugKotlin

# Step 4 — Tests
cat prompts/CONTEXT.md prompts/contract/testing.md \
    prompts/features/movies/test.md | claude
./gradlew :feature:movies:testDebugUnitTest

git add . && git commit -m "feat: movies feature — list + detail SDUI + MVI + tests ✅"


## ─── PHASE 3: Banking Feature ─────────────────────────────────────────────

# Single step — full banking placeholder (SDUI + MVI + mock JSON)
cat prompts/CONTEXT.md prompts/contract/mvi.md \
    prompts/contract/sdui_contract.md \
    prompts/contract/sdui_engine.md \
    prompts/features/banking/feature.md | claude
git diff
./gradlew :feature:banking:compileDebugKotlin

git add . && git commit -m "feat: banking tab — SDUI placeholder ✅"


## ─── PHASE 4: Wire Navigation ─────────────────────────────────────────────

cat prompts/CONTEXT.md prompts/contract/navigation.md | claude

# Verify:
# - SplashScreen → MainScreen (replace, back cleared)
# - BottomNav: Movies tab + Banking tab
# - Movies tab: MoviesScreen → SeriesDetailScreen (PUSH with seriesId param)
# - Back from detail → Movies list (POP)

./gradlew :app:compileDebugKotlin
./gradlew assembleDebug
git add . && git commit -m "feat: full navigation wired ✅"


## ─── PHASE 5: Full Build + Smoke Test ────────────────────────────────────

./gradlew assembleDebug
./gradlew test

# Install and manually verify:
# 1. Splash screen shows → transitions to MainScreen
# 2. Movies tab loads → list screen renders from tv_series_list.json SDUI
# 3. Live OMDb data appears (series titles, posters, ratings)
# 4. Tap any series → navigates to detail screen
# 5. Detail screen loads → series_detail.json SDUI rendered with live data
# 6. Season rows generated correctly (Season 1, Season 2, ...)
# 7. Back button returns to list
# 8. Banking tab → SDUI placeholder renders correctly
# 9. Bottom nav switches correctly between tabs

git add . && git commit -m "feat: MovieApp v1.0 complete ✅"


## ─── ERROR RECOVERY ───────────────────────────────────────────────────────

# Kotlin compile error:
cat prompts/CONTEXT.md prompts/fixes/error_fix.md | claude
# (paste error in error_fix.md first)

# Gradle build error:
cat prompts/CONTEXT.md prompts/fixes/build_fix.md | claude
# (paste error in build_fix.md first)

# SDUI component not rendering:
cat prompts/CONTEXT.md prompts/contract/sdui_engine.md \
    prompts/contract/sdui_contract.md | claude
# Describe: "component type X is not rendering, here is the JSON..."


## ─── ADDING A NEW SDUI COMPONENT ─────────────────────────────────────────

cat prompts/CONTEXT.md prompts/contract/sdui_engine.md \
    prompts/contract/sdui_contract.md | claude
# Then describe: "Add a new component type called X with these props: ..."
# Steps Claude will follow:
# 1. Add type to ComponentRegistry when branch
# 2. Create composable in engine/sdui/components/
# 3. Add StyleModel fields if new style props needed
# 4. Update sdui_contract.md component types table


## ─── ADDING A NEW FEATURE (Template — follow for every future feature) ───
#
# This is the EXACT checklist to follow when adding any new feature to MovieApp.
# Example features you might add later:
#   - Watchlist (save series to local Room DB)
#   - Movies tab (separate from TV series — films only)
#   - Search (search bar + results screen)
#   - Notifications tab
#   - User Profile tab
#
# ── STEP 1: Create prompt files ──────────────────────────────────────────
#
# Create a new folder: prompts/features/<feature_name>/
# Inside it, create these files (copy from an existing feature as template):
#
#   api.md       → Define DTOs, Retrofit endpoints, mapper, repository impl
#   domain.md    → Define domain models, repository interface, use cases
#   ui.md        → Define State/Intent/Effect, ViewModel flow, Screen composable
#   test.md      → List all test cases for ViewModel and UseCases
#
# Each file must follow the same pattern used in prompts/features/movies/
# Keep each file focused on ONE layer only (api OR domain OR ui OR test)
#
# ── STEP 2: Create the SDUI JSON ─────────────────────────────────────────
#
# Create the screen JSON file: prompts/features/<feature_name>/<feature>_screen.json
# Follow the SDUI schema in prompts/contract/sdui_contract.md exactly:
#   - Define screenId, dataSource (if live API), type, children
#   - Use color tokens: screenBackground, cardBackground, surface, primaryText, etc.
#   - Use {{templateKey}} for all dynamic values
#   - Copy to: assets/screens/<feature>_screen.json in :core:network module
#
# ── STEP 3: Add the module to settings.gradle.kts ────────────────────────
#
# In settings.gradle.kts, add:
#   include(":feature:<feature_name>")
#
# Create the module folder:
#   feature/<feature_name>/
#     ├── build.gradle.kts          ← copy from feature/movies/build.gradle.kts
#     └── src/main/java/com/example/moviesdemoapp/feature/<feature_name>/
#           ├── data/
#           │   ├── remote/         ← DTOs, ApiService, mapper
#           │   └── repository/     ← RepositoryImpl
#           ├── domain/
#           │   ├── model/          ← Domain models
#           │   ├── repository/     ← Repository interface
#           │   └── usecase/        ← UseCases
#           ├── ui/
#           │   ├── XState.kt       ← State + Intent + Effect
#           │   ├── XViewModel.kt
#           │   └── XScreen.kt
#           └── di/
#               └── XModule.kt      ← Hilt bindings
#
# ── STEP 4: Add route to Routes.kt ───────────────────────────────────────
#
# In :engine:navigation, add to Routes object:
#   const val FEATURE_NAME = "feature_name"
#   const val FEATURE_DETAIL = "feature_name_detail/{id}"   ← if detail screen needed
#   fun featureDetail(id: String) = "feature_name_detail/$id"
#
# ── STEP 5: Register NavGraph in :app ────────────────────────────────────
#
# In :feature:<feature_name>, create NavGraph extension:
#   fun NavGraphBuilder.featureNameGraph(navController: NavController) {
#       composable(Routes.FEATURE_NAME) { FeatureNameScreen(navController) }
#   }
#
# In ArchitectNavHost.kt (:app), add:
#   featureNameGraph(navController)
#
# ── STEP 6: Add bottom nav tab (if it is a top-level tab) ────────────────
#
# In MainScreen.kt (:app), add to bottomNavItems list:
#   BottomNavItem(
#       route = Routes.FEATURE_NAME,
#       label = "Feature",
#       icon = Icons.Default.YourIcon
#   )
#
# Also update CONTEXT.md module map to include the new module.
#
# ── STEP 7: Run the prompts (one layer per session) ───────────────────────
#
# Session A — API layer:
cat prompts/CONTEXT.md prompts/contract/api_contract.md \
    prompts/features/<feature_name>/api.md | claude
./gradlew :feature:<feature_name>:compileDebugKotlin
#
# Session B — Domain layer:
cat prompts/CONTEXT.md prompts/contract/mvi.md \
    prompts/features/<feature_name>/domain.md | claude
./gradlew :feature:<feature_name>:compileDebugKotlin
#
# Session C — UI layer:
cat prompts/CONTEXT.md prompts/contract/mvi.md \
    prompts/contract/sdui_contract.md \
    prompts/contract/sdui_engine.md \
    prompts/features/<feature_name>/ui.md | claude
git diff
./gradlew :feature:<feature_name>:compileDebugKotlin
#
# Session D — Tests:
cat prompts/CONTEXT.md prompts/contract/testing.md \
    prompts/features/<feature_name>/test.md | claude
./gradlew :feature:<feature_name>:testDebugUnitTest
#
# Session E — Wire navigation:
cat prompts/CONTEXT.md prompts/contract/navigation.md | claude
./gradlew :app:compileDebugKotlin
#
git add . && git commit -m "feat: <feature_name> — SDUI + MVI + tests ✅"
#
# ── EXAMPLE: Adding a Watchlist feature ──────────────────────────────────
#
# 1. Create prompts/features/watchlist/api.md
#       → WatchlistRepository reads from Room (no remote API)
#       → WatchlistRepositoryImpl uses WatchlistDao from :core:data
#
# 2. Create prompts/features/watchlist/domain.md
#       → AddToWatchlistUseCase, RemoveFromWatchlistUseCase, GetWatchlistUseCase
#       → Domain model: WatchlistItem (imdbID, title, posterUrl, rating)
#
# 3. Create prompts/features/watchlist/ui.md
#       → WatchlistState(screenModel, isLoading, listData["watchlist"])
#       → WatchlistIntent: LoadScreen, RemoveItem(imdbID)
#       → WatchlistEffect: ShowToast("Removed from watchlist")
#       → SDUI JSON: watchlist_screen.json (list of saved series)
#
# 4. Create prompts/features/watchlist/test.md
#       → Test: given_addItem_when_inserted_then_appearsInList
#       → Test: given_removeItem_when_deleted_then_removedFromList
#
# 5. Add to settings.gradle.kts: include(":feature:watchlist")
# 6. Add Routes.WATCHLIST = "watchlist"
# 7. Add tab to MainScreen BottomNav (bookmark icon)
# 8. Run sessions A → E as above
#
# ── EXAMPLE: Adding a Search feature ─────────────────────────────────────
#
# 1. prompts/features/search/api.md
#       → SearchApiService: GET /?s={query}&type=series
#       → SearchRepositoryImpl calls OmdbApiService.searchSeries(query)
#
# 2. prompts/features/search/domain.md
#       → SearchSeriesUseCase(query: String): Result<List<Map<String,String>>>
#
# 3. prompts/features/search/ui.md
#       → SearchState(query, isLoading, listData["results"])
#       → SearchIntent: QueryChanged(text), Search, ClearQuery, SeriesTapped(imdbID)
#       → SearchEffect: NavigateToDetail(seriesId)
#       → SDUI JSON: search_screen.json (search bar + results list)
#
# 4. Add Routes.SEARCH = "search" and wire in ArchitectNavHost
# 5. Run sessions A → E


## ─── QUICK REFERENCE ─────────────────────────────────────────────────────

# Always load:            CONTEXT.md
# For new feature UI:     + contract/mvi.md + contract/sdui_contract.md
#                           + contract/sdui_engine.md + features/X/ui.md
# For API work:           + contract/api_contract.md + features/X/api.md
# For DB work:            + contract/db_contract.md
# For domain/use cases:   + contract/mvi.md + features/X/domain.md
# For tests:              + contract/testing.md + features/X/test.md
# For navigation:         + contract/navigation.md
# For SDUI engine:        + contract/sdui_engine.md + contract/sdui_contract.md
# For errors:             + fixes/error_fix.md  (paste error in file first)
# For build errors:       + fixes/build_fix.md  (paste gradle output in file first)


## ─── TOKEN SAVING RULES ──────────────────────────────────────────────────
# 1. CONTEXT.md must stay under 50 lines — trim if it grows
# 2. One layer per session (api OR domain OR ui — never all at once)
# 3. Never paste full source files into claude — paste only error messages
# 4. Commit after each layer so git diff stays small and focused
# 5. Use ./gradlew :module:task (not root ./gradlew) to save build time
# 6. Use Haiku model for error fixes, Sonnet for generation
# 7. Phase 1 (core + engine) must always complete before Phase 2 and 3
# 8. Always run git diff before ./gradlew build to review changes


## ─── MODULE STRUCTURE REFERENCE ──────────────────────────────────────────

# settings.gradle.kts must include:
# include(":app")
# include(":core:domain")
# include(":core:ui")
# include(":core:network")
# include(":core:data")
# include(":engine:sdui")
# include(":engine:navigation")
# include(":feature:movies")
# include(":feature:banking")

# Asset files to place in :core:network/src/main/assets/
# assets/screens/tv_series_list.json   ← copy from prompts/features/movies/
# assets/screens/series_detail.json    ← copy from prompts/features/movies/
# assets/screens/banking_home.json     ← defined in features/banking/feature.md

# Screenshot references (copy to :app/src/main/assets/screenshots/)
# screenshots/movies_list.png          ← Series Hub list screen
# screenshots/series_detail.png        ← Series detail screen
