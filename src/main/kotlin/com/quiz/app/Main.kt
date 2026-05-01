package com.quiz.app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.quiz.app.R

class Main : AppCompatActivity() {

    // Global variables to track the quiz state
    private var questionList: Array<Question> = arrayOf()
    private var currentIndex = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find Containers (The "Rooms" of our app)
        val menuContainer = findViewById<LinearLayout>(R.id.menuContainer)
        val quizContainer = findViewById<LinearLayout>(R.id.quizContainer)
        val scoreContainer = findViewById<LinearLayout>(R.id.scoreContainer)

        // 2. Find UI Elements inside those containers
        val startQuizBtn = findViewById<Button>(R.id.startQuizBtn)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val restartBtn = findViewById<Button>(R.id.restartBtn)
        val backToMenuBtn = findViewById<Button>(R.id.backToMenuBtn)
        val menuExitBtn = findViewById<Button>(R.id.menuExitBtn)

        val questionDisplay = findViewById<TextView>(R.id.questionText)
        val optionsGroup = findViewById<RadioGroup>(R.id.optionsGroup)
        val finalScoreText = findViewById<TextView>(R.id.finalScoreText)

        // Put the RadioButtons in a list so we can loop through them easily
        val rButtons = listOf<RadioButton>(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3)
        )

        // Load the questions from assets/questions.json immediately
        loadQuestions()

        /**
         * Switches visibility between Menu, Quiz, and Score screens.
         */
        fun updateUI(state: String) {
            menuContainer.visibility = if (state == "MENU") View.VISIBLE else View.GONE
            quizContainer.visibility = if (state == "QUIZ") View.VISIBLE else View.GONE
            scoreContainer.visibility = if (state == "SCORE") View.VISIBLE else View.GONE
        }

        /**
         * Clears old selection and updates the screen with new question data.
         */
        fun showQuestion() {
            // THE FIX: Clear selection BEFORE showing new data to avoid flicker
            optionsGroup.clearCheck()

            // Add a small fade-in animation for a "premium" feel
            quizContainer.alpha = 0f

            val q = questionList[currentIndex]
            questionDisplay.text = q.text

            // Map JSON options to the RadioButtons
            for (i in rButtons.indices) {
                rButtons[i].text = q.options[i]
            }

            quizContainer.animate().alpha(1f).setDuration(250).start()
        }

        // --- BUTTON ACTIONS ---

        // Start Quiz from Landing Page
        startQuizBtn.setOnClickListener {
            currentIndex = 0
            score = 0
            updateUI("QUIZ")
            showQuestion()
        }

        // Check answer and move to next question
        submitBtn.setOnClickListener {
            val selectedId = optionsGroup.checkedRadioButtonId

            if (selectedId != -1) {
                // Determine which index was selected
                val selectedBtn = findViewById<RadioButton>(selectedId)
                val selectedIndex = rButtons.indexOf(selectedBtn)

                // Compare with correct answer from JSON
                if (selectedIndex == questionList[currentIndex].correctAnswer) {
                    score++
                }

                currentIndex++

                // If more questions exist, show next. Otherwise, show score.
                if (currentIndex < questionList.size) {
                    showQuestion()
                } else {
                    finalScoreText.text = "You Scored\n$score / ${questionList.size}"
                    updateUI("SCORE")
                }
            } else {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
            }
        }

        // Restart Quiz from Score Page
        restartBtn.setOnClickListener {
            // We just trigger the "Start Quiz" logic again
            startQuizBtn.performClick()
        }

        // Go back to the very first screen
        backToMenuBtn.setOnClickListener {
            updateUI("MENU")
        }

        // Close the app entirely
        menuExitBtn.setOnClickListener {
            finish()
        }
    }

    /**
     * Reads the questions.json file from the assets folder and converts it to Kotlin objects.
     */
    private fun loadQuestions() {
        try {
            val jsonString = assets.open("questions.json").bufferedReader().use { it.readText() }
            questionList = Gson().fromJson(jsonString, Array<Question>::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading questions!", Toast.LENGTH_LONG).show()
        }
    }
}