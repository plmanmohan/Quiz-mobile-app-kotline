package com.quiz.app

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: Int
)