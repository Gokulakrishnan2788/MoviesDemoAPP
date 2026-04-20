# SDUI Engine Contract
# Files live in :engine:sdui — do not modify after initial generation

## Package: com.example.moviesdemoapp.engine.sdui

## SDUIParser
object SDUIParser {
    fun parse(json: String): ScreenModel
    // Uses Kotlinx Serialization with ignoreUnknownKeys = true
    // Returns ScreenModel domain object
}

## ScreenModel (domain model — in :core:domain)
data class ScreenModel(
    val screenId: String,
    val dataSource: DataSourceModel? = null,
    val type: String,                        // "scroll", "column", "lazyColumn"
    val children: List<ComponentNode>
)

data class DataSourceModel(
    val type: String,
    val method: String,
    val url: String,
    val responseRoot: String? = null,
    val enrichmentDataSource: DataSourceModel? = null
)

data class ComponentNode(
    val id: String,
    val type: String,
    val style: StyleModel? = null,
    val children: List<ComponentNode> = emptyList(),
    val dataBinding: String? = null,
    val template: String? = null,
    val titleTemplate: String? = null,
    val subtitleTemplate: String? = null,
    val text: String? = null,
    val icon: String? = null,
    val listDataBinding: String? = null,
    val countBinding: String? = null,
    val itemLayout: ComponentNode? = null,
    val action: String? = null,
    val visibility: VisibilityModel? = null
)

data class StyleModel(
    val padding: Int? = null,
    val spacing: Int? = null,
    val backgroundColor: String? = null,
    val foregroundColor: String? = null,
    val cornerRadius: Int? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val lineLimit: Int? = null,
    val frameWidth: Int? = null,
    val frameHeight: Int? = null
)

data class VisibilityModel(
    val dataBinding: String,
    val isNotEmpty: Boolean = false
)

## ComponentRegistry
object ComponentRegistry {
    fun render(
        node: ComponentNode,
        dataMap: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit
    ): @Composable () -> Unit
    // when (node.type) { "column" -> ..., "row" -> ..., "text" -> ..., etc. }
    // Unknown type → render red placeholder Text("Unknown: ${node.type}")
}

## SDUIRenderer (main composable)
@Composable
fun SDUIRenderer(
    screenModel: ScreenModel?,
    isLoading: Boolean,
    error: String?,
    dataMap: Map<String, String> = emptyMap(),
    listData: Map<String, List<Map<String, String>>> = emptyMap(),
    onAction: (actionId: String, params: Map<String, String>) -> Unit
)
// - Shows CircularProgressIndicator when isLoading
// - Shows error Text when error != null
// - Wraps children in ScrollableColumn / LazyColumn / Column based on screenModel.type
// - Calls ComponentRegistry.render() for each child

## Template Resolver (utility)
object TemplateResolver {
    fun resolve(template: String, dataMap: Map<String, String>): String
    // Replaces all {{key}} occurrences with dataMap[key] ?: ""
    fun isVisible(node: ComponentNode, dataMap: Map<String, String>): Boolean
    // Checks visibility.dataBinding and visibility.isNotEmpty
}

## NavigationAction (in :engine:navigation)
data class NavigationAction(
    val type: NavType,           // PUSH, REPLACE, POP, DEEP_LINK
    val destination: String,
    val params: Map<String, String> = emptyMap()
)
enum class NavType { PUSH, REPLACE, POP, DEEP_LINK }

## Routes (in :engine:navigation)
object Routes {
    const val SPLASH          = "splash"
    const val MAIN            = "main"
    const val MOVIES          = "movies"
    const val SERIES_DETAIL   = "series_detail/{seriesId}"
    const val BANKING         = "banking"

    fun seriesDetail(seriesId: String) = "series_detail/$seriesId"
}
