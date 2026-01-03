package com.ahmetkaraaslan.labx

import androidx.lifecycle.ViewModel
import com.ahmetkaraaslan.labx.model.Question
import com.ahmetkaraaslan.labx.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TestUiState(
    val currentQuestion: Question,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val isQuizFinished: Boolean = false
)

sealed interface TestEvent {
    data class AnswerSelected(val index: Int) : TestEvent
    object NextQuestionClicked : TestEvent
}

class TestViewModel(private val quiz: Quiz) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TestUiState(
            currentQuestion = quiz.questions.first(),
            totalQuestions = quiz.questions.size
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: TestEvent) {
        when (event) {
            is TestEvent.AnswerSelected -> handleAnswerSelection(event.index)
            is TestEvent.NextQuestionClicked -> moveToNextQuestion()
        }
    }

    private fun handleAnswerSelection(selectedIndex: Int) {
        if (_uiState.value.isAnswered) return

        val currentState = _uiState.value
        val isCorrect = selectedIndex == currentState.currentQuestion.correctAnswerIndex

        _uiState.update { state ->
            state.copy(
                isAnswered = true,
                selectedOptionIndex = selectedIndex,
                score = if (isCorrect) state.score + 1 else state.score
            )
        }
    }

    private fun moveToNextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < currentState.totalQuestions) {
            _uiState.update { state ->
                state.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = quiz.questions[nextIndex],
                    isAnswered = false,
                    selectedOptionIndex = null
                )
            }
        } else {
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }
}