// ui/screens/StatsScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.moonlited.pocketmanager.data.api.MaslachResponse
import ru.moonlited.pocketmanager.data.api.MunsterbergResponse
import ru.moonlited.pocketmanager.data.api.SanTestResponse
import ru.moonlited.pocketmanager.viewmodel.SanViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt
import java.time.format.DateTimeFormatter

enum class StatsTestType(val title: String) {
    SAN("САН"),
    MASLACH("Маслач"),
    MUNSTERBERG("Мюнстерберг")
}

data class ChartPoint(val xLabel: String, val values: List<Float>)

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun StatsScreen(viewModel: SanViewModel, onOpenDrawer: () -> Unit, initialTest: String? = null) {
    val sanHistory by viewModel.sanHistory.collectAsState()
    val maslachHistory by viewModel.maslachHistory.collectAsState()
    val munsterbergHistory by viewModel.munsterbergHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTest by remember { 
        mutableStateOf(
            StatsTestType.entries.find { it.name == initialTest } ?: StatsTestType.SAN
        ) 
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogText by remember { mutableStateOf<String?>(null) }

    if (infoDialogTitle != null && infoDialogText != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null; infoDialogText = null },
            title = { Text(infoDialogTitle!!) },
            text = { Text(infoDialogText!!) },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null; infoDialogText = null }) {
                    Text("Понятно")
                }
            }
        )
    }

    fun getWeekStart(date: LocalDate): LocalDate {
        return date.with(java.time.DayOfWeek.MONDAY)
    }

    val sanChartData = remember(sanHistory) {
        val dailyGroups = sanHistory.mapNotNull {
            try {
                val dt = LocalDateTime.parse(it.date).toLocalDate()
                dt to it
            } catch (e: Exception) { null }
        }.groupBy { it.first }
            .toSortedMap()
            .entries.toList()
            .takeLast(30)

        if (dailyGroups.size > 15) {
            dailyGroups.chunked(2).map { chunk ->
                val allResults = chunk.flatMap { it.value }
                val avgS = allResults.map { it.second.scoreS }.average().toFloat()
                val avgA = allResults.map { it.second.scoreA }.average().toFloat()
                val avgN = allResults.map { it.second.scoreN }.average().toFloat()
                
                val label = if (chunk.size > 1) {
                    "${chunk.first().key.format(DateTimeFormatter.ofPattern("dd.MM"))}-${chunk.last().key.format(DateTimeFormatter.ofPattern("dd.MM"))}"
                } else {
                    chunk.first().key.format(DateTimeFormatter.ofPattern("dd.MM"))
                }
                
                ChartPoint(label, listOf(avgS, avgA, avgN))
            }
        } else {
            dailyGroups.map { (date, list) ->
                val avgS = list.map { it.second.scoreS }.average().toFloat()
                val avgA = list.map { it.second.scoreA }.average().toFloat()
                val avgN = list.map { it.second.scoreN }.average().toFloat()
                ChartPoint(date.format(DateTimeFormatter.ofPattern("dd.MM")), listOf(avgS, avgA, avgN))
            }
        }
    }

    val maslachChartData = remember(maslachHistory) {
        maslachHistory.mapNotNull {
            try {
                val dt = LocalDateTime.parse(it.date).toLocalDate()
                dt to it
            } catch (e: Exception) { null }
        }.groupBy { getWeekStart(it.first) }
            .toSortedMap()
            .map { (weekStart, list) ->
                val avgEI = list.map { it.second.emotionalExhaustion }.average().toFloat()
                val avgDP = list.map { it.second.depersonalization }.average().toFloat()
                val avgPA = list.map { it.second.personalAccomplishment }.average().toFloat()
                ChartPoint(weekStart.format(DateTimeFormatter.ofPattern("dd.MM")), listOf(avgEI, avgDP, avgPA))
            }
    }

    val munsterbergChartData = remember(munsterbergHistory) {
        munsterbergHistory.mapNotNull {
            try {
                val dt = LocalDateTime.parse(it.date).toLocalDate()
                dt to it
            } catch (e: Exception) { null }
        }.groupBy { getWeekStart(it.first) }
            .toSortedMap()
            .map { (weekStart, list) ->
                val avgEfficiency = list.map { 
                    val time = it.second.timeSpentSeconds.coerceAtMost(120)
                    ((120f - time) / 120f) * (it.second.correctWords / 5f)
                }.average().toFloat()
                ChartPoint(weekStart.format(DateTimeFormatter.ofPattern("dd.MM")), listOf(avgEfficiency))
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                },
                actions = {
                    IconButton(onClick = {
                        when (selectedTest) {
                            StatsTestType.SAN -> {
                                infoDialogTitle = "САН"
                                infoDialogText = "С - Самочувствие\nА - Активность\nН - Настроение\nГрафик показывает среднее значение за день."
                            }
                            StatsTestType.MASLACH -> {
                                infoDialogTitle = "Тест Маслач"
                                infoDialogText = "ЭИ - Эмоциональное истощение\nДП - Деперсонализация\nПД - Профессиональные достижения\nГрафик усреднен по неделям."
                            }
                            StatsTestType.MUNSTERBERG -> {
                                infoDialogTitle = "Тест Мюнстерберга"
                                infoDialogText = "Отображает количество правильно найденных слов и время (в секундах).\nГрафик усреднен по неделям."
                            }
                        }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Инфо")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && sanHistory.isEmpty() && maslachHistory.isEmpty() && munsterbergHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTest.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Выберите тест") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        StatsTestType.entries.forEach { test ->
                            DropdownMenuItem(
                                text = { Text(test.title) },
                                onClick = {
                                    selectedTest = test
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        when (selectedTest) {
                            StatsTestType.SAN -> {
                                DynamicsChart(
                                    points = sanChartData,
                                    lineColors = listOf(Color.Red, Color.Green, Color.Blue),
                                    lineLabels = listOf("С", "А", "Н"),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            StatsTestType.MASLACH -> {
                                DynamicsChart(
                                    points = maslachChartData,
                                    lineColors = listOf(Color.Red, Color.Cyan, Color.Magenta),
                                    lineLabels = listOf("ЭИ", "ДП", "ПД"),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            StatsTestType.MUNSTERBERG -> {
                                DynamicsChart(
                                    points = munsterbergChartData,
                                    lineColors = listOf(Color(0xFF4CAF50)),
                                    lineLabels = listOf("Результативность"),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTest) {
                        StatsTestType.SAN -> {
                            if (sanHistory.isEmpty()) item { Text("Нет данных", modifier = Modifier.padding(16.dp)) }
                            items(sanHistory) { result -> SanResultCard(result) }
                        }
                        StatsTestType.MASLACH -> {
                            if (maslachHistory.isEmpty()) item { Text("Нет данных", modifier = Modifier.padding(16.dp)) }
                            items(maslachHistory) { result -> MaslachResultCard(result) }
                        }
                        StatsTestType.MUNSTERBERG -> {
                            if (munsterbergHistory.isEmpty()) item { Text("Нет данных", modifier = Modifier.padding(16.dp)) }
                            items(munsterbergHistory) { result -> MunsterbergResultCard(result) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicsChart(
    points: List<ChartPoint>,
    lineColors: List<Color>,
    lineLabels: List<String>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Нет данных для графика", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val globalMax = points.flatMap { it.values }.maxOrNull() ?: 1f
    val yMax = if (globalMax > 0f) globalMax * 1.2f else 10f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            lineLabels.forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(lineColors[index], CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val paddingX = 40.dp.toPx()
            val paddingY = 20.dp.toPx()

            val effectiveWidth = canvasWidth - paddingX * 2
            val effectiveHeight = canvasHeight - paddingY * 2

            // Axes
            drawLine(
                color = Color.LightGray,
                start = Offset(paddingX, canvasHeight - paddingY),
                end = Offset(canvasWidth - paddingX, canvasHeight - paddingY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.LightGray,
                start = Offset(paddingX, paddingY),
                end = Offset(paddingX, canvasHeight - paddingY),
                strokeWidth = 2f
            )

            // Draw Y axis labels
            val yAxisTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            val steps = 4
            for (i in 0..steps) {
                val value = (yMax / steps) * i
                val y = canvasHeight - paddingY - (value / yMax) * effectiveHeight
                val label = if (yMax <= 5f) String.format("%.1f", value) else value.toInt().toString()
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    paddingX - 10f,
                    y + 10f,
                    yAxisTextPaint
                )
            }

            if (points.size > 1) {
                val stepX = effectiveWidth / (points.size - 1)

                for (lineIndex in lineColors.indices) {
                    val path = Path()
                    val pathAlpha = if (lineColors.size > 1) 0.6f else 1.0f
                    val strokeColor = lineColors[lineIndex].copy(alpha = pathAlpha)

                    points.forEachIndexed { index, point ->
                        val value = point.values.getOrNull(lineIndex) ?: 0f
                        val x = paddingX + index * stepX
                        val y = canvasHeight - paddingY - (value / yMax) * effectiveHeight

                        if (index == 0) path.moveTo(x, y)
                        else path.lineTo(x, y)

                        drawCircle(
                            color = strokeColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // X labels
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                val numLabels = minOf(5, points.size)
                if (numLabels > 0) {
                    val labelStepIndex = (points.size - 1).toFloat() / (numLabels - 1).coerceAtLeast(1)
                    for (i in 0 until numLabels) {
                        val pointIndex = (i * labelStepIndex).roundToInt().coerceIn(0, points.lastIndex)
                        val point = points[pointIndex]
                        
                        val labelText = point.xLabel.substringBefore("-")
                        val x = paddingX + pointIndex * stepX
                        
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            x,
                            canvasHeight,
                            textPaint
                        )
                    }
                }
            } else if (points.size == 1) {
                val x = canvasWidth / 2
                for (lineIndex in lineColors.indices) {
                    val value = points[0].values.getOrNull(lineIndex) ?: 0f
                    val y = canvasHeight - paddingY - (value / yMax) * effectiveHeight
                    drawCircle(
                        color = lineColors[lineIndex],
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    points[0].xLabel,
                    x,
                    canvasHeight,
                    textPaint
                )
            }
        }
    }
}

@Composable
fun SanResultCard(result: SanTestResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("С", result.scoreS, getColorForScore(result.scoreS, 1f, 7f))
                StatItem("А", result.scoreA, getColorForScore(result.scoreA, 1f, 7f))
                StatItem("Н", result.scoreN, getColorForScore(result.scoreN, 1f, 7f))
            }
        }
    }
}

fun getColorForScore(score: Float, min: Float, max: Float, invert: Boolean = false): Color {
    val rawRatio = (score - min) / (max - min)
    val ratio = rawRatio.coerceIn(0f, 1f)
    val finalRatio = if (invert) 1f - ratio else ratio
    val hue = finalRatio * 120f
    return Color.hsv(hue, 0.8f, 0.6f) // Using 0.6 value for better contrast on light themes
}

@Composable
fun RowScope.StatItem(label: String, value: Float, color: Color? = null, isInt: Boolean = true) {
    val displayValue = if (isInt) kotlin.math.round(value).toInt().toString() else String.format("%.2f", value)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = displayValue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color ?: Color.Unspecified
        )
    }
}

@Composable
fun MaslachResultCard(result: MaslachResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("ЭИ", result.emotionalExhaustion, getColorForScore(result.emotionalExhaustion, 0f, 54f, invert = true))
                StatItem("ДП", result.depersonalization, getColorForScore(result.depersonalization, 0f, 30f, invert = true))
                StatItem("ПД", result.personalAccomplishment, getColorForScore(result.personalAccomplishment, 0f, 48f, invert = false))
            }
        }
    }
}

@Composable
fun MunsterbergResultCard(result: MunsterbergResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            val timeFactor = kotlin.math.sqrt(kotlin.math.max(1f, 120f - result.timeSpentSeconds.toFloat()) / 120f)
            val efficiency = if (result.correctWords == 0) 0f else timeFactor * (result.correctWords.toFloat() / 5f) / (result.errors + 1)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Слов", result.correctWords.toFloat(), isInt = true)
                StatItem("Время (с)", result.timeSpentSeconds.toFloat(), isInt = true)
                StatItem("Ошибки", result.errors.toFloat(), isInt = true)
                StatItem("Рез-ть", efficiency, getColorForScore(efficiency, 0f, 1f), isInt = false)
            }
        }
    }
}