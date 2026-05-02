package com.quiz.app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson


class QuizActivity : AppCompatActivity() {

    private var questionList: Array<Question> = arrayOf()
    private var currentIndex = 0
    private var score = 0
    private var userStandard: String = ""
    private var userSubject: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 1. Get Data from Intent
        userStandard = intent.getStringExtra("STD") ?: "Standard 1"
        userSubject = intent.getStringExtra("SUBJECT") ?: "math"

        // 2. Find UI Elements
        val menuContainer = findViewById<LinearLayout>(R.id.menuContainer)
        val quizContainer = findViewById<LinearLayout>(R.id.quizContainer)
        val scoreContainer = findViewById<LinearLayout>(R.id.scoreContainer)
        val startQuizBtn = findViewById<Button>(R.id.startQuizBtn)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val restartBtn = findViewById<Button>(R.id.restartBtn)
        val backToMenuBtn = findViewById<Button>(R.id.backToMenuBtn)
        val menuExitBtn = findViewById<Button>(R.id.menuExitBtn)
        val questionDisplay = findViewById<TextView>(R.id.questionText)
        val optionsGroup = findViewById<RadioGroup>(R.id.optionsGroup)
        val finalScoreText = findViewById<TextView>(R.id.finalScoreText)
        val quizTitleText = findViewById<TextView>(R.id.quizTitleText)

        val rButtons = listOf<RadioButton>(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3)
        )

        // Set the Title
        quizTitleText.text = "${userSubject.replaceFirstChar { it.uppercase() }} - $userStandard"

        // Load Questions
        loadQuestions()

        // 3. UI Logic Functions
        fun updateUI(state: String) {
            menuContainer.visibility = if (state == "MENU") View.VISIBLE else View.GONE
            quizContainer.visibility = if (state == "QUIZ") View.VISIBLE else View.GONE
            scoreContainer.visibility = if (state == "SCORE") View.VISIBLE else View.GONE
        }

        fun showQuestion() {
            optionsGroup.clearCheck()
            quizContainer.alpha = 0f
            val q = questionList[currentIndex]
            questionDisplay.text = q.text
            for (i in rButtons.indices) {
                rButtons[i].text = q.options[i]
            }
            quizContainer.animate().alpha(1f).setDuration(250).start()
        }

        // 4. Click Listeners
        startQuizBtn.setOnClickListener {
            if (questionList.isNotEmpty()) {
                currentIndex = 0
                score = 0
                updateUI("QUIZ")
                showQuestion()
            } else {
                Toast.makeText(this, "No questions found!", Toast.LENGTH_SHORT).show()
            }
        }

        submitBtn.setOnClickListener {
            val selectedId = optionsGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedBtn = findViewById<RadioButton>(selectedId)
                val selectedIndex = rButtons.indexOf(selectedBtn)
                if (selectedIndex == questionList[currentIndex].correctAnswer) score++

                currentIndex++
                if (currentIndex < questionList.size) {
                    showQuestion()
                } else {
                    finalScoreText.text = "Final Score:\n$score / ${questionList.size}"
                    updateUI("SCORE")
                }
            } else {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
            }
        }

        restartBtn.setOnClickListener { startQuizBtn.performClick() }
        backToMenuBtn.setOnClickListener { finish() }
        menuExitBtn.setOnClickListener { finishAffinity() }
    }

    private fun loadQuestions() {
        // Filename format: questions_standard1_math.json
        val formattedStd = userStandard.replace(" ", "").lowercase()
        val fileName = "questions_${formattedStd}_${userSubject}.json"
        val assetManager = assets
        val files = assetManager.list("")
        files?.forEach { println("ASSET_DEBUG: Found file: $it") }
        println("DEBUG_QUIZ: Searching for file: $fileName")
        try {
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            questionList = Gson().fromJson(jsonString, Array<Question>::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not load: $fileName", Toast.LENGTH_LONG).show()
        }
    }
}