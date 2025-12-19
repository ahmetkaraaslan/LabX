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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.model.Quiz
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.*

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                TestNavigator()
            }
        }
    }
}

@Composable
fun TestNavigator() {
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

    if (currentQuiz == null) {
        TestSelectionScreen(
            quizzes = allQuizzes,
            completedIds = completedQuizIds,
            onQuizSelected = { quiz ->
                vibrate(context, 50)
                playSound(context, R.raw.click_sound)
                currentQuiz = quiz
            },
            onBackPressed = { (context as? ComponentActivity)?.finish() }
        )
    } else {
        QuizScreen(
            quiz = currentQuiz!!,
            onComplete = { onQuizComplete(currentQuiz!!.id) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSelectionScreen(quizzes: List<Quiz>, completedIds: Set<Int>, onQuizSelected: (Quiz) -> Unit, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Seçimi", color = Color.White) },
                navigationIcon = { IconButton(onClick = {
                    vibrate(context, 50)
                    playSound(context, R.raw.click_sound)
                    onBackPressed()
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(verticalGradientBrush).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quizzes, key = { it.id }) { quiz ->
                val isUnlocked = quiz.id == 1 || (quiz.id - 1) in completedIds
                val alpha = if (isUnlocked) 1f else 0.6f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .clickable(enabled = isUnlocked) { onQuizSelected(quiz) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = quiz.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    when {
                        quiz.id in completedIds -> {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Tamamlandı", tint = Color(0xFF4CAF50))
                        }
                        !isUnlocked -> {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Kilitli", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizScreen(quiz: Quiz, onComplete: () -> Unit) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var showResultScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentQuestion = quiz.questions[currentQuestionIndex]

    if (showResultScreen) {
        ResultScreen(score = score, totalQuestions = quiz.questions.size, onDone = onComplete)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1) / quiz.questions.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Text(
                    text = currentQuestion.questionText,
                    color = Color.White, 
                    fontSize = 20.sp, 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                currentQuestion.options.forEachIndexed { index, option ->
                    val isCorrect = index == currentQuestion.correctAnswerIndex
                    val isSelected = index == selectedOptionIndex
                    val borderColor = when {
                        !isAnswered -> Color.White.copy(alpha = 0.5f)
                        isSelected && isCorrect -> Color(0xFF4CAF50)
                        isSelected && !isCorrect -> Color.Red
                        else -> Color.White.copy(alpha = 0.5f)
                    }
                    val backgroundColor = when {
                        !isAnswered -> Color.Transparent
                        isSelected && isCorrect -> Color(0x334CAF50)
                        isSelected && !isCorrect -> Color(0x33FF0000)
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAnswered) { 
                                selectedOptionIndex = index
                                isAnswered = true
                                if (isCorrect) {
                                    score++
                                    playSound(context, R.raw.success_sound)
                                    vibrate(context, 500)
                                } else {
                                    playSound(context, R.raw.error_sound)
                                    vibrate(context, 1000)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
            Button(
                onClick = {
                    if (currentQuestionIndex < quiz.questions.size - 1) {
                        currentQuestionIndex++
                        isAnswered = false
                        selectedOptionIndex = null
                    } else {
                        showResultScreen = true
                    }
                },
                enabled = isAnswered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (currentQuestionIndex < quiz.questions.size - 1) "Sonraki Soru" else "Testi Bitir")
            }
        }
    }
}

@Composable
fun ResultScreen(score: Int, totalQuestions: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Test Tamamlandı!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Skorun:", fontSize = 24.sp, color = Color.White.copy(alpha = 0.8f))
        Text("$score / $totalQuestions", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = {
            vibrate(context, 50)
            playSound(context, R.raw.click_sound)
            onDone()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Menüye Dön")
        }
    }
}
