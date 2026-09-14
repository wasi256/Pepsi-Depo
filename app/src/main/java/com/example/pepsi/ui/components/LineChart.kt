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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pepsi.data.model.TrendPoint

@Composable
fun LineChart(
    data: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (data.size < 2) return@Canvas
            val stepX = size.width / (data.size - 1)
            fun yFor(value: Int) = size.height - (size.height * value.toFloat() / maxValue)

            val path = Path()
            data.forEachIndexed { index, point ->
                val x = index * stepX
                val y = yFor(point.value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 5f))

            data.forEachIndexed { index, point ->
                val x = index * stepX
                val y = yFor(point.value)
                drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
                drawContext.canvas.nativeCanvas.drawText(
                    point.value.toString(),
                    x,
                    (y - 14f).coerceAtLeast(12f),
                    android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.sp.toPx()
                    },
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { point ->
                Text(
                    text = point.label,
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
