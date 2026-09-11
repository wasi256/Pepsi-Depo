package com.example.pepsi.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Minimalist line chart card for a single trend, drawn with Canvas so the module
 * doesn't need a third-party charting dependency for a handful of sparkline-style graphs.
 */
@Composable
fun TrendLineChart(
    title: String,
    currentValueLabel: String,
    values: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Trend over recent activity", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = currentValueLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = lineColor,
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 12.dp),
            ) {
                if (values.size < 2) return@Canvas
                val minValue = values.min()
                val maxValue = values.max()
                val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
                val stepX = size.width / (values.size - 1)

                val points = values.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - ((value - minValue) / range) * size.height
                    Offset(x, y)
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                    )
                }
                points.forEach { point ->
                    drawCircle(color = lineColor, radius = 7f, center = point)
                }
            }
        }
    }
}
