package com.example.pepsi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pepsi.data.model.ChartEntry

@Composable
fun EntryBarChart(
    data: List<ChartEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp),
        ) {
            val barCount = data.size
            if (barCount == 0) return@Canvas
            val gap = size.width * 0.02f
            val barWidth = (size.width - gap * (barCount - 1)) / barCount

            drawLine(
                color = labelColor.copy(alpha = 0.3f),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f,
            )

            data.forEachIndexed { index, entry ->
                val barHeight = size.height * (entry.value.toFloat() / maxValue)
                val left = index * (barWidth + gap)
                val top = size.height - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f),
                )

                drawContext.canvas.nativeCanvas.drawText(
                    entry.value.toString(),
                    left + barWidth / 2,
                    (top - 8f).coerceAtLeast(12f),
                    android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.sp.toPx()
                    },
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
