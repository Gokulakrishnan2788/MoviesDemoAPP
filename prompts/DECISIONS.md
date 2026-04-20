# Architecture Decision Records (ADR)

## ADR-001: Kotlinx Serialization over Gson/Moshi
Status: Accepted
Reason: Compile-time safety, Kotlin-first, works with sealed classes for SDUI polymorphism
Consequence: All data classes need @Serializable annotation

## ADR-002: StateFlow over LiveData
Status: Accepted
Reason: Kotlin-native, lifecycle-aware with collectAsStateWithLifecycle, composable-friendly
Consequence: ViewModels expose StateFlow<State> not MutableLiveData

## ADR-003: Hilt over Koin
Status: Accepted
Reason: Compile-time validation, official Google support, better multi-module support
Consequence: All modules need @InstallIn annotations, app needs @HiltAndroidApp

## ADR-004: Component Registry Pattern for SDUI
Status: Accepted
Reason: Open/Closed principle — add new components without touching renderer
Consequence: Each component type must be registered in ComponentRegistry at startup

## ADR-005: Effect Channel for one-shot events
Status: Accepted
Reason: Navigation, toasts, dialogs must fire once — StateFlow replays on recomposition
Consequence: Use Channel<Effect>(BUFFERED) + receiveAsFlow() in every ViewModel

## ADR-006: Feature modules cannot depend on each other
Status: Accepted
Reason: Prevent circular dependencies, enforce clean boundaries
Consequence: Shared models go in :core:domain, shared UI in :core:ui

## ADR-007: Live OMDb API for Movies via dataSource in SDUI JSON
Status: Accepted
Reason: SDUI JSON contains dataSource block — ViewModel reads it and fires Retrofit call
Consequence: ScreenModel drives both UI structure AND data fetching URL dynamically

## ADR-008: Template Binding for dynamic data
Status: Accepted
Reason: {{key}} placeholders in JSON props allow server to define layout without knowing data
Consequence: SDUIRenderer must resolve all {{key}} tokens before rendering each component

## ADR-009: Room for local persistence (watchlist / session)
Status: Accepted
Reason: Offline-first, testable, Kotlin-native with coroutines support
Consequence: All entities need @Entity, DAOs need @Dao, AppDatabase as singleton via Hilt

## ADR-010: MockInterceptor for Banking tab
Status: Accepted
Reason: Banking screens are SDUI-driven with mocked JSON — no real banking backend
Consequence: OkHttp MockInterceptor reads from assets/mock/ for banking API calls only
