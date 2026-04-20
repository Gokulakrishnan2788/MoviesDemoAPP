# Global Constraints
# These rules apply to EVERY file generated in this project

## Code Constraints
- No lateinit var — use by lazy or constructor injection
- No !! operator — use safe calls and Elvis operator
- No runBlocking in production code — use viewModelScope or suspend
- No hardcoded strings — use string resources or design tokens
- No hardcoded colors — DesignTokens only
- No hardcoded dimensions — spacing tokens only
- No static/companion object mutable state — inject via Hilt
- No God classes — single responsibility per file
- Max function length: 30 lines (extract if longer)
- Max file length: 200 lines (split if longer)

## Compose Constraints
- No side effects in composable body — use LaunchedEffect/SideEffect
- No ViewModel creation in composable — always inject via hiltViewModel()
- No direct state mutation — only through Intent
- Stateless composables where possible — hoist state to ViewModel
- Every composable must have a @Preview

## Architecture Constraints
- Repository interface in :core:domain, impl in :feature:* or :core:data
- UseCase = one public operator fun invoke() only
- ViewModel never imports Android framework (except ViewModel base class)
- No business logic in Composables — only UI rendering and intent emission
- DataSource block in ScreenModel is read ONLY by ViewModel — never by renderer

## SDUI Constraints
- SDUIRenderer only renders — it never fetches data
- All {{template}} binding resolved before passing to composable
- Unknown component types must render a visible placeholder (not crash)
- generatedList items use 1-based index (Season 1, Season 2...)
- list items receive a flat Map<String, String> per item for binding

## Testing Constraints
- Every ViewModel must have a corresponding ViewModelTest
- Every UseCase must have a corresponding UseCaseTest
- Mock all dependencies — no real network/DB in unit tests
- Test naming: given_when_then pattern
- Use MockK for mocking, Turbine for Flow testing
