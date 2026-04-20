# Testing Contract
# Every ViewModel and UseCase must be tested

## Libraries
- JUnit 4 — test framework
- MockK — Kotlin-first mocking
- Turbine — Flow/StateFlow testing
- kotlinx-coroutines-test — TestCoroutineDispatcher

## ViewModel Test Pattern
@OptIn(ExperimentalCoroutinesApi::class)
class XViewModelTest {

    @get:Rule val coroutineRule = MainDispatcherRule()

    private val mockUseCase: XUseCase = mockk()
    private lateinit var viewModel: XViewModel

    @Before
    fun setup() {
        viewModel = XViewModel(mockUseCase)
    }

    @Test
    fun given_loadScreen_when_useCaseSucceeds_then_stateHasData() = runTest {
        // Arrange
        coEvery { mockUseCase() } returns Result.Success(mockData)

        // Act
        viewModel.handleIntent(XIntent.LoadScreen)

        // Assert
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.screenModel).isNotNull()
        }
    }

    @Test
    fun given_loadScreen_when_useCaseFails_then_stateHasError() = runTest {
        coEvery { mockUseCase() } returns Result.Error(message = "Network error")

        viewModel.handleIntent(XIntent.LoadScreen)

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.error).isEqualTo("Network error")
        }
    }
}

## UseCase Test Pattern
class XUseCaseTest {
    private val mockRepo: XRepository = mockk()
    private val useCase = XUseCase(mockRepo)

    @Test
    fun given_validParams_when_invoked_then_returnsSuccess() = runTest {
        coEvery { mockRepo.fetch(any()) } returns Result.Success(mockData)
        val result = useCase("param")
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }
}

## MainDispatcherRule
class MainDispatcherRule(
    val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
        dispatcher.cleanupTestCoroutines()
    }
}

## Test naming convention
given_{precondition}_when_{action}_then_{expectedResult}
