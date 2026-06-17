package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.moonlited.pocketmanager.viewmodel.SanViewModel

val allDictionaryWords = listOf(
    "СЛОВО", "РАДОСТЬ", "ЗАДАЧА", "СВЕТ", "ВРЕМЯ", "КНИГА", "ПЛАН", "ДЕРЕВО",
    "РУКА", "СОЛНЦЕ", "ПРАВДА", "МЫСЛЬ", "КРАСОТА", "СИЛА", "УСПЕХ", "ЧУДО",
    "ГОРОД", "ДОРОГА", "СЕМЬЯ", "СЕРДЦЕ", "ПОБЕДА", "РАБОТА", "ДРУГ", "ОПЫТ"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MunsterbergTestScreen(
    viewModel: SanViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToStats: () -> Unit,
    onExit: () -> Unit = {}
) {
    val isSaved by viewModel.isSaved.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetState()
            onNavigateToStats()
        }
    }

    var timeLeft by remember { mutableStateOf(120) }
    var testActive by remember { mutableStateOf(false) }
    var testCompleted by remember { mutableStateOf(false) }

    var foundWords by remember { mutableStateOf(setOf<String>()) }
    var wrongWords by remember { mutableStateOf(setOf<String>()) }
    var inputText by remember { mutableStateOf("") }

    var testData by remember { mutableStateOf(Pair("", emptyList<String>())) }
    val gridText = testData.first
    val currentWords = testData.second

    LaunchedEffect(testActive) {
        if (testActive) {
            while (timeLeft > 0 && testActive) {
                delay(1000)
                timeLeft -= 1
            }
            if (timeLeft <= 0) {
                testActive = false
                testCompleted = true
            }
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = testActive) {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тест Мюнстерберга") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Вы уверены?") },
                text = { Text("Прогресс прохождения теста не будет сохранен. Вы действительно хотите выйти?") },
                confirmButton = {
                    TextButton(onClick = { 
                        showExitDialog = false
                        onExit()
                    }) { Text("Выйти") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("Отмена") }
                }
            )
        }
        
        var showRecommendationDialog by remember { mutableStateOf(false) }
        
        if (showRecommendationDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showRecommendationDialog = false
                    viewModel.saveMunsterbergTest(foundWords.size, 120 - timeLeft, wrongWords.size)
                },
                title = { Text("Обратите внимание") },
                text = { Text("Ваша результативность оказалась ниже 40%. Похоже, ваше внимание снижено из-за усталости. Рекомендуем сделать паузу и переключить вид деятельности.") },
                confirmButton = {
                    TextButton(onClick = { 
                        showRecommendationDialog = false
                        viewModel.saveMunsterbergTest(foundWords.size, 120 - timeLeft, wrongWords.size)
                    }) { Text("Понятно, сохранить результат") }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!testActive && !testCompleted && timeLeft == 120) {
                Text(
                    "Вам будет предложен текст из случайных букв, среди которых скрыты слова. Найдите их и впишите. У вас есть 2 минуты.\nПодсказка: все слова состоят минимум из 4 букв.",
                    textAlign = TextAlign.Center
                )
                Button(onClick = { 
                    testData = generateTestData()
                    testActive = true 
                }) {
                    Text("Начать тест")
                }
            } else if (testActive) {
                Text("Осталось времени: $timeLeft сек", fontWeight = FontWeight.Bold)

                Card(
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        gridText.split("\n").forEach { line ->
                            Row(horizontalArrangement = Arrangement.Center) {
                                line.forEach { char ->
                                    Text(
                                        text = char.toString(),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { 
                        inputText = it.uppercase()
                    },
                    label = { Text("Впишите найденное слово") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            val word = inputText.trim()
                            if (word.isNotEmpty()) {
                                if (currentWords.contains(word) && !foundWords.contains(word)) {
                                    foundWords = foundWords + word
                                    if (foundWords.size == currentWords.size) {
                                        testActive = false
                                        testCompleted = true
                                    }
                                } else if (!currentWords.contains(word)) {
                                    wrongWords = wrongWords + word
                                }
                                inputText = ""
                            }
                        }
                    )
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    foundWords.forEach { word ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(word, color = Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4CAF50))
                        )
                    }
                    wrongWords.forEach { word ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(word, color = Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFF44336))
                        )
                    }
                }

                Text("Найдено: ${foundWords.size} / ${currentWords.size}")

                Button(
                    onClick = {
                        testActive = false
                        testCompleted = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Завершить досрочно")
                }
            } else if (testCompleted) {
                Text("Тест завершен!", style = MaterialTheme.typography.titleLarge)
                Text("Вы нашли ${foundWords.size} слов из ${currentWords.size}")
                Text("Затрачено времени: ${120 - timeLeft} сек")

                Button(
                    onClick = {
                        val timeSpent = 120 - timeLeft
                        val timeFactor = kotlin.math.sqrt(kotlin.math.max(1f, 120f - timeSpent) / 120f)
                        val efficiency = if (currentWords.isEmpty()) 0f else timeFactor * (foundWords.size.toFloat() / 5f) / (wrongWords.size + 1)
                        if (efficiency < 0.4f) {
                            showRecommendationDialog = true
                        } else {
                            viewModel.saveMunsterbergTest(foundWords.size, timeSpent, wrongWords.size)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Сохранить результат")
                }
            }
        }
    }
}

fun generateTestData(): Pair<String, List<String>> {
    val selectedWords = allDictionaryWords.shuffled().take(5)
    val chars = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
    val lines = mutableListOf<String>()
    
    var wordIndex = 0
    while (lines.size < 13) {
        val lineLen = 15
        if (wordIndex < selectedWords.size && (lines.size % 2 == 1)) {
            val word = selectedWords[wordIndex]
            val maxPadding = lineLen - word.length
            val prefixLen = (0..maxPadding).random()
            val suffixLen = maxPadding - prefixLen
            
            val prefix = (1..prefixLen).map { chars.random() }.joinToString("")
            val suffix = (1..suffixLen).map { chars.random() }.joinToString("")
            
            lines.add(prefix + word + suffix)
            wordIndex++
        } else {
            lines.add((1..lineLen).map { chars.random() }.joinToString(""))
        }
    }
    
    val resultString = lines.joinToString("\n")
    return Pair(resultString, selectedWords)
}
