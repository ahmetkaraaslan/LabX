package com.ahmetkaraaslan.labx.model

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class Quiz(
    val id: Int,
    val title: String,
    val questions: List<Question>
)
