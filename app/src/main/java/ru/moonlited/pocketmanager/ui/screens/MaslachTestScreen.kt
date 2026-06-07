package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.viewmodel.SanViewModel

val maslachQuestions = listOf(
    "1. Я чувствую себя эмоционально опустошенным(ой) к концу рабочего дня." to "EE",
    "2. Я чувствую себя усталым(ой), когда встаю утром и должен(на) идти на работу." to "EE",
    "3. Я чувствую, что работа с людьми каждый день - это тяжелое бремя для меня." to "EE",
    "4. Я могу легко понять, что чувствуют мои пациенты/клиенты/коллеги." to "PA",
    "5. Я чувствую, что отношусь к некоторым людям как к безличным объектам." to "DP",
    "6. Работа с людьми весь день вызывает у меня стресс." to "EE",
    "7. Я очень эффективно решаю проблемы людей." to "PA",
    "8. Я чувствую, что выгорел(а) на своей работе." to "EE",
    "9. Я чувствую, что положительно влияю на жизни других людей через свою работу." to "PA",
    "10. С тех пор, как я начал(а) работать здесь, я стал(а) более черствым(ой) к людям." to "DP",
    "11. Я беспокоюсь о том, что эта работа делает меня более жестким(ой) эмоционально." to "DP",
    "12. У меня много энергии." to "PA",
    "13. Я чувствую разочарование в своей работе." to "EE",
    "14. Я чувствую, что слишком усердно работаю на своей работе." to "EE",
    "15. Я не особо забочусь о том, что происходит с некоторыми людьми на работе." to "DP",
    "16. Прямая работа с людьми вызывает у меня слишком большой стресс." to "EE",
    "17. Я легко могу создать расслабленную атмосферу для своих пациентов/клиентов/коллег." to "PA",
    "18. Я чувствую себя воодушевленным(ой) после работы с людьми." to "PA",
    "19. Я достиг(ла) много стоящего на этой работе." to "PA",
    "20. Я чувствую себя на пределе возможностей." to "EE",
    "21. На работе я решаю эмоциональные проблемы спокойно." to "PA",
    "22. Я чувствую, что люди винят меня за некоторые свои проблемы." to "DP"
)

val scaleOptions = listOf("0 (Никогда)", "1", "2", "3", "4", "5", "6 (Ежедневно)")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaslachTestScreen(
    viewModel: SanViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToStats: () -> Unit,
    onExit: () -> Unit = {}
) {
    val isSaved by viewModel.isSaved.collectAsState()

    val randomizedQuestions = remember { maslachQuestions.shuffled() }

    val originalIndexMap = remember { randomizedQuestions.map { maslachQuestions.indexOf(it) } }
    var answers by remember { mutableStateOf(MutableList(22) { -1 }) }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetState()
            onNavigateToStats()
        }
    }

    var showInfoDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentQuestionIndex < randomizedQuestions.size) {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тест Маслач") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, "Информация")
                    }
                }
            )
        }
    ) { padding ->
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("О тесте Маслач") },
                text = { Text("Опросник Маслач (MBI) используется для оценки профессионального выгорания. Он измеряет три аспекта: Эмоциональное истощение, Деперсонализацию и Редукцию личных достижений. Ответьте на 22 вопроса, оценив частоту возникновения чувств.") },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) { Text("Понятно") }
                }
            )
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentQuestionIndex < randomizedQuestions.size) {
                Text(
                    text = "Вопрос ${currentQuestionIndex + 1} из 22",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = randomizedQuestions[currentQuestionIndex].first.substringAfter(". "),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Text(
                            text = "От 0 (Никогда) до 6 (Ежедневно)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (0..6).forEach { score ->
                                Button(
                                    onClick = {
                                        val newAnswers = answers.toMutableList()
                                        newAnswers[originalIndexMap[currentQuestionIndex]] = score
                                        answers = newAnswers
                                        
                                        currentQuestionIndex++
                                    },
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(score.toString())
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Тест завершен!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = {
                        var ee = 0f
                        var dp = 0f
                        var pa = 0f
                        for (i in answers.indices) {
                            val score = answers[i].toFloat()
                            when (maslachQuestions[i].second) {
                                "EE" -> ee += score
                                "DP" -> dp += score
                                "PA" -> pa += score
                            }
                        }
                        viewModel.saveMaslachTest(ee, dp, pa)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Отправить результаты")
                }
            }
        }
    }
}
