package com.mahiuddin.magicstory.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun MinimalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    label: String? = null,
    valueFormatter: (Float) -> String = { "%.0f".format(it) },
    activeColor: Color = PrimaryRed,
    inactiveColor: Color = Color(0xFFE0E0E0),
    thumbColor: Color = PrimaryRed
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = "$label: ${valueFormatter(value)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202020),
                modifier = Modifier.width(85.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .pointerInput(valueRange, steps) {
                    detectTapGestures { offset ->
                        val trackWidth = size.width.toFloat()
                        if (trackWidth > 0) {
                            val fraction = (offset.x / trackWidth).coerceIn(0f, 1f)
                            var newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            if (steps > 0) {
                                val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                                val stepIndex = Math.round((newValue - valueRange.start) / stepSize)
                                newValue = (valueRange.start + stepIndex * stepSize).coerceIn(valueRange.start, valueRange.endInclusive)
                            }
                            currentOnValueChange(newValue)
                            currentOnFinished?.invoke()
                        }
                    }
                }
                .pointerInput(valueRange, steps) {
                    detectDragGestures(
                        onDragEnd = { currentOnFinished?.invoke() },
                        onDragCancel = { currentOnFinished?.invoke() }
                    ) { change, _ ->
                        change.consume()
                        val trackWidth = size.width.toFloat()
                        if (trackWidth > 0) {
                            val fraction = (change.position.x / trackWidth).coerceIn(0f, 1f)
                            var newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            if (steps > 0) {
                                val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                                val stepIndex = Math.round((newValue - valueRange.start) / stepSize)
                                newValue = (valueRange.start + stepIndex * stepSize).coerceIn(valueRange.start, valueRange.endInclusive)
                            }
                            currentOnValueChange(newValue)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                val canvasW = size.width
                val canvasH = size.height
                val centerY = canvasH / 2f

                val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
                val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
                val thumbX = fraction * canvasW

                // Draw Inactive Track (2dp thickness)
                drawLine(
                    color = inactiveColor,
                    start = Offset(0f, centerY),
                    end = Offset(canvasW, centerY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Draw Active Track (2dp thickness)
                if (thumbX > 0) {
                    drawLine(
                        color = activeColor,
                        start = Offset(0f, centerY),
                        end = Offset(thumbX, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Draw Minimal Thumb (~9dp diameter, 4.5dp radius)
                drawCircle(
                    color = Color.White,
                    radius = 5.5.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
                drawCircle(
                    color = thumbColor,
                    radius = 4.5.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
            }
        }
    }
}
