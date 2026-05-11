package com.quiz.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.get
import kotlin.text.get

class QuestionFragment : Fragment() {

    private var userStandard: String = ""
    private var userSubject: String = ""
    private var formattedStd:String? = null
    private var currentIndex = 0
    private var score = 0
    private var currentChapter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userStandard = it.getString("STD").toString()
            userSubject = it.getString("SUBJECT").toString()
            currentChapter = it.getInt("CHAPTER_ID", 1)
            formattedStd = userStandard.replace(" ", "").lowercase()
        }
    }

    // 2. Layout Setup (Mandatory for UI)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Just return the inflated layout, don't do logic here
        return inflater.inflate(R.layout.fragment_questions, container, false)
    }

    // 3. UI Logic (Best Practice)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadQuestions(view)
    }
    private fun loadQuestions(view: View) {
        // Filename format: questions_standard1_math.json
        val fileName = "questions_${formattedStd}_ch${currentChapter}_${userSubject}.json"
        try {
            val jsonString = requireContext().assets.open(fileName).bufferedReader().use { it.readText() }
            val questionList: List<Question> = Gson().fromJson(jsonString, object : TypeToken<List<Question>>() {}.type)
            val questionDisplay = view.findViewById<TextView>(R.id.questionText)
            val quizContainer = view.findViewById<LinearLayout>(R.id.quizContainer)
            // val scoreContainer = view.findViewById<LinearLayout>(R.id.scoreContainer)
            val optionsGroup = view.findViewById<RadioGroup>(R.id.optionsGroup)
            val rButtons = listOf<RadioButton>(
                view.findViewById(R.id.option1),
                view.findViewById(R.id.option2),
                view.findViewById(R.id.option3)
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
            showQuestion()
            val submitBtn = view.findViewById<Button>(R.id.submitBtn)

            val finalScoreText = view.findViewById<TextView>(R.id.finalScoreText)

            // 3. UI Logic Functions

            submitBtn.setOnClickListener {
                val selectedId = optionsGroup.checkedRadioButtonId
                if (selectedId != -1) {
                    val selectedBtn = view.findViewById<RadioButton>(selectedId)
                    val selectedIndex = rButtons.indexOf(selectedBtn)
                    if (selectedIndex == questionList[currentIndex].correctAnswer) score++

                    currentIndex++
                    if (currentIndex < questionList.size) {
                        showQuestion()
                    } else {
                        finalScoreText.text = "Final Score:\n$score / ${questionList.size}"

                    }
                } else {
                    Toast.makeText(requireContext(), "Please select an answer!", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Could not load: $fileName", Toast.LENGTH_LONG).show()
        }
    }

}