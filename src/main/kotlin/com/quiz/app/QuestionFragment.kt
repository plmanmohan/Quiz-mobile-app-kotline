package com.quiz.app

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuestionFragment : Fragment() {

    private var userStandard: String = ""
    private var userSubject: String = ""
    private var formattedStd: String? = null
    private var currentChapter = 0

    private var currentIndex = 0
    private var questionList: List<Question> = listOf()
    private val userAnswers = mutableMapOf<Int, Int>()

    private var MAX_QUESTIONS = 5
    private var MAX_TIME_MINUTES = 5 // From arguments
    private var countDownTimer: CountDownTimer? = null

    private lateinit var timerText: TextView
    private lateinit var questionDisplay: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var prevBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var rButtons: List<RadioButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userStandard = it.getString("STD", "")
            userSubject = it.getString("SUBJECT", "")
            currentChapter = it.getInt("CHAPTER_ID", 1)
            MAX_QUESTIONS = it.getInt("TOTAL_QUESTION", 5)
            MAX_TIME_MINUTES = it.getInt("TIME_LIMIT", 5) // Assuming this is in minutes
            formattedStd = userStandard.replace(" ", "").lowercase()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_questions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI References
        timerText = view.findViewById(R.id.timerText)
        questionDisplay = view.findViewById(R.id.questionText)
        optionsGroup = view.findViewById(R.id.optionsGroup)
        progressBar = view.findViewById(R.id.quizProgress)
        nextBtn = view.findViewById(R.id.nextBtn)
        prevBtn = view.findViewById(R.id.prevBtn)

        rButtons = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3)
        )
        val isDailyPractice =
            arguments?.getBoolean(
                "IS_DAILY_PRACTICE",
                false
            ) ?: false
        if (isDailyPractice) {

            val json =
                arguments?.getString("QUESTION_LIST")

            if (json != null) {

                val type =
                    object : TypeToken<List<Question>>() {}.type

                questionList =
                    Gson().fromJson(json, type)

                setupQuiz()
            }

        } else {

            // existing chapter logic
            loadQuestionsFromChapter(view)
        }
        // setupQuiz(view)
        startGlobalTimer() // Start the timer ONCE for the whole exam
    }

    private fun startGlobalTimer() {
        // Safety check: If MAX_TIME_MINUTES is 0 or negative, default to 5 minutes
        val timeInMinutes = if (MAX_TIME_MINUTES > 0) MAX_TIME_MINUTES else 5
        val totalMillis = (timeInMinutes * 60 * 1000).toLong()

        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = (millisUntilFinished / 1000) % 60

                // Only update text if the fragment is still visible
                if (isAdded) {
                    timerText.text = String.format("Time: %02d:%02d", mins, secs)
                }
            }

            override fun onFinish() {
                // ONLY call this when the clock actually hits 00:00
                if (isAdded) {
                    calculateScore()
                }
            }
        }.start()
    }

    private fun setupQuiz() {
        progressBar.max = questionList.size
        // Button Listeners
        nextBtn.setOnClickListener {
            saveCurrentAnswer()
            if (currentIndex < questionList.size - 1) {
                currentIndex++
                updateUI()
            } else {
                calculateScore() // Final Submission
            }
        }

        prevBtn.setOnClickListener {
            saveCurrentAnswer()
            if (currentIndex > 0) {
                currentIndex--
                updateUI()
            }
        }

        updateUI()
    }
    private fun loadQuestionsFromChapter(view: View) {
        val fileName = "questions_${formattedStd}_ch${currentChapter}_${userSubject}.json"
        try {
            val jsonString = requireContext().assets.open(fileName).bufferedReader().use { it.readText() }
            val allQuestions: List<Question> = Gson().fromJson(jsonString, object : TypeToken<List<Question>>() {}.type)

            questionList = allQuestions.shuffled().take(MAX_QUESTIONS)

            val chapterTitle = view.findViewById<TextView>(R.id.chapterTitle)
            chapterTitle.text = "Chapter $currentChapter: ${userSubject.uppercase()}"

            setupQuiz()


        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading quiz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentAnswer() {
        val selectedId = optionsGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedBtn = view?.findViewById<RadioButton>(selectedId)
            userAnswers[currentIndex] = rButtons.indexOf(selectedBtn)
        }
    }

    private fun updateUI() {
        if (currentIndex !in questionList.indices) return

        val q = questionList[currentIndex]
        questionDisplay.text = q.text
        progressBar.progress = currentIndex + 1

        optionsGroup.clearCheck()
        for (i in rButtons.indices) {
            rButtons[i].text = q.options[i]
            if (userAnswers[currentIndex] == i) {
                rButtons[i].isChecked = true
            }
        }

        prevBtn.visibility = if (currentIndex == 0) View.INVISIBLE else View.VISIBLE
        nextBtn.text = if (currentIndex == questionList.size - 1) "Finish" else "Next"
    }
    private fun calculateScore() {
        countDownTimer?.cancel()
        saveCurrentAnswer()

        var score = 0
        questionList.forEachIndexed { index, question ->
            if (userAnswers[index] == question.correctAnswer) {
                score++
            }
        }

        val resultMessage = "Exam Finished!\n\nYour Score: $score / ${questionList.size}"
        // --- SAVE TO DATABASE ---
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val currentDateTime = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
            val resultEntry = QuizResult(
                standard = userStandard, subject = userSubject,
                chapter = currentChapter, score = score,
                totalQuestions = questionList.size, createdAt = currentDateTime)
            db.quizDao().insertResult(resultEntry)
        }
        view?.let { fragmentView ->
            val container = fragmentView.findViewById<LinearLayout>(R.id.quizContainer)
            // Access context safely in a Fragment
            val context = requireContext()

            // 1. Get colors dynamically from the theme
            val typedValue = android.util.TypedValue()

            // Get Background Color
            context.theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
            val bgColor = typedValue.data

            // Get Text Color (colorOnSurface)
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            val textColor = typedValue.data
            container?.apply {
                removeAllViews()
                // Ensure the container itself isn't black
                setBackgroundColor(bgColor)
                gravity = android.view.Gravity.CENTER
                orientation = LinearLayout.VERTICAL
            }

            val scoreTv = TextView(requireContext()).apply {
                text = resultMessage
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f)
                setTextColor(textColor)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val restartBtn = Button(requireContext()).apply {
                text = "Back to Chapters"

                // Fix: Explicitly set text color so it's not hidden
                setTextColor(textColor) // A nice blue color

                // Fix: Use null background to completely remove any default button shading/black boxes
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 60
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }

                setOnClickListener {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }

            container?.addView(scoreTv)
            container?.addView(restartBtn)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}