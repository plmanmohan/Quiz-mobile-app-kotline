package com.quiz.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ChapterConfig(
    val id: String,
    val name: String
)
class QuizActivity : AppCompatActivity() {

    private var questionList: Array<Question> = arrayOf()
    private var currentIndex = 0
    private var currentChapter = 0
    private var score = 0
    private var userStandard: String = ""
    private var userSubject: String = ""
    private var formattedStd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 1. Get Data from Intent
        userStandard = intent.getStringExtra("STD") ?: "Standard 1"
        userSubject = intent.getStringExtra("SUBJECT") ?: "math"
        formattedStd = userStandard.replace(" ", "").lowercase()

// 1. Initially show only the Chapter Grid
        updateUI("CHAPTERS")
        setupChapterGrid()
    }
    // Move updateUI outside of showQuestions so other functions can use it
    private fun updateUI(state: String) {
        val chapterContainer = findViewById<GridLayout>(R.id.chapterContainer)
        val quizContainer = findViewById<LinearLayout>(R.id.quizContainer)
        val scoreContainer = findViewById<LinearLayout>(R.id.scoreContainer)
        chapterContainer.visibility = if (state == "CHAPTERS") View.VISIBLE else View.GONE
        quizContainer.visibility = if (state == "QUIZ") View.VISIBLE else View.GONE
        scoreContainer.visibility = if (state == "SCORE") View.VISIBLE else View.GONE
    }
    private fun showQuestions() {
        val questionDisplay = findViewById<TextView>(R.id.questionText)
        val quizContainer = findViewById<LinearLayout>(R.id.quizContainer)
        val scoreContainer = findViewById<LinearLayout>(R.id.scoreContainer)
        val optionsGroup = findViewById<RadioGroup>(R.id.optionsGroup)
        val rButtons = listOf<RadioButton>(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3)
        )

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
        updateUI("QUIZ")
        showQuestion()
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        val finalScoreText = findViewById<TextView>(R.id.finalScoreText)

        // 3. UI Logic Functions

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
    }
    private fun loadQuestions() {
        // Filename format: questions_standard1_math.json

        val fileName = "questions_${formattedStd}_ch${currentChapter}_${userSubject}.json"
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

    private fun setupChapterGrid() {
        try {

            val jsonString = assets.open("${formattedStd}_${userSubject}.json").bufferedReader().use { it.readText() }
            val chapterList: List<ChapterConfig> = Gson().fromJson(jsonString, object : TypeToken<List<ChapterConfig>>() {}.type)

            // These MUST match the IDs in your activity_main.xml
            val chapterIds = arrayOf(R.id.ch1, R.id.ch2, R.id.ch3, R.id.ch4,
                R.id.ch5)

            for (i in chapterList.indices) {
                if (i < chapterIds.size) {
                    val chapterCard = findViewById<MaterialCardView>(chapterIds[i])
                    val chapterContainer = findViewById<GridLayout>(R.id.chapterContainer)
                    val title = chapterCard.findViewById<TextView>(R.id.chText)

                    title.text = chapterList[i].name

                    chapterCard.setOnClickListener {
                        if (chapterList.isNotEmpty()) {
                            currentChapter = chapterList[i].id.toInt()
                            currentIndex = 0
                            score = 0
                            // Load Questions
                            loadQuestions()
                            if (questionList.isNotEmpty()) {
                                updateUI("QUIZ")
                                showQuestions() // Start the quiz logic
                            }
                        } else {
                            Toast.makeText(this, "Error loading Chapters!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Check <>.json and XML IDs!", Toast.LENGTH_SHORT).show()
        }
    }
}