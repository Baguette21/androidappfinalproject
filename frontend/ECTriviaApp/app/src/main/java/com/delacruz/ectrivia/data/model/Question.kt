package com.ectrvia.ectrivia.data.model

data class Question(
    val id: Long,
    val questionText: String,
    val answers: List<Answer>,
    val questionOrder: Int,
    val correctAnswerIndex: Int = 0,
    val timerSeconds: Int
)

data class Answer(
    val index: Int,
    val text: String
)
