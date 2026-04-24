package com.ahmetkaraaslan.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ahmetkaraaslan.labx.model.Question
import com.ahmetkaraaslan.labx.model.Quiz
import com.ahmetkaraaslan.labx.ui.theme.*
import com.ahmetkaraaslan.labx.utils.*

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
    // FeedbackManager içindeki loadQuizzesFromJson fonksiyonunu kullanıyoruz
    val quiz = remember { loadQuizzesFromJson(context).firstOrNull() }
    
    if (quiz != null) {
        TestScreen(quiz = quiz, onFinish = onFinishActivity)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Quiz verisi yüklenemedi.", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(quiz: Quiz, onFinish: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TestViewModel = viewModel(factory = TestViewModelFactory(quiz))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.test_selection), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        playClickFeedback(context)
                        onFinish()
                    }) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LabX_Background_Gradient)
                .padding(padding)
        ) {
            if (uiState.isQuizFinished) {
                QuizResultScreen(
                    score = uiState.score,
                    total = uiState.totalQuestions,
                    onRestart = {
                        playClickFeedback(context)
                        onFinish()
                    },
                    onFinish = {
                        playClickFeedback(context)
                        onFinish()
                    }
                )
            } else {
                QuizQuestionScreen(
                    question = uiState.currentQuestion,
                    currentIndex = uiState.currentQuestionIndex,
                    totalQuestions = uiState.totalQuestions,
                    selectedOption = uiState.selectedOptionIndex,
                    isAnswered = uiState.isAnswered,
                    onOptionSelected = { index ->
                        playClickFeedback(context)
                        viewModel.onEvent(TestEvent.AnswerSelected(index))
                    },
                    onNextQuestion = {
                        playClickFeedback(context)
                        viewModel.onEvent(TestEvent.NextQuestionClicked)
                    }
                )
            }
        }
    }
}

@Composable
fun QuizQuestionScreen(
    question: Question,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    isAnswered: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = (currentIndex + 1).toFloat() / totalQuestions,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
            color = LabX_Primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${currentIndex + 1} / $totalQuestions",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = question.questionText,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        question.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == index
            val backgroundColor = when {
                isAnswered && index == question.correctAnswerIndex -> LabX_Success
                isAnswered && isSelected && index != question.correctAnswerIndex -> LabX_Error
                isSelected -> Color.White
                else -> LabX_Button_Transparent
            }
            val contentColor = if (isSelected || (isAnswered && (index == question.correctAnswerIndex || (isSelected && index != question.correctAnswerIndex)))) {
                if (backgroundColor == Color.White) LabX_Primary else Color.White
            } else {
                Color.White
            }

            Button(
                onClick = { if (!isAnswered) onOptionSelected(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    contentColor = contentColor
                ),
                enabled = !isAnswered || isSelected || index == question.correctAnswerIndex
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        if (isAnswered) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = if (currentIndex < totalQuestions - 1) stringResource(id = R.string.next_question) else stringResource(id = R.string.finish_test),
                    color = LabX_Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, total: Int, onRestart: () -> Unit, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(id = R.string.test_completed), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${stringResource(id = R.string.your_score)} $score / $total",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Tekrarla", color = LabX_Primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text(stringResource(id = R.string.return_to_menu), color = Color.White)
        }
    }
}
