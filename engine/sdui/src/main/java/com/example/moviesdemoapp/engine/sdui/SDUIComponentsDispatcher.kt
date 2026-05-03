package com.example.moviesdemoapp.engine.sdui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.example.analytics.engine.AnalyticsEngine
import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.event.Provider
import com.example.moviesdemoapp.core.network.model.ActionModel
import com.example.moviesdemoapp.core.network.model.Analytics
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.components.NodeRenderer
import com.example.moviesdemoapp.engine.sdui.model.AdaptiveConfig
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt


/**
 * Routes each SDUI node type to its dedicated component file in the components/ folder.
 */
@Singleton
class SDUIComponentsDispatcher @Inject constructor(private val resolver: TemplateResolver,
                                                   private val analyticsEngine: AnalyticsEngine,
                                                   private val  bindingResolver :BindingResolver,
                                                   private val context: Context) {


    private val sduiViewModel = SDUIViewModel(context)
    /** Delegates to [TemplateResolver.isVisible] — called by [SDUIRenderEngine.RenderNode]. */
    fun isVisible(node: ComponentNode, data: Map<String, String>): Boolean =
        resolver.isVisible(node, data)
    private var activityScreenName:String? = null
    private val formDataStoreAndValidation = FormDataStorage.formDataStoreAndValidation
    fun readAndSetValue(screenName: String?, key: String?) : String {
        val inMemoryValue = FormDataStorage.readAndSetValue(screenName, key)
        if (inMemoryValue.isNotEmpty()) return inMemoryValue
        
        return key?.let { sduiViewModel.getFieldData(it) }?.replace("$$","$") ?: ""
    }
    private fun validateForm(screenName: String?) = FormDataStorage.validateForm(screenName, sduiViewModel)

    @SuppressLint("ConfigurationScreenWidthHeight")
    @Composable
    fun rememberAdaptiveConfig(): AdaptiveConfig {

        val density = LocalDensity.current
        val configuration = LocalConfiguration.current

        val fontScale = density.fontScale
        val isTablet = configuration.screenWidthDp >= 600
        val isLandscape =
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        return AdaptiveConfig(
            fontScale = fontScale,
            isLargeFont = fontScale > 1.3f,
            isTablet = isTablet,
            isLandscape = isLandscape
        )
    }

    @Composable
    fun AdaptiveLayout(
        config: AdaptiveConfig,
        rowContent: @Composable () -> Unit,
        columnContent: @Composable () -> Unit
    ) {

        when {
            config.isLargeFont -> {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    columnContent()
                }
            }

            config.isTablet || config.isLandscape -> {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowContent()
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    columnContent()
                }
            }
        }
    }

    @Composable
    fun RenderBuiltIn(
        screenName:String?,
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        this.activityScreenName = screenName
        // Visibility check
        if (!resolver.isVisible(node, data)) return

        // Wrap the 4-param onAction into a 3-param one for NodeRenderer
        val wrappedOnAction: (String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit = { type, params, action, isBackClick ->
            onAction(screenName ?: "", type, params, action, isBackClick)
        }

        when (node.type) {
            "topBar"         -> RenderTopBar(node, data, onAction)
            "column"         -> RenderColumn(node, data, listData, onAction, renderNode)
            "row"            -> RenderRow(node, data, listData, onAction, renderNode)
            "summaryRow"     -> RenderSummeryRow(node, data, listData, onAction, renderNode)
            "card"           -> RenderCard(node, data, listData, onAction, renderNode)
            "spacer"         -> RenderSpacer(node)
            "divider"        -> HorizontalDivider(color = DesignTokens.Surface, thickness = 1.dp)
            "text"           -> RenderText(node, data)
            "textField"      -> RenderEditText(node, data)
            "dateField"      -> RenderDateField(node, data)
            "dropdown"       -> RenderDropDownField(node, data)
            "segmentedControl"-> RenderSwitchField(node, data)
            "header"         -> RenderHeader(node, data, onAction)
            "image"          -> RenderImage(node, data)
            "icon"           -> RenderIcon(node)
            "button"         -> RenderButton(node, data, onAction)
            "slider"         -> RenderSlider(node, data, onAction)
            "stepperField"   -> RenderStepperField(node, data, onAction)
            "currencyField"  -> RenderCurrencyField(node, data, onAction)
            "toggle"         -> RenderToggleField(node, data, onAction)
            "list"           -> RenderList(node, data, listData, wrappedOnAction, renderNode)
            "generatedList"  -> RenderGeneratedList(node, data, listData, wrappedOnAction, renderNode)
            else -> Box(Modifier.padding(DesignTokens.SpacingMd)) {
                Text("[unknown: ${node.type}]", color = DesignTokens.Accent)
            }
        }
    }


    @Composable
    private fun RenderCurrencyField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
    ){
        val bindingKey = component.dataBinding ?: ""
        
        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val rawValue = formDataStoreAndValidation[bindingKey]?.replace("$", "") ?: ""
        val formattedValue = formatCurrency(rawValue)
        val isRequired = component.validation?.required == true
        val min = component.validation?.min as? Int ?: 0
        val numericValue = rawValue.toLongOrNull() ?: 0

        val isError = when {
            isRequired && rawValue.isEmpty() -> true
            numericValue < min -> true
            else -> false
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription =
                        (component.screenAccessibility?.label ?: component.label).toString()
                }
        ) {
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = formattedValue,
                onValueChange = { input ->
                    val clean = input.replace("[^0-9]".toRegex(), "")
                    formDataStoreAndValidation[bindingKey] = clean
                    sduiViewModel.saveFieldData(bindingKey, clean)
                    activityScreenName?.let { screenName ->
                        FormDataStorage.updateFormData(screenName, bindingKey, clean)
                    }
                },
                placeholder = { Text(component.placeholder ?: "") },
                leadingIcon = { Text(component.currencySymbol ?: "") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )

            if (isError) {
                val errorText = when {
                    isRequired && rawValue.isEmpty() -> "This field is required"
                    numericValue < min -> "Minimum value is $min"
                    else -> ""
                }
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    fun formatCurrency(value: String): String {
        return try {
            val number = value.replace(",", "").toLong()
            "%,d".format(number)
        } catch (e: Exception) {
            value
        }
    }

    @Composable
    private fun RenderToggleField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
    ){
        val bindingKey = component.dataBinding ?: ""
        
        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val checked = formDataStoreAndValidation[bindingKey]?.toBoolean() ?: false

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription =
                        (component.screenAccessibility?.label ?: component.label).toString()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = component.label ?: "",
                fontSize = 16.sp
            )

            Switch(
                checked = checked,
                onCheckedChange = {
                    formDataStoreAndValidation[bindingKey] = it.toString()
                    sduiViewModel.saveFieldData(bindingKey, it.toString())
                    activityScreenName?.let { screenName ->
                        FormDataStorage.updateFormData(screenName, bindingKey, it.toString())
                    }
                }
            )
        }
    }

    @Composable
    private fun RenderStepperField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
    ){
        val bindingKey = component.dataBinding ?: ""
        val min = component.minValue ?: 1
        val max = component.maxValue ?: 1
        val step = component.step ?: 1

        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue.ifEmpty { min.toString() }
            }
        }

        val value = formDataStoreAndValidation[bindingKey]?.toIntOrNull() ?: min
        val height = (component.style?.height as? Int ?: 40).dp

        fun formatValue(v: Int): String {
            return component.displayTemplate
                ?.replace("{{value}}", v.toString())
                ?: v.toString()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription =
                        (component.screenAccessibility?.label ?: component.label).toString()
                }
        ) {
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (value - step >= min) {
                            val newValue = value - step
                            formDataStoreAndValidation[bindingKey] = newValue.toString()
                            sduiViewModel.saveFieldData(bindingKey, newValue.toString())
                            activityScreenName?.let { screenName ->
                                FormDataStorage.updateFormData(screenName, bindingKey, newValue.toString())
                            }
                        }
                    }
                ) {
                    Text("-", fontSize = 20.sp)
                }

                Text(
                    text = formatValue(value),
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = {
                        if (value + step <= max) {
                            val newValue = value + step
                            formDataStoreAndValidation[bindingKey] = newValue.toString()
                            sduiViewModel.saveFieldData(bindingKey, newValue.toString())
                            activityScreenName?.let { screenName ->
                                FormDataStorage.updateFormData(screenName, bindingKey, newValue.toString())
                            }
                        }
                    }
                ) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }
    }

    @Composable
    private fun RenderSlider(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
    ){
        val bindingKey = component.dataBinding ?: ""
        val min = component.minValue?.toFloat() ?: 0.0f
        val max = component.maxValue?.toFloat() ?: 0.0f
        val step = component.step?.toFloat() ?: 0.0f

        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue.ifEmpty { min.toString() }
            }
        }

        val sliderValue = formDataStoreAndValidation[bindingKey]?.toFloatOrNull() ?: min
        val steps = if (step > 0) ((max - min) / step).toInt() - 1 else 0

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription =
                        (component.screenAccessibility?.label ?: component.label).toString()
                }
        ) {
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "₹${sliderValue.toInt()}",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(
                value = sliderValue,
                onValueChange = {
                    formDataStoreAndValidation[bindingKey] = it.toString()
                    sduiViewModel.saveFieldData(bindingKey, it.toString())
                    activityScreenName?.let { screenName ->
                        FormDataStorage.updateFormData(screenName, bindingKey, it.toString())
                    }
                },
                valueRange = min..max,
                steps = steps
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("₹${min.toInt()}", fontSize = 12.sp)
                Text("₹${max.toInt()}", fontSize = 12.sp)
            }
        }
    }

    // ─── TopBar ───────────────────────────────────────────────────────────────

    @Composable
    private fun RenderTopBar(
        node: ComponentNode,
        data: Map<String, String>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
    ) {
        val title = node.props["title"]
            ?: node.titleTemplate?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) } ?: ""
        val subtitle = node.props["subtitle"]
            ?: node.subtitleTemplate?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) }
        val hasBack = node.props["leadingIcon"] == "back"
        val hasSearch = node.props["trailingIcon"] == "search"
        val padH = node.style?.padding?.dp ?: DesignTokens.SpacingMd
        val padTop = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
        val padBottom = node.style?.paddingBottom?.dp ?: DesignTokens.SpacingSm

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padH, end = padH, top = padTop, bottom = padBottom),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (hasBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { node.action?.dispatch(activityScreenName, data, onAction, node) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DesignTokens.PrimaryText,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                Text(
                    text = title,
                    color = DesignTokens.PrimaryText,
                    fontSize = DesignTokens.TextXxl,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(4f),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (hasSearch) {
                        IconButton(onClick = { node.action?.dispatch(activityScreenName, data, onAction, node) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = DesignTokens.PrimaryText)
                        }
                    }
                }
            }
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = DesignTokens.SecondaryText,
                    fontSize = DesignTokens.TextMd,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignTokens.SpacingXs),
                )
            }
        }
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    @Composable
    private fun RenderColumn(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: 0.dp
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        Column(
            modifier = mod,
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            val internalOnAction: (String, Map<String, String>, ActionModel?, Boolean) -> Unit = { type, params, action, isBackClickAction ->
                onAction(activityScreenName ?: "", type, params, action, isBackClickAction)
            }
            node.children.forEach { renderNode(it, data, listData, internalOnAction) }
        }
    }


    @Composable
    private fun RenderSummeryRow(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        if (node.action != null) mod = mod.clickable { node.action?.dispatch(activityScreenName, data, onAction, node) }
        var value = node.valueTemplate?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) } ?: ""
        if(value.startsWith("$$")) value = value.replace("$$","$")
        Row(
            modifier = mod,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // 🏷️ Label (Left)
            Text(
                text = node.label ?: "",
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = Color.Gray
            )

            // 📊 Value (Right)
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.End
            )
        }
    }

    @Composable
    private fun RenderRow(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        if (node.action != null) mod = mod.clickable { node.action?.dispatch(activityScreenName, data, onAction, node) }
        Row(modifier = mod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            val internalOnAction: (String, Map<String, String>, ActionModel?, Boolean) -> Unit = { type, params, action, isBackAction ->
                onAction(activityScreenName ?: "", type, params, action, isBackAction)
            }
            node.children.forEach {
                if (it.type.equals("button", ignoreCase = true) && it.style?.weight != null) {
                    RenderButton(
                        component =  it, data=  data, onAction =  onAction, modifier = Modifier.weight(
                            it.style?.weight!!
                        )
                    )
                } else {
                    renderNode(it, data, listData, internalOnAction)
                }
            }
        }
    }

    @Composable
    private fun RenderCard(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) } ?: DesignTokens.CardBackground
        val pad = node.style?.padding?.dp?.takeIf { it > 0.dp } ?: DesignTokens.SpacingMd
        val radius = node.style?.cornerRadius?.dp ?: DesignTokens.RadiusMd
        var mod = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm)
        if (node.action != null) mod = mod.clickable { node.action?.dispatch(activityScreenName, data, onAction, node) }
        Card(
            modifier = mod,
            shape = RoundedCornerShape(radius),
            colors = CardDefaults.cardColors(containerColor = bg),
        ) {
            Column(modifier = Modifier.padding(pad)) {
                val internalOnAction: (String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit = { type, params, action, isBackClicked ->
                    onAction(activityScreenName ?: "", type, params, action, isBackClicked)
                }
                node.children.forEach {
                    renderNode(it, data, listData, internalOnAction)
                }
            }
        }
    }

    @Composable
    private fun RenderSpacer(node: ComponentNode) {
        val h = node.props["height"]?.toFloatOrNull() ?: node.style?.spacing ?: 8f
        Spacer(modifier = Modifier.height(h.dp))
    }

    @Composable
    private fun RenderSwitchField(component: ComponentNode, data: Map<String, String>) {
        val bindingKey = component.dataBinding ?: ""
        
        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val selectedValue = formDataStoreAndValidation[bindingKey] ?: ""
        val cornerRadius = (component.style?.cornerRadius?.toInt() ?: 8).dp
        val fontSize = component.style?.fontSize?.sp ?: DesignTokens.TextMd

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .padding(4.dp)
            ) {
                component.options?.forEach { option ->
                    val isSelected = option.value == selectedValue
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(if (isSelected) Color(0xFF4CAF50) else Color.Transparent)
                            .clickable {
                                formDataStoreAndValidation[bindingKey] = option.value
                                sduiViewModel.saveFieldData(bindingKey, option.value)
                                activityScreenName?.let { screenName ->
                                    FormDataStorage.updateFormData(screenName, bindingKey, option.value)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.titleBinding,
                            fontSize = fontSize,
                            color = Color.White
                        )
                    }
                }
            }

            val isError = component.validation?.required == true && selectedValue.isEmpty()
            if (isError) {
                Text(
                    text = "This field is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun RenderDropDownField(component: ComponentNode, data: Map<String, String>) {
        val bindingKey = component.dataBinding ?: ""
        var expanded by remember { mutableStateOf(false) }

        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val selectedValue = formDataStoreAndValidation[bindingKey] ?: ""
        val selectedTitle: String = component.options
            ?.find { it.value == selectedValue }
            ?.titleBinding ?: component.placeholder ?: ""

        val fontSize = component.style?.fontSize?.sp ?: DesignTokens.TextMd
        val cornerRadius = component.style?.cornerRadius?.dp ?: 0.dp
        val isError = component.validation?.required == true && selectedValue.isEmpty()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = component.screenAccessibility?.label ?: ""
                }
        ) {
            Text(
                text = component.label ?: "",
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedTitle,
                    onValueChange = {},
                    readOnly = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                    textStyle = TextStyle(fontSize = fontSize),
                    shape = RoundedCornerShape(cornerRadius),
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expanded = !expanded }
                        )
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    component.options?.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.titleBinding, fontSize = fontSize) },
                            onClick = {
                                formDataStoreAndValidation[bindingKey] = option.value
                                sduiViewModel.saveFieldData(bindingKey, option.value)
                                activityScreenName?.let { screenName ->
                                    FormDataStorage.updateFormData(screenName, bindingKey, option.value)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (isError) {
                Text(
                    text = "This field is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun RenderDateField(node: ComponentNode, data: Map<String, String>) {
        val bindingKey = node.dataBinding ?: ""
        
        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val text = node.label ?: ""
        val placeholder = node.placeholder ?: ""
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        
        val value = formDataStoreAndValidation[bindingKey] ?: ""

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    modifier = Modifier.weight(1f)
                )
                var isClicked by remember { mutableStateOf(false) }
                IconButton(onClick = { isClicked = !isClicked }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "calendar", tint = DesignTokens.PrimaryText)
                }

                if (isClicked) {
                    DatePickerModal(onDateSelected = { millis ->
                        millis?.let {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val dateString = formatter.format(java.util.Date(millis))
                            formDataStoreAndValidation[bindingKey] = dateString
                            sduiViewModel.saveFieldData(bindingKey, dateString)
                            activityScreenName?.let { screenName ->
                                FormDataStorage.updateFormData(screenName, bindingKey, dateString)
                            }
                        }
                        isClicked = false
                    }, onDismiss = { isClicked = false })
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DatePickerModal(
        onDateSelected: (Long?) -> Unit,
        onDismiss: () -> Unit
    ) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 30.dp),
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    @Composable
    private fun RenderEditText(node: ComponentNode, data: Map<String, String>) {
        val bindingKey = node.dataBinding ?: ""
        
        LaunchedEffect(bindingKey) {
            if (formDataStoreAndValidation[bindingKey].isNullOrEmpty()) {
                val savedValue = readAndSetValue(activityScreenName, bindingKey)
                formDataStoreAndValidation[bindingKey] = savedValue
            }
        }

        val value = formDataStoreAndValidation[bindingKey] ?: ""
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val cornerRadius = (node.style?.cornerRadius as? Int ?: 8).dp
        val isRequired = node.validation?.required == true
        val minLength = node.validation?.minLength as? Int ?: 0
        val isInputTypeValid = isInputTypeValid(value, node.inputType)

        val isError = when {
            isRequired && value.isEmpty() -> true
            value.isNotEmpty() && value.length < minLength -> true
            !isInputTypeValid -> true
            else -> false
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = node.screenAccessibility?.label ?: ""
                }
        ) {
            Text(
                text = node.label ?: "",
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = value,
                onValueChange = { data ->
                    formDataStoreAndValidation[bindingKey] = data
                    sduiViewModel.saveFieldData(bindingKey, data)
                    activityScreenName?.let { screenName ->
                        FormDataStorage.updateFormData(screenName, bindingKey, data)
                    }
                },
                placeholder = { Text(node.placeholder ?: "") },
                keyboardOptions = KeyboardOptions(keyboardType = getKeyboardType(node.inputType)),
                visualTransformation = if (node.inputType?.lowercase() == "password") {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = fontSize),
                shape = RoundedCornerShape(cornerRadius)
            )

            if (isError) {
                val errorText = when {
                    isRequired && value.isEmpty() -> "This field is required"
                    value.length < minLength -> "Minimum $minLength characters required"
                    !isInputTypeValid -> "Invalid ${node.inputType ?: "format"}"
                    else -> ""
                }
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    private fun getKeyboardType(inputType: String?): KeyboardType {
        return when (inputType?.lowercase()) {
            "number" -> KeyboardType.Number
            "phone" -> KeyboardType.Phone
            "email" -> KeyboardType.Email
            "password" -> KeyboardType.Password
            "decimal" -> KeyboardType.Decimal
            "uri" -> KeyboardType.Uri
            else -> KeyboardType.Text
        }
    }

    private fun isInputTypeValid(value: String, inputType: String?): Boolean {
        if (value.isEmpty()) return true
        return when (inputType?.lowercase()) {
            "email" -> android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
            "phone" -> android.util.Patterns.PHONE.matcher(value).matches()
            "number" -> value.all { it.isDigit() }
            "decimal" -> value.toDoubleOrNull() != null
            else -> true
        }
    }


    // ─── Text / Header ────────────────────────────────────────────────────────

    @Composable
    private fun RenderText(node: ComponentNode, data: Map<String, String>) {
        val text = node.template?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) }
            ?: node.dataBinding?.let { data[it] }
            ?: node.text
            ?: node.props["text"]
            ?: ""
        val color = (node.style?.foregroundColor ?: node.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        val maxLines = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
        val pad = node.style?.padding?.dp ?: 0.dp
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = if (pad > 0.dp) Modifier.padding(pad) else Modifier,
        )
    }

    @Composable
    private fun RenderHeader(
        node: ComponentNode,
        data: Map<String, String>,
        onAction: (currentScreen: String, actionId: String, params: Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
    ) {
        var title = node.titleTemplate?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) }
            ?: node.props["title"] ?: ""
        if (title.isEmpty() && !node.titleBinding?.isNullOrEmpty()!!) {
            title = bindingResolver.resolve(activityScreenName,node.titleBinding)
        }
        var subtitle: String? = node.subtitleTemplate?.let { resolver.resolve(it, data, sduiViewModel.getFieldData(it)) }
            ?: node.props["subtitle"]
        if (subtitle.isNullOrEmpty() && !node.subtitleBinding.isNullOrEmpty()) {
            subtitle = bindingResolver.resolve(activityScreenName,node.subtitleBinding)
        }
        val hasSearch = node.action?.type == "search"
        val isleadingIcon = !node.leadingIcon.isNullOrEmpty()
        val leadinIcon = node.leadingIcon
        val padH = node.style?.padding?.dp ?: DesignTokens.SpacingMd
        val padTop = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
        val padBottom = node.style?.paddingBottom?.dp ?: DesignTokens.SpacingSm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padH, end = padH, top = padTop, bottom = padBottom),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if(isleadingIcon){
                IconButton(onClick = { node.action?.dispatch(activityScreenName, data, onAction, node) }) {
                    Icon(Icons.Default.Backspace, contentDescription = "back", tint = DesignTokens.PrimaryText)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = DesignTokens.PrimaryText, fontSize = DesignTokens.TextXxl, fontWeight = FontWeight.Bold)
                subtitle?.let { Text(text = it, color = DesignTokens.SecondaryText, fontSize = DesignTokens.TextMd) }
            }
            if (hasSearch) {
                IconButton(onClick = { node.action?.dispatch(activityScreenName, data, onAction, node) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = DesignTokens.PrimaryText)
                }
            }
        }
    }

    // ─── Media ────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderImage(node: ComponentNode, data: Map<String, String>) {
        val url = node.dataBinding?.let { data[it] } ?: node.props["url"] ?: ""
        val w = node.style?.frameWidth?.dp
        val h = node.style?.frameHeight?.dp ?: 200.dp
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        val mod = (if (w != null) Modifier.size(width = w, height = h)
        else Modifier
            .fillMaxWidth()
            .height(h)).clip(RoundedCornerShape(radius))
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = mod)
    }

    @Composable
    private fun RenderIcon(node: ComponentNode) {
        val name = node.icon ?: node.props["icon"] ?: ""
        val color = node.style?.foregroundColor?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val size = node.style?.fontSize?.dp ?: 16.dp
        val icon = when {
            name.contains("search") -> Icons.Default.Search
            name.contains("play") || name.contains("tv") -> Icons.Default.PlayCircle
            else -> Icons.Default.Star
        }
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(size))
    }

    // ─── Input ────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderButton(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
        modifier: Modifier
    ) {
        val cornerRadius = (component.style?.cornerRadius as? Int ?: 8).dp
        val context = LocalContext.current

        val backgroundColor = when (component.variant) {
            "secondary" -> Color.LightGray
            else -> Color.Blue
        }

        val textColor = when (component.variant) {
            "secondary" -> Color.Black
            else -> Color.White
        }

        Button(
            onClick = {
                if(component.titleBinding?.equals("Back", ignoreCase = true) == true) {
                    component.action?.dispatch(activityScreenName, data, onAction, component)
                } else {
                    if(validateForm(activityScreenName ?: "")){
                        activityScreenName?.let { eventName ->
                            sduiViewModel.markFormCompleted(eventName, FormDataStorage.getFormJsonData(eventName))
                            component.analytics?.let {
                                analyticsEngine.track(
                                    AnalyticsEvent(
                                        provider = Provider.FIREBASE,
                                        eventName = eventName,
                                        params = bindAnalyticsDataParam(it),
                                    )
                                )
                            }

                        }
                        component.action?.dispatch(activityScreenName, data, onAction, component)
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            },
            modifier = modifier
                .semantics {
                    contentDescription =
                        (component.screenAccessibility?.label ?: bindingResolver.resolve( activityScreenName,component.titleBinding ?: "")).toString()
                },
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            )
        ) {

            Text(
                text = bindingResolver.resolve( activityScreenName,component.titleBinding ?: ""),
                color = textColor
            )
        }
    }

    @Composable
    private fun RenderButton(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
    ) {
        val height = (component.style?.height as? Int ?: 48).dp
        val cornerRadius = (component.style?.cornerRadius as? Int ?: 8).dp
        val weight = (component.style?.weight as? Int ?: 0).dp

        val backgroundColor = when (component.variant) {
            "secondary" -> Color.LightGray
            else -> Color.Blue
        }

        val textColor = when (component.variant) {
            "secondary" -> Color.Black
            else -> Color.White
        }
        val context = LocalContext.current
        Button(
            onClick = {
                if(bindingResolver.resolve( activityScreenName,component.titleBinding?: "").equals("Back", ignoreCase = true)) {
                    component.action?.dispatch(activityScreenName, data, onAction, component)
                } else {
                    if(validateForm(activityScreenName?: "")){
                        activityScreenName?.let { eventName ->
                            sduiViewModel.markFormCompleted(eventName, FormDataStorage.getFormJsonData(eventName))
                            component.analytics?.let {
                                analyticsEngine.track(
                                    AnalyticsEvent(
                                        provider = Provider.FIREBASE,
                                        eventName = eventName,
                                        params = bindAnalyticsDataParam(it),
                                    )
                                )
                            }

                        }
                        component.action?.dispatch(activityScreenName, data, onAction, component)
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .semantics {
                    contentDescription =
                        (component.accessibility?.label ?: bindingResolver.resolve( activityScreenName,component.titleBinding))
                },
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            )
        ) {

            Text(
                text = bindingResolver.resolve( activityScreenName,component.titleBinding ?: ""),
                color = textColor
            )
        }
    }

    private fun bindAnalyticsDataParam(it: Analytics): Map<String, String?> {
        val params = mutableMapOf<String, String?>()
        it.params?.forEach { param ->
            val value = FormDataStorage.readAndSetValue( activityScreenName,param.value.replace("{", "")?.replace("}", "") ?: "")
            params[param.key] = value
        }
        return params
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderList(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val binding = node.listDataBinding ?: return
        val items = listData[binding] ?: emptyList()
        val layout = node.itemLayout ?: return
        val spacing = node.style?.spacing?.dp ?: 0.dp
        val spacingPx = with(LocalDensity.current) { spacing.toPx() }

        // Drag state — shared across all items in this list
        var draggingIndex by remember { mutableIntStateOf(-1) }
        var dragOffsetY by remember { mutableStateOf(0f) }
        // Height of each item in px, populated via onGloballyPositioned
        val itemHeights = remember { mutableStateMapOf<Int, Float>() }

        // Index where the dragged item would be dropped at current offset
        val targetIndex = if (draggingIndex >= 0 && itemHeights.isNotEmpty() && items.isNotEmpty()) {
            val cellH = (itemHeights[draggingIndex] ?: itemHeights.values.average().toFloat()) + spacingPx
            (draggingIndex + (dragOffsetY / cellH).roundToInt()).coerceIn(items.indices)
        } else -1

        Column(
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            items.forEachIndexed { index, itemData ->
                val isDragging = index == draggingIndex
                val cellH = (itemHeights[draggingIndex] ?: 0f) + spacingPx

                // How far to visually shift this item so others "make room" for the dragged item
                val translationY = when {
                    isDragging -> dragOffsetY
                    targetIndex >= 0 && draggingIndex >= 0 -> when {
                        draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> -cellH
                        draggingIndex > targetIndex && index in targetIndex until draggingIndex -> cellH
                        else -> 0f
                    }
                    else -> 0f
                }

                // key() ensures Compose tracks each item by its identity, not list position —
                // so composable state stays correct when the list reorders.
                key(itemData["id"] ?: index.toString()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { itemHeights[index] = it.size.height.toFloat() }
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                this.translationY = translationY
                                if (isDragging) {
                                    scaleX = 1.03f
                                    scaleY = 1.03f
                                }
                            }
                            .pointerInput(index) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        if (draggingIndex >= 0 && itemHeights.isNotEmpty() && items.isNotEmpty()) {
                                            val h = (itemHeights[draggingIndex]
                                                ?: itemHeights.values.average()
                                                    .toFloat()) + spacingPx
                                            val to =
                                                (draggingIndex + (dragOffsetY / h).roundToInt())
                                                    .coerceIn(items.indices)
                                            if (to != draggingIndex) {
                                                onAction(
                                                    "reorder",
                                                    mapOf(
                                                        "binding" to binding,
                                                        "from" to draggingIndex.toString(),
                                                        "to" to to.toString(),
                                                    ),
                                                    null,
                                                    false
                                                )
                                            }
                                        }
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                )
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        renderNode(layout, data + itemData, listData, onAction)
                        // Drag handle — subtle visual hint at trailing edge
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = DesignTokens.SecondaryText.copy(alpha = 0.35f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(18.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderGeneratedList(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>, ActionModel?, isBackClickAction: Boolean) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val count = node.countBinding?.let { data[it]?.toIntOrNull() } ?: 0
        val layout = node.itemLayout ?: return
        val spacing = node.style?.spacing?.dp ?: 0.dp
        Column(
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            (1..count).forEach { i ->
                renderNode(layout, data + mapOf("seasonNumber" to i.toString(), "index" to i.toString()), listData, onAction)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Resolve any `{{key}}` in [routeTemplate] or [route], build the params map,
 * and invoke [onAction] with (type, params).
 */
private fun ActionModel.dispatch(
    currentScreen: String?,
    data: Map<String, String>,
    onAction: (String, String, Map<String, String>, action: ActionModel?, isBackClickAction: Boolean) -> Unit,
    node:ComponentNode
) {
    val resolvedRoute = routeTemplate?.let { tpl ->
        var r = tpl
        data.forEach { (k, v) -> r = r.replace("{{$k}}", v) }
        r
    } ?: route
    val params = buildMap<String, String> {
        resolvedRoute?.let { put("route", it) }
        putAll(this@dispatch.params)
    }.toMutableMap()

    destination?.let { des->
        params["route"] = des
    }
    val isBacKButtonClicked = node.action?.destination.equals("back", ignoreCase = true) || node.action?.destination.equals("previous", ignoreCase = true) || node.action?.backClicked == true
    onAction(currentScreen ?: "", type, params, this, isBacKButtonClicked)
}

private fun String?.toFontWeight(): FontWeight = when (this) {
    "bold" -> FontWeight.Bold
    "semibold" -> FontWeight.SemiBold
    "medium" -> FontWeight.Medium
    else -> FontWeight.Normal
}

@Composable
private fun UnknownComponent(type: String) {
    Box(Modifier.padding(DesignTokens.SpacingMd)) {
        Text("[unknown: $type]", color = DesignTokens.Accent)
    }
}
