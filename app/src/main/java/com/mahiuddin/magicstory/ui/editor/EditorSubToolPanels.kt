package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun EditorSubToolContainer(
    activeSubTool: ActiveSubTool,
    selectedId: String,
    selectedElem: TemplateElement?,
    state: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onOpenColorPicker: (targetId: String, currentHex: String, mode: String) -> Unit,
    onOpenGradientBuilder: (targetId: String) -> Unit,
    onOpenEyedropper: (mode: String) -> Unit
) {
    var effectTab by remember { mutableStateOf("DROP_SHADOW") }

    when (activeSubTool) {
        ActiveSubTool.FONT_FAMILY -> {
            val currentFont = state.getTextFontFamily(selectedId, "SANS_SERIF")
            val fonts = listOf(
                "SANS_SERIF" to Pair("Roboto Sans", FontFamily.SansSerif),
                "SERIF" to Pair("Editorial Serif", FontFamily.Serif),
                "SCRIPT" to Pair("Dancing Script", FontFamily.Cursive),
                "MONOSPACE" to Pair("Space Mono", FontFamily.Monospace),
                "CASUAL" to Pair("Casual Sans", FontFamily.Default),
                "SANS_SERIF_BLACK" to Pair("Impact Heavy", FontFamily.SansSerif),
                "SANS_SERIF_CONDENSED" to Pair("Fashion Condensed", FontFamily.SansSerif),
                "SANS_SERIF_LIGHT" to Pair("Montserrat Light", FontFamily.SansSerif),
                "SANS_SERIF_MEDIUM" to Pair("Inter Medium", FontFamily.SansSerif)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fonts.forEach { (key, info) ->
                    val isSelected = currentFont.equals(key, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateFontFamily(selectedId, key) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryRed.copy(alpha = 0.15f) else Color.White,
                        border = BorderStroke(1.5.dp, if (isSelected) PrimaryRed else Color(0xFFEAEAEA))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aa MagicStory",
                                fontFamily = info.second,
                                fontSize = 15.sp,
                                fontWeight = if (key.contains("BLACK") || key.contains("CONDENSED")) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF202020)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = info.first,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PrimaryRed else Color(0xFF777777)
                            )
                        }
                    }
                }
            }
        }
        ActiveSubTool.FONT_SIZE -> {
            val currentSize = state.getFontSize(selectedId, 24f)
            MinimalSlider(
                value = currentSize,
                onValueChange = { viewModel.updateFontSize(selectedId, it) },
                onValueChangeFinished = { viewModel.commitGestureEnd() },
                valueRange = 0f..500f,
                label = "Size",
                valueFormatter = { "${it.toInt()} sp" },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        ActiveSubTool.TEXT_COLOR -> {
            val currentElemColor = state.getTextColor(selectedId, "#000000")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onOpenEyedropper("FILL") },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onOpenColorPicker(selectedId, currentElemColor, "FILL") },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Color Picker", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onOpenGradientBuilder(selectedId) },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Gradient, contentDescription = "Custom Gradient Builder", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                        }
                    }

                    state.savedSwatches.forEach { colorHex ->
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.updateTextColor(selectedId, colorHex) },
                            shape = CircleShape,
                            color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                            border = BorderStroke(1.5.dp, Color(0xFFEAEAEA))
                        ) {}
                    }
                }
            }
        }
        ActiveSubTool.STROKE -> {
            val currentStroke = state.getElementStroke(selectedId) ?: Pair("#C91F2C", 0f)
            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MinimalSlider(
                    value = currentStroke.second,
                    onValueChange = { viewModel.updateElementStroke(selectedId, currentStroke.first, it) },
                    valueRange = 0f..30f,
                    label = "Stroke Width",
                    valueFormatter = { "${it.toInt()} dp" }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onOpenEyedropper("STROKE") },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Stroke", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onOpenColorPicker(selectedId, currentStroke.first, "STROKE") },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Stroke Color Picker", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.updateElementStroke(selectedId, null, 0f) },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear Stroke", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                        }
                    }

                    state.savedSwatches.forEach { hex ->
                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.updateElementStroke(selectedId, hex, currentStroke.second.coerceAtLeast(3f)) },
                            shape = CircleShape,
                            color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black },
                            border = BorderStroke(1.dp, Color(0xFFEAEAEA))
                        ) {}
                    }
                }
            }
        }
        ActiveSubTool.OPACITY -> {
            val currentOpacity = state.getElementOpacity(selectedId)
            MinimalSlider(
                value = currentOpacity,
                onValueChange = { viewModel.updateElementOpacity(selectedId, it) },
                valueRange = 0f..1f,
                label = "Opacity",
                valueFormatter = { "${(it * 100).toInt()}%" },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        ActiveSubTool.EFFECTS -> {
            val curEff = state.getLayerEffect(selectedId)
            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = effectTab == "DROP_SHADOW",
                        onClick = { effectTab = "DROP_SHADOW" },
                        label = { Text("Drop Shadow 👥", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = effectTab == "OUTER_GLOW",
                        onClick = { effectTab = "OUTER_GLOW" },
                        label = { Text("Outer Glow ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = effectTab == "INNER_GLOW",
                        onClick = { effectTab = "INNER_GLOW" },
                        label = { Text("Inner Glow 🌟", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                when (effectTab) {
                    "DROP_SHADOW" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenColorPicker(selectedId, curEff.dropShadowColor ?: "#000000", "SHADOW") },
                                shape = CircleShape,
                                color = try { Color(android.graphics.Color.parseColor(curEff.dropShadowColor ?: "#000000")) } catch (_: Exception) { Color.Black },
                                border = BorderStroke(1.5.dp, PrimaryRed)
                            ) {}

                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenEyedropper("SHADOW") },
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Shadow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                }
                            }

                            FilterChip(
                                selected = curEff.dropShadowColor != null,
                                onClick = {
                                    if (curEff.dropShadowColor != null) {
                                        viewModel.updateDropShadow(selectedId, null, 0f, 45f, 0f, 0f, 0f)
                                    } else {
                                        viewModel.updateDropShadow(selectedId, "#000000", 12f, 135f, 8f, 2f, 0.6f)
                                    }
                                },
                                label = { Text(if (curEff.dropShadowColor != null) "Disable Shadow" else "Enable Shadow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }

                        if (curEff.dropShadowColor != null) {
                            MinimalSlider(
                                value = curEff.dropShadowOpacity,
                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, curEff.dropShadowDistance, curEff.dropShadowSpread, it) },
                                valueRange = 0f..1f,
                                label = "Opacity",
                                valueFormatter = { "${(it * 100).toInt()}%" }
                            )
                            MinimalSlider(
                                value = curEff.dropShadowRadius,
                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, it, curEff.dropShadowAngle, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                valueRange = 0f..60f,
                                label = "Blur",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                            MinimalSlider(
                                value = curEff.dropShadowDistance,
                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, it, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                valueRange = 0f..100f,
                                label = "Distance",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                            Column {
                                MinimalSlider(
                                    value = curEff.dropShadowAngle,
                                    onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, it, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                    valueRange = 0f..360f,
                                    label = "Angle",
                                    valueFormatter = { "${it.toInt()}°" }
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(45f to "45° (Diag)", 90f to "90° (V)", 135f to "135° (PS)", 180f to "180°", 270f to "270°", 315f to "315°").forEach { (ang, label) ->
                                        FilterChip(
                                            selected = curEff.dropShadowAngle == ang,
                                            onClick = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, ang, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                            label = { Text(label, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                        )
                                    }
                                }
                            }
                            MinimalSlider(
                                value = curEff.dropShadowSpread,
                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, curEff.dropShadowDistance, it, curEff.dropShadowOpacity) },
                                valueRange = 0f..30f,
                                label = "Spread",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                        }
                    }
                    "OUTER_GLOW" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenColorPicker(selectedId, curEff.outerGlowColor ?: "#FF0055", "OUTER_GLOW") },
                                shape = CircleShape,
                                color = try { Color(android.graphics.Color.parseColor(curEff.outerGlowColor ?: "#FF0055")) } catch (_: Exception) { Color.Red },
                                border = BorderStroke(1.5.dp, PrimaryRed)
                            ) {}

                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenEyedropper("OUTER_GLOW") },
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Glow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                }
                            }

                            FilterChip(
                                selected = curEff.outerGlowColor != null,
                                onClick = {
                                    if (curEff.outerGlowColor != null) {
                                        viewModel.updateOuterGlow(selectedId, null, 0f, 0f, 0f)
                                    } else {
                                        viewModel.updateOuterGlow(selectedId, "#FF0055", 16f, 4f, 0.8f)
                                    }
                                },
                                label = { Text(if (curEff.outerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }

                        if (curEff.outerGlowColor != null) {
                            MinimalSlider(
                                value = curEff.outerGlowOpacity,
                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, curEff.outerGlowRadius, curEff.outerGlowSpread, it) },
                                valueRange = 0f..1f,
                                label = "Opacity",
                                valueFormatter = { "${(it * 100).toInt()}%" }
                            )
                            MinimalSlider(
                                value = curEff.outerGlowRadius,
                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, it, curEff.outerGlowSpread, curEff.outerGlowOpacity) },
                                valueRange = 0f..80f,
                                label = "Radius",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                            MinimalSlider(
                                value = curEff.outerGlowSpread,
                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, curEff.outerGlowRadius, it, curEff.outerGlowOpacity) },
                                valueRange = 0f..40f,
                                label = "Spread",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                        }
                    }
                    "INNER_GLOW" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenColorPicker(selectedId, curEff.innerGlowColor ?: "#00D2FF", "INNER_GLOW") },
                                shape = CircleShape,
                                color = try { Color(android.graphics.Color.parseColor(curEff.innerGlowColor ?: "#00D2FF")) } catch (_: Exception) { Color.Cyan },
                                border = BorderStroke(1.5.dp, PrimaryRed)
                            ) {}

                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onOpenEyedropper("INNER_GLOW") },
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Inner Glow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                }
                            }

                            FilterChip(
                                selected = curEff.innerGlowColor != null,
                                onClick = {
                                    if (curEff.innerGlowColor != null) {
                                        viewModel.updateInnerGlow(selectedId, null, 0f, 0f, 0f)
                                    } else {
                                        viewModel.updateInnerGlow(selectedId, "#00D2FF", 12f, 2f, 0.7f)
                                    }
                                },
                                label = { Text(if (curEff.innerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }

                        if (curEff.innerGlowColor != null) {
                            MinimalSlider(
                                value = curEff.innerGlowOpacity,
                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, curEff.innerGlowRadius, curEff.innerGlowChoke, it) },
                                valueRange = 0f..1f,
                                label = "Opacity",
                                valueFormatter = { "${(it * 100).toInt()}%" }
                            )
                            MinimalSlider(
                                value = curEff.innerGlowRadius,
                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, it, curEff.innerGlowChoke, curEff.innerGlowOpacity) },
                                valueRange = 0f..50f,
                                label = "Radius",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                            MinimalSlider(
                                value = curEff.innerGlowChoke,
                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, curEff.innerGlowRadius, it, curEff.innerGlowOpacity) },
                                valueRange = 0f..30f,
                                label = "Choke",
                                valueFormatter = { "${it.toInt()} dp" }
                            )
                        }
                    }
                }
            }
        }
        ActiveSubTool.TEXT_SPACING -> {
            val currentLetterSpacing = state.getLetterSpacing(selectedId, 0f)
            val currentLineSpacing = state.getLineSpacing(selectedId, 1.0f)
            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MinimalSlider(
                    value = currentLetterSpacing,
                    onValueChange = { viewModel.updateTextLetterSpacing(selectedId, it) },
                    onValueChangeFinished = { viewModel.commitGestureEnd() },
                    valueRange = 0.0f..0.4f,
                    label = "Letter",
                    valueFormatter = { "${(it * 100).toInt()}" }
                )
                MinimalSlider(
                    value = currentLineSpacing,
                    onValueChange = { viewModel.updateTextLineSpacing(selectedId, it) },
                    onValueChangeFinished = { viewModel.commitGestureEnd() },
                    valueRange = 0.8f..2.0f,
                    label = "Line",
                    valueFormatter = { "%.1f".format(it) }
                )
            }
        }
        ActiveSubTool.TEXT_BG -> {
            val bgColors = listOf("#C91F2C", "#A91521", "#211D1E", "#000000", "#FFF7F7", "#D9E0D8", "#EAEAEA")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.updateTextBgColor(selectedId, null) },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                    }
                }

                bgColors.forEach { colorHex ->
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.updateTextBgColor(selectedId, colorHex) },
                        shape = CircleShape,
                        color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                        border = BorderStroke(1.5.dp, Color(0xFFEAEAEA))
                    ) {}
                }
            }
        }
        ActiveSubTool.BG_TEXTURE -> {
            val textures = listOf("NONE", "PAPER", "NOISE", "CANVAS")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                textures.forEach { tex ->
                    FilterChip(
                        selected = state.bgTextureType == tex,
                        onClick = { viewModel.updateBgTexture(tex) },
                        label = { Text(tex) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                    )
                }
            }
        }
        ActiveSubTool.SHAPE_TYPE -> {
            val currentShape = (selectedElem as? TemplateElement.Shape)?.shapeType ?: "RECTANGLE"
            val shapeTypes = listOf(
                "RECTANGLE" to "⬛ Rect",
                "ROUNDED_RECTANGLE" to "🔲 Rounded",
                "TRIANGLE" to "🔺 Triangle",
                "ELLIPSE" to "🔴 Circle",
                "HEXAGON" to "⬢ Hexagon"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shapeTypes.forEach { (typeKey, label) ->
                    FilterChip(
                        selected = currentShape.equals(typeKey, ignoreCase = true),
                        onClick = { viewModel.updateShapeType(selectedId, typeKey) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                    )
                }
            }
        }
        ActiveSubTool.CORNER_RADIUS -> {
            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                CornerRadiusPanel(
                    shape = shape,
                    onUpdateSingleRadius = { viewModel.updateShapeCornerRadius(selectedId, it) },
                    onUpdateIndividualRadii = { tl, tr, br, bl, isLinked ->
                        viewModel.updateShapeCornerRadii(selectedId, tl, tr, br, bl, isLinked)
                    }
                )
            }
        }
        ActiveSubTool.GEOMETRY -> {
            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                GeometryPanel(
                    shape = shape,
                    onUpdateGeometry = { sides, points, innerRatio ->
                        viewModel.updateShapeGeometry(selectedId, sides, points, innerRatio)
                    }
                )
            }
        }
        ActiveSubTool.STROKE_ADVANCED -> {
            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                StrokeAdvancedPanel(
                    shape = shape,
                    editorState = state,
                    onUpdateStrokeStyle = { style, cap, join, opacity ->
                        viewModel.updateShapeStrokeStyle(selectedId, style, cap, join, opacity)
                    },
                    onUpdateStrokeWidth = { w ->
                        val strokeColor = state.getElementStroke(selectedId)?.first ?: shape.borderColor ?: "#C91F2C"
                        viewModel.updateElementStroke(selectedId, strokeColor, w)
                    },
                    onOpenColorPicker = {
                        onOpenColorPicker(
                            selectedId,
                            state.getElementStroke(selectedId)?.first ?: shape.borderColor ?: "#C91F2C",
                            "STROKE"
                        )
                    },
                    onOpenEyedropper = {
                        onOpenEyedropper("STROKE")
                    }
                )
            }
        }
        ActiveSubTool.ROTATE -> {
            val currentRot = state.getElementRotation(selectedId, 0f)
            MinimalSlider(
                value = currentRot,
                onValueChange = { viewModel.updateElementRotation(selectedId, it) },
                onValueChangeFinished = { viewModel.commitGestureEnd() },
                valueRange = 0f..360f,
                label = "Angle",
                valueFormatter = { "${it.toInt()}°" },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        else -> {}
    }
}
