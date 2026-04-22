package com.example.moviesdemoapp.engine.sdui.components

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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.ActionModel
import com.example.moviesdemoapp.engine.sdui.ComponentNode
import com.example.moviesdemoapp.engine.sdui.FormDataStorage
import com.example.moviesdemoapp.engine.sdui.TemplateResolver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get
import kotlin.math.roundToInt
import kotlin.ranges.rangeTo

/** Recursive render callback — must be called from a @Composable context. */
typealias NodeRenderer =
    @Composable (node: ComponentNode, data: Map<String, String>, listData: Map<String, List<Map<String, String>>>, onAction: (String, Map<String, String>) -> Unit) -> Unit

/**
 * Built-in rendering implementations for every standard SDUI component type.
 * Actions are forwarded as (actionId, params) strings — the ViewModel decides what to do.
 */
@Singleton
class SDUIComponents @Inject constructor(private val resolver: TemplateResolver) {
    private val formDataStoreAndValidation = FormDataStorage.formDataStoreAndValidation
    fun readAndSetValue(key: String?) = FormDataStorage.readAndSetValue(key)
    private fun validateForm() = FormDataStorage.validateForm()


    @Composable
    fun RenderBuiltIn(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        // Visibility check
        if (!resolver.isVisible(node, data)) return

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
            "list"           -> RenderList(node, data, listData, onAction, renderNode)
            "generatedList"  -> RenderGeneratedList(node, data, listData, onAction, renderNode)
            else -> Box(Modifier.padding(DesignTokens.SpacingMd)) {
                Text("[unknown: ${node.type}]", color = DesignTokens.Accent)
            }
        }
    }


    @Composable
    private fun RenderCurrencyField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ){
        val formState = remember {mutableStateMapOf<String, String>() }
        val min = component.validation?.min as? Int ?: 0
        var rawValue by remember {
            mutableStateOf(formState.getOrDefault(component.dataBinding, "").replace("$", ""))
        }

        val formattedValue = formatCurrency(rawValue)

        val isRequired = component.validation?.required == true

        LaunchedEffect(Unit) {
            if(isRequired &&  (formDataStoreAndValidation[component.dataBinding] == null || formDataStoreAndValidation[component.dataBinding]?.isEmpty() == true)){
                formDataStoreAndValidation[component.dataBinding ?: ""] = readAndSetValue(component.dataBinding)
            }
        }
        val numericValue = rawValue.toLongOrNull() ?: 0

        val isError = when {
            isRequired && rawValue.isEmpty() -> true
            numericValue < min -> true
            else -> false
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        (component.accessibility?.label ?: component.label).toString()
                }
        ) {

            // 🔤 Label
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 💰 Currency TextField
            OutlinedTextField(
                value = formattedValue,
                onValueChange = { input ->

                    val clean = input.replace("[^0-9]".toRegex(), "")

                    rawValue = clean
                    component.dataBinding?.let {
                        formState[it] = clean
                        if (component.validation?.required == true) {
                            formDataStoreAndValidation[component.dataBinding] = "$$clean"
                        }
                    }

                },
                placeholder = {
                    Text(component.placeholder ?: "")
                },
                leadingIcon = {
                    Text(component.currencySymbol ?: "")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )

            // ❗ Error Message
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

    class ToggleFormState {

        private val data = mutableStateMapOf<String, Any>()

        fun setValue(key: String, value: Any) {
            data[key] = value
        }

        fun getBoolean(key: String): Boolean {
            return data[key] as? Boolean ?: false
        }
    }

    @Composable
    private fun RenderToggleField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ){
        val formState = remember { ToggleFormState() }

        var checked by remember {
            mutableStateOf(formState.getBoolean(component.dataBinding ?: ""))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription =
                        (component.accessibility?.label ?: component.label).toString()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // 🔤 Label
            Text(
                text = component.label ?: "",
                fontSize = 16.sp
            )

            // 🔘 Switch
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    formState.setValue(component.dataBinding ?: "", it)
                }
            )
        }
    }

    class FormState {

        private val data = mutableStateMapOf<String, Int>()

        fun setValue(key: String, value: Int) {
            data[key] = value
        }

        fun getValue(key: String, default: Int): Int {
            return data[key] ?: default
        }
    }

    @Composable
    private fun RenderStepperField(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ){
        val formState = remember { FormState() }

        var value by remember {
            mutableIntStateOf(
                formState.getValue(component.dataBinding ?: "", component.minValue?: 1)
            )
        }

        val min = component.minValue ?: 1
        val max = component.maxValue ?: 1
        val step = component.step ?: 1

        val height = (component.style?.height as? Int ?: 40).dp

        fun formatValue(v: Int): String {
            return component.displayTemplate
                ?.replace("{{value}}", v.toString())
                ?: v.toString()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        (component.accessibility?.label ?: component.label).toString()
                }
        ) {

            // 🔤 Label
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 🔢 Stepper Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // ➖ Decrease
                IconButton(
                    onClick = {
                        if (value - step >= min) {
                            value -= step
                            formState.setValue(component.dataBinding ?: "", value)
                        }
                    }
                ) {
                    Text("-", fontSize = 20.sp)
                }

                // 📊 Value Display
                Text(
                    text = formatValue(value),
                    fontSize = 16.sp
                )

                // ➕ Increase
                IconButton(
                    onClick = {
                        if (value + step <= max) {
                            value += step
                            formState.setValue(component.dataBinding ?: "", value)
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
        onAction: (String, Map<String, String>) -> Unit,
    ){
        val formState = remember {mutableStateMapOf<String, Float>() }
        val min = component.minValue?.toFloat() ?: 0.0f
        val max = component.maxValue?.toFloat() ?: 0.0f
        val step = component.step?.toFloat() ?: 0.0f

        var sliderValue by remember {
            mutableStateOf(
                formState[component.dataBinding]
                    .takeIf { it != 0f } ?: min
            )
        }

        // Calculate steps (Compose expects steps count, not value)
        val steps = ((max - min) / step).toInt() - 1

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        (component.accessibility?.label ?: component.label).toString()
                }
        ) {

            // 🔤 Label
            Text(
                text = component.label ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 📊 Current Value Display
            Text(
                text = "₹${sliderValue.toInt()}",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 🎚️ Slider
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    component.dataBinding?.let { key ->
                        formState[key] =it
                    }

                },
                valueRange = min..max,
                steps = steps
            )

            // 📏 Min / Max Labels
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
        onAction: (String, Map<String, String>) -> Unit,
    ) {
        val title = node.props["title"]
            ?: node.titleTemplate?.let { resolver.resolve(it, data) } ?: ""
        val subtitle = node.props["subtitle"]
            ?: node.subtitleTemplate?.let { resolver.resolve(it, data) }
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
                                .clickable { node.action?.dispatch(data, onAction) },
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
                        IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
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
        onAction: (String, Map<String, String>) -> Unit,
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
            node.children.forEach { renderNode(it, data, listData, onAction) }
        }
    }


    fun resolveValue(template: String, formState: SnapshotStateMap<String, Any>): String {
        try {
            val regex = "\\{\\{(.+?)}}".toRegex()
            val match = regex.find(template)

            val key = match?.groupValues?.get(1) ?: return ""

            return formState.getValue(key).toString() ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    @Composable
    private fun RenderSummeryRow(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val formState = remember { mutableStateMapOf<String, Any>() }
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        if (node.action != null) mod = mod.clickable { node.action.dispatch(data, onAction) }
        val value = formDataStoreAndValidation[node.valueTemplate?.replace("{{", "")?.replace("}}", "")] ?: resolveValue(node.valueTemplate?: "", formState)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        if (node.action != null) mod = mod.clickable { node.action.dispatch(data, onAction) }
        Row(modifier = mod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            node.children.forEach {
                if (it.type.equals("button", ignoreCase = true) && it.style?.weight != null) {
                     RenderButton(
                         component =  it, data=  data, onAction =  onAction, modifier = Modifier.weight(it.style.weight)
                    )
                } else {
                    renderNode(it, data, listData, onAction)
                }
            }
        }
    }

    @Composable
    private fun RenderCard(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) } ?: DesignTokens.CardBackground
        val pad = node.style?.padding?.dp?.takeIf { it > 0.dp } ?: DesignTokens.SpacingMd
        val radius = node.style?.cornerRadius?.dp ?: DesignTokens.RadiusMd
        var mod = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm)
        if (node.action != null) mod = mod.clickable { node.action.dispatch(data, onAction) }
        Card(
            modifier = mod,
            shape = RoundedCornerShape(radius),
            colors = CardDefaults.cardColors(containerColor = bg),
        ) {
            Column(modifier = Modifier.padding(pad)) {
                node.children.forEach {
                    renderNode(it, data, listData, onAction)
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
        val formState = remember { mutableStateMapOf<String, String>() }
        var expanded by remember { mutableStateOf(false) }

        val selectedValue = formState[component.dataBinding]

        val selectedTitle: String? = component.options
            ?.find { it.value == selectedValue }
            ?.title ?: component.placeholder

        val cornerRadius = (component.style?.cornerRadius?.toInt() ?: 8).dp
        val color = (component.style?.foregroundColor ?: component.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = component.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = component.style?.fontWeight.toFontWeight()
        val maxLines = component.style?.lineLimit ?: component.style?.maxLines ?: Int.MAX_VALUE
        val pad = component.style?.padding?.dp ?: 0.dp
        LaunchedEffect(Unit) {
            if(component.validation?.required == true &&  (formDataStoreAndValidation[component.dataBinding] == null || formDataStoreAndValidation[component.dataBinding]?.isEmpty() == true)){
                formDataStoreAndValidation[component.dataBinding ?: ""] = readAndSetValue(component.dataBinding)
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {

            // 🔤 Label
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
                            .background(
                                if (isSelected) Color(0xFF4CAF50) else Color.Transparent
                            )
                            .clickable {
                                component.dataBinding?.let {
                                    formState[it] = option.value
                                    if (component.validation?.required == true) {
                                        formDataStoreAndValidation[component.dataBinding] =
                                            option.value
                                    }
                                }

                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.title,
                            fontSize = fontSize,
                            color = Color.White
                        )
                    }
                }
            }

            // ⚠️ Validation
            val isError = component.validation?.required == true &&
                    formState[component.dataBinding].isNullOrEmpty()

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
        val formState = remember { mutableStateMapOf<String, String>() }
        var expanded by remember { mutableStateOf(false) }

        val selectedValue = formState[component.dataBinding]

        val selectedTitle: String = component.options
            ?.find { it.value == selectedValue }
            ?.title ?: component.placeholder ?: ""

        val color = (component.style?.foregroundColor ?: component.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = component.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = component.style?.fontWeight.toFontWeight()
        val maxLines = component.style?.lineLimit ?: component.style?.maxLines ?: Int.MAX_VALUE
        val pad = component.style?.padding?.dp ?: 0.dp
        val cornerRadius = component.style?.cornerRadius?.dp ?: 0.dp

        val isError = component.validation?.required == true &&
                selectedValue.isNullOrEmpty()
        LaunchedEffect(Unit) {
            if (component.validation?.required == true &&  (formDataStoreAndValidation[component.dataBinding] == null || formDataStoreAndValidation[component.dataBinding]?.isEmpty() == true)) {
                formDataStoreAndValidation[component.dataBinding ?: ""] = readAndSetValue(component.dataBinding)
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = component.accessibility?.label ?: ""
                }
        ) {

            // 🔤 Label
            Text(
                text = component.label ?:"",
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 📦 Dropdown Field
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedTitle,
                    onValueChange = {},
                    readOnly = true,
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
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
                            text = {
                                Text(
                                    text = option.title,
                                    fontSize = fontSize
                                )
                            },
                            onClick = {
                                component.dataBinding?.let {
                                    formState[it] = option.value
                                }
                                if(component.validation?.required == true){
                                    formDataStoreAndValidation[component.dataBinding ?: ""] = option.value
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }

            // ❗ Validation Error
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

/*    @Composable
    private fun RenderDropDownField(component: ComponentNode, data: Map<String, String>) {
        val formState = remember { mutableStateMapOf<String, String>() }
        var expanded by remember { mutableStateOf(false) }

        val selectedValue = formState[component.dataBinding]

        val selectedTitle: String? = component.options
            ?.find { it.value == selectedValue }
            ?.title ?: component.placeholder

        val color = (component.style?.foregroundColor ?: component.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = component.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = component.style?.fontWeight.toFontWeight()
        val maxLines = component.style?.lineLimit ?: component.style?.maxLines ?: Int.MAX_VALUE
        val pad = component.style?.padding?.dp ?: 0.dp
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(color = color)) {

            // 🔤 Label
            component.label?.let {
                Text(
                    text = it,
                    fontSize = fontSize,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // 📦 Dropdown Box
            Box {
                (selectedTitle ?: component.options?.get(0)?.title)?.let {
                    OutlinedTextField(
                        value = it,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        textStyle = TextStyle(fontSize = fontSize),
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }

                DropdownMenu(
                    modifier = Modifier.clickable{
                        expanded  = !expanded
                    },
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    component.options?.forEach { option ->

                        DropdownMenuItem(
                            text = { Text(option.title) },
                            onClick = {
                                component.dataBinding?.let {
                                    formState[it] = option.value
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }*/

    @Composable
    private fun RenderDateField(node: ComponentNode, data: Map<String, String>) {
        val text = node.label ?: ""
        val placeholder = node.placeholder ?: ""
        val color = (node.style?.foregroundColor ?: node.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        val maxLines = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
        val pad = node.style?.padding?.dp ?: 0.dp
        var changedText: String? by remember { mutableStateOf(null) }
        Column {
            Text(
                text = text,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = if (pad > 0.dp) Modifier.padding(pad) else Modifier,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = changedText ?: placeholder,
                    color = color,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (pad > 0.dp) Modifier.padding(pad) else Modifier,
                )
                var isClicked by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    isClicked  =!isClicked
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "back", tint = DesignTokens.PrimaryText)
                }

                if (isClicked) {
                    DatePickerModal(onDateSelected = { millis ->
                        millis?.let {
                            val formatter =
                                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            val dateString: String? = formatter.format(Date(millis))
                            dateString?.let {
                                changedText = dateString
                            }
                        }
                        isClicked  = false
                    }, onDismiss = {
                        isClicked  = false
                    })
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
        val formState = remember { mutableStateMapOf<String, String>() }
        val value = formState[node.dataBinding] ?: ""
        val text = node.label ?: ""
        val placeholder = node.placeholder ?: ""
        val color = (node.style?.foregroundColor ?: node.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        val maxLines = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
        val pad = node.style?.padding?.dp ?: 0.dp

        val cornerRadius = (node.style?.cornerRadius as? Int ?: 8).dp

        val isRequired = node.validation?.required == true
        val minLength = node.validation?.minLength as? Int ?: 0
        LaunchedEffect(Unit) {
            if(isRequired &&  (formDataStoreAndValidation[node.dataBinding] == null || formDataStoreAndValidation[node.dataBinding]?.isEmpty() == true)){
                formDataStoreAndValidation[node.dataBinding ?: ""] = readAndSetValue(node.dataBinding)
            }
        }


        val isError = when {
            isRequired && value.isEmpty() -> true
            value.isNotEmpty() && value.length < minLength -> true
            else -> false
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = node.accessibility?.label ?: ""
                }
        ) {

            // 🔤 Label
            Text(
                text = node.label ?: "",
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // ✏️ TextField
            OutlinedTextField(
                value = value,
                onValueChange = { data->
                    node.dataBinding?.let { key->
                        formState[key] =  data
                        formDataStoreAndValidation[node.dataBinding ?: ""] = data
                    }

                },
                placeholder = {
                    Text(node.placeholder ?: "")
                },
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = fontSize),
                shape = RoundedCornerShape(cornerRadius)
            )

            // ❗ Error Message
            if (isError) {
                val errorText = when {
                    isRequired && value.isEmpty() -> "This field is required"
                    value.length < minLength -> "Minimum $minLength characters required"
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



        /*val text = node.label ?: ""
        val placeholder = node.placeholder ?: ""
        val color = (node.style?.foregroundColor ?: node.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        val maxLines = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
        val pad = node.style?.padding?.dp ?: 0.dp
        var changedText by remember { mutableStateOf("") }
        Column {
            TextField(
                value = text,
                onValueChange = { newText -> changedText = newText },
                label = { Text(placeholder) },
                modifier = if (pad > 0.dp) Modifier.padding(pad) else Modifier,
                maxLines = maxLines,
                colors = TextFieldDefaults.colors()
            )

        }*/





    }

    // ─── Text / Header ────────────────────────────────────────────────────────

    @Composable
    private fun RenderText(node: ComponentNode, data: Map<String, String>) {
        val text = node.template?.let { resolver.resolve(it, data) }
            ?: node.dataBinding?.let { data[it] }
            ?: node.text
            ?: node.props["text"] ?: ""
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
        onAction: (String, Map<String, String>) -> Unit,
    ) {
        var title = node.titleTemplate?.let { resolver.resolve(it, data) }
            ?: node.props["title"] ?: ""
        if(title.isEmpty() && node.title != null){
            title =  node.title
        }
        val subtitle = node.subtitleTemplate?.let { resolver.resolve(it, data) }
            ?: node.props["subtitle"]
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
                IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
                    Icon(Icons.Default.Backspace, contentDescription = "back", tint = DesignTokens.PrimaryText)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = DesignTokens.PrimaryText, fontSize = DesignTokens.TextXxl, fontWeight = FontWeight.Bold)
                subtitle?.let { Text(text = it, color = DesignTokens.SecondaryText, fontSize = DesignTokens.TextMd) }
            }
            if (hasSearch) {
                IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
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
        onAction: (String, Map<String, String>) -> Unit,
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
                if(component.title.equals("Back", ignoreCase = true)) {
                    component.action?.dispatch(data, onAction)
                } else {
                    if(validateForm()){
                        component.action?.dispatch(data, onAction)
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
                        (component.accessibility?.label ?: component.title).toString()
                },
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            )
        ) {

            Text(
                text = component.title ?: "",
                color = textColor
            )
        }
    }

    @Composable
    private fun RenderButton(
        component: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
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
                if(component.title.equals("Back", ignoreCase = true)) {
                    component.action?.dispatch(data, onAction)
                } else {
                    if(validateForm()){
                        component.action?.dispatch(data, onAction)
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
                        (component.accessibility?.label ?: component.title).toString()
                },
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            )
        ) {

            Text(
                text = component.title ?: "",
                color = textColor
            )
        }
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderList(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
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
        onAction: (String, Map<String, String>) -> Unit,
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
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
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
    onAction(type, params)
}

private fun String?.toFontWeight(): FontWeight = when (this) {
    "bold" -> FontWeight.Bold
    "semibold" -> FontWeight.SemiBold
    "medium" -> FontWeight.Medium
    else -> FontWeight.Normal
}
