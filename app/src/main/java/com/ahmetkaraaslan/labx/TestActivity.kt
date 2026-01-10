package com.ahmetkaraaslan.labx

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmetkaraaslan.labx.model.Quiz
import com.ahmetkaraaslan.labx.ui.theme.*
import com.ahmetkaraaslan.labx.utils.*

// ViewModel Factory to pass the quiz to the ViewModel
class TestViewModelFactory(private val quiz: Quiz) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(quiz) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                TestNavigator { finish() }
            }
        }
    }
}

@Composable
fun TestNavigator(onFinishActivity: () -> Unit) {
    val context = LocalContext.current
    val allQuizzes = remember { loadQuizzesFromJson(context) }
    var completedQuizIds by remember { mutableStateOf(loadCompletedQuizzes(context)) }
    var currentQuiz by remember { mutableStateOf<Quiz?>(null) }

    val onQuizComplete: (Int) -> Unit = { quizId ->
        val newCompletedIds = completedQuizIds + quizId
        saveCompletedQuizzes(context, newCompletedIds)
        completedQuizIds = newCompletedIds
        currentQuiz = null
    }

    val selectedQuiz = currentQuiz

    if (selectedQuiz == null) {
        TestSelectionScreen(
            quizzes = allQuizzes,
            completedIds = completedQuizIds,
            onQuizSelected = {
                playClickFeedback(context)
                currentQuiz = it
            },
            onBackPressed = { 
                playClickFeedback(context)
                onFinishActivity()
            }
        )
    } else {
        // ÖNEMLİ: Her test için farklı ViewModel key'i kullan (test ID'sine göre)
        // Böylece önceki test'in state'i yeni teste karışmaz
        val viewModel: TestViewModel = viewModel(
            key = "test_${selectedQuiz.id}",
            factory = TestViewModelFactory(selectedQuiz)
        )
        QuizScreen(
            viewModel = viewModel,
            onComplete = { 
                onQuizComplete(selectedQuiz.id)
                // Test tamamlandığında state temizle
                // currentQuiz = null yapıldığında Compose otomatik olarak ViewModel'i dispose eder
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSelectionScreen(
    quizzes: List<Quiz>,
    completedIds: Set<Int>,
    onQuizSelected: (Quiz) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.test_selection), color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = onBackPressed) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.back), tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LabX_Background_Gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quizzes, key = { it.id }) { quiz ->
                val isUnlocked = quiz.id == 1 || (quiz.id - 1) in completedIds
                val alpha = if (isUnlocked) 1f else 0.6f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .background(LabX_Button_Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = isUnlocked) { onQuizSelected(quiz) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = quiz.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    when {
                        quiz.id in completedIds -> {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = stringResource(id = R.string.completed), tint = LabX_Success)
                        }
                        !isUnlocked -> {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = stringResource(id = R.string.locked), tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizScreen(viewModel: TestViewModel, onComplete: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isQuizFinished) {
        ResultScreen(score = uiState.score, totalQuestions = uiState.totalQuestions, onDone = onComplete)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LabX_Background_Gradient)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = (uiState.currentQuestionIndex + 1) / uiState.totalQuestions.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = uiState.currentQuestion.questionText,
                    color = Color.White, 
                    fontSize = 20.sp, 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                uiState.currentQuestion.options.forEachIndexed { index, option ->
                    AnswerOption(
                        optionText = option,
                        index = index,
                        isSelected = index == uiState.selectedOptionIndex,
                        isCorrect = index == uiState.currentQuestion.correctAnswerIndex,
                        isAnswered = uiState.isAnswered,
                        onOptionSelected = { selectedIndex ->
                            val isCorrect = selectedIndex == uiState.currentQuestion.correctAnswerIndex
                            if (isCorrect) playSuccessFeedback(context) else playErrorFeedback(context)
                            viewModel.onEvent(TestEvent.AnswerSelected(selectedIndex))
                        }
                    )
                }
            }
            Button(
                onClick = { viewModel.onEvent(TestEvent.NextQuestionClicked) },
                enabled = uiState.isAnswered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(if (uiState.currentQuestionIndex < uiState.totalQuestions - 1) R.string.next_question else R.string.finish_test))
            }
        }
    }
}

@Composable
fun AnswerOption(
    optionText: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    val (borderColor, backgroundColor) = when {
        !isAnswered -> LabX_White_50 to Color.Transparent
        isSelected && !isCorrect -> LabX_Error to LabX_Error_Bg
        isCorrect -> LabX_Success to LabX_Success_Bg
        else -> LabX_White_50 to Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isAnswered) { onOptionSelected(index) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(optionText, color = Color.White, fontSize = 16.sp)
    }
}


@Composable
fun ResultScreen(score: Int, totalQuestions: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().background(LabX_Background_Gradient).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(id = R.string.test_completed), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(id = R.string.your_score), fontSize = 24.sp, color = LabX_White_80)
        Text("$score / $totalQuestions", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = {
            playClickFeedback(context)
            onDone()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(id = R.string.return_to_menu))
        }
    }
}
