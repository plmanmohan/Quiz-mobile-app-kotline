package com.quiz.app

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val chapter: String,
    val standard: String
)