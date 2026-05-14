package com.quiz.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class QuizActivity : AppCompatActivity() {
    private var userStandard: String = ""
    private var userSubject: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 1. Get Data from Intent
        userStandard = intent.getStringExtra("STD") ?: "Standard 1"
        userSubject = intent.getStringExtra("SUBJECT") ?: "math"

        // 2. Load the ChapterFragment for the first time
        if (savedInstanceState == null) { // Only load if the app isn't being restored from a rotation
            loadChapterFragment()
        }
    }

    private fun loadChapterFragment() {
        // 1. Create the fragment instance
        val fragment = ChapterFragment()

        // 2. Pass the data (Standard/Subject) to the fragment using a Bundle
        val bundle = Bundle()
        bundle.putString("STD", userStandard)
        bundle.putString("SUBJECT", userSubject)
        fragment.arguments = bundle

        // 3. Start the transaction to "plug it in"
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    // Note: No 'private' keyword here
    fun switchToQuestions(chapterId: Int, totalQuestions: Int, timeLimit: Int) {
        val fragment = QuestionFragment()

        // 1. Pack the "Suitcase" (Bundle)
        val bundle = Bundle()
        bundle.putInt("CHAPTER_ID", chapterId)
        bundle.putInt("TOTAL_QUESTION", totalQuestions)
        bundle.putInt("TIME_LIMIT", timeLimit)
        bundle.putString("STD", userStandard)   // Pass the standard too
        bundle.putString("SUBJECT", userSubject) // Pass the subject too
        fragment.arguments = bundle

        // 2. Perform the Swap
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            ) // Optional: Adds a nice smooth transition
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // CRITICAL: This allows the "Back" button to work!
            .commit()
    }
}