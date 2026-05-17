package com.quiz.app

import androidx.room.*

@Dao
interface QuizDao {
    @Insert
    suspend fun insertResult(result: QuizResult)

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<QuizResult>

    @Query("SELECT COUNT(*) FROM quiz_results")
    suspend fun getTotalAttendance(): Int
}