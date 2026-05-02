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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Receive the standard from the previous screen
        userStandard = intent.getStringExtra("SELECTED_STANDARD") ?: "Standard 1"

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

        val rButtons = listOf<RadioButton>(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3)
        )

        // Update title to show user their selected class
        findViewById<TextView>(R.id.quizTitleText).text = "Quiz for $userStandard"

        loadQuestions()

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

        startQuizBtn.setOnClickListener {
            currentIndex = 0
            score = 0
            updateUI("QUIZ")
            showQuestion()
        }

        submitBtn.setOnClickListener {
            val selectedId = optionsGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedBtn = findViewById<RadioButton>(selectedId)
                val selectedIndex = rButtons.indexOf(selectedBtn)
                if (selectedIndex == questionList[currentIndex].correctAnswer) score++
                currentIndex++
                if (currentIndex < questionList.size) showQuestion()
                else {
                    finalScoreText.text = "Final Score for $userStandard:\n$score / ${questionList.size}"
                    updateUI("SCORE")
                }
            } else {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
            }
        }

        restartBtn.setOnClickListener { startQuizBtn.performClick() }
        backToMenuBtn.setOnClickListener { finish() } // Goes back to MainActivity grid
        menuExitBtn.setOnClickListener { finishAffinity() }
    }

    private fun loadQuestions() {
        try {
            // Logic: Load different files based on standard (e.g., questions_std1.json)
            val fileName = when(userStandard) {
                "Standard 1" -> "questions_std1.json"
                "Standard 2" -> "questions_std2.json"
                else -> "questions.json"
            }
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            questionList = Gson().fromJson(jsonString, Array<Question>::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading questions for $userStandard!", Toast.LENGTH_LONG).show()
        }
    }
}