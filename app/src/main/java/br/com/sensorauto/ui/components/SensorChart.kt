package br.com.sensorauto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @param yRange  Quando fornecido, a escala Y é estática (ex: -20f..20f para acelerômetro).
 *                Quando nulo, a escala se ajusta dinamicamente ao range dos dados.
 * @param axisLabels  Rótulos de cada série (ex: ["X","Y","Z"]). Exibidos com o valor atual.
 * @param axisValues  Valor mais recente de cada série, na mesma ordem de [series].
 * @param axisUnit    Unidade exibida ao lado do valor (ex: "m/s²", "rad/s").
 */
@Composable
fun SensorChart(
    title: String,
    icon: ImageVector,
    series: List<List<Float>>,
    seriesColors: List<Color>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float>? = null,
    axisLabels: List<String> = emptyList(),
    axisValues: List<Float>? = null,
    axisUnit: String = ""
) {
    Column(modifier = modifier) {
        // Header — título + ícone
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // Linha de valores por eixo
        if (axisLabels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                axisLabels.forEachIndexed { i, label ->
                    val color = seriesColors.getOrElse(i) { Color.Gray }
                    val value = axisValues?.getOrNull(i)
                    AxisChip(label = label, value = value, unit = axisUnit, color = color)
                }
            }
        }

        // Área do gráfico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Linhas de grade horizontais
                val gridColor = Color.Gray.copy(alpha = 0.15f)
                repeat(4) { i ->
                    val y = h * (i + 1) / 5f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                // Linha zero quando a escala é estática e cruza 0
                if (yRange != null && yRange.start < 0f && yRange.endInclusive > 0f) {
                    val zeroY = h - h * (0f - yRange.start) / (yRange.endInclusive - yRange.start)
                    drawLine(
                        Color.Gray.copy(alpha = 0.35f),
                        Offset(0f, zeroY),
                        Offset(w, zeroY),
                        strokeWidth = 1.5f
                    )
                }

                // Cada série
                series.forEachIndexed { seriesIdx, samples ->
                    if (samples.size < 2) return@forEachIndexed
                    val color = seriesColors.getOrElse(seriesIdx) { Color.Gray }

                    val min: Float
                    val max: Float
                    if (yRange != null) {
                        min = yRange.start
                        max = yRange.endInclusive
                    } else {
                        min = samples.min()
                        max = samples.max()
                    }
                    val range = (max - min).takeIf { it > 0f } ?: 1f

                    for (i in 1 until samples.size) {
                        val x0 = w * (i - 1) / (samples.size - 1).toFloat()
                        val x1 = w * i / (samples.size - 1).toFloat()
                        val y0 = h - h * (samples[i - 1] - min) / range
                        val y1 = h - h * (samples[i] - min) / range
                        drawLine(color, Offset(x0, y0), Offset(x1, y1), strokeWidth = 2f)
                    }
                }
            }

            // Rótulos do eixo X
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-60s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-30s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Agora", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Rótulos do eixo Y (escala estática)
            if (yRange != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = yRange.endInclusive.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = yRange.start.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AxisChip(label: String, value: Float?, unit: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = ": ",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = if (value != null) "%+.2f".format(value) else "—",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}
