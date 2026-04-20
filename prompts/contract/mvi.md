# MVI Contract
# Use this EXACT pattern for every feature. Zero deviation.

## BaseViewModel (in :core:domain — DO NOT regenerate)
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {
    abstract fun initialState(): S
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<S> = _state.asStateFlow()
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()
    fun handleIntent(intent: I) { viewModelScope.launch { reduce(intent) } }
    protected abstract suspend fun reduce(intent: I)
    protected fun setState(block: S.() -> S) { _state.update { it.block() } }
    protected fun setEffect(effect: E) { viewModelScope.launch { _effect.send(effect) } }
}

## Marker interfaces (in :core:domain — DO NOT regenerate)
interface UiState
interface UiIntent
interface UiEffect

## Result wrapper (in :core:domain — DO NOT regenerate)
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int? = null, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

## Per-Feature Pattern

### XState.kt (State + Intent + Effect in ONE file)
data class XState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val screenModel: ScreenModel? = null,
    // feature-specific fields
) : UiState

sealed class XIntent : UiIntent {
    object LoadScreen : XIntent()
    data class HandleAction(val actionId: String, val params: Map<String,String> = emptyMap()) : XIntent()
    // feature-specific intents
}

sealed class XEffect : UiEffect {
    data class Navigate(val action: NavigationAction) : XEffect()
    data class ShowToast(val message: String) : XEffect()
}

### XViewModel.kt
@HiltViewModel
class XViewModel @Inject constructor(
    private val useCase: XUseCase
) : BaseViewModel<XState, XIntent, XEffect>() {

    override fun initialState() = XState()

    override suspend fun reduce(intent: XIntent) {
        when (intent) {
            is XIntent.LoadScreen -> loadScreen()
            is XIntent.HandleAction -> handleAction(intent.actionId, intent.params)
        }
    }
}

### XScreen.kt
@Composable
fun XScreen(navController: NavController, viewModel: XViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(XIntent.LoadScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is XEffect.Navigate -> NavigationEngine.navigate(navController, effect.action)
                is XEffect.ShowToast -> { /* show toast */ }
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        onAction = { actionId, params -> viewModel.handleIntent(XIntent.HandleAction(actionId, params)) }
    )
}
