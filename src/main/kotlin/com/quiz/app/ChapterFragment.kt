package com.quiz.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.widget.AdapterView

data class ChapterConfig(
    val id: String,
    val name: String
)
data class PracticeMode(
    val id: String,
    val name: String,
    val details: String,
    val question_count: Int,  // New field
    val timer_seconds: Int,   // New field
    val icon_color: String
) {
    override fun toString(): String = name // Spinner uses toString() for display
}
data class PracticeModeResponse(val practice_modes: List<PracticeMode>)

class ChapterFragment : Fragment() {

    private var userStandard: String = ""
    private var userSubject: String = ""
    private var formattedStd:String? = null
    private var currentIndex = 0
    private var currentChapter = 0
    private var modes: List<PracticeMode> = listOf()
    private var totalQuestions = 0
    private var timeLimit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the data from the "arguments" property
        arguments?.let {
            userStandard = it.getString("STD").toString()
            userSubject = it.getString("SUBJECT").toString()
            formattedStd = userStandard?.replace(" ", "")?.lowercase()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This links the fragment to its XML layout
        return inflater.inflate(R.layout.fragment_chapters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPracticeModeJson()
        setupQuestionMode()
        setupChapterGrid(view)
    }
    private fun loadPracticeModeJson() {
        val jsonString = requireContext().assets.open("config/practice_modes.json")
            .bufferedReader().use { it.readText() }

        val type = object : TypeToken<PracticeModeResponse>() {}.type
        modes = Gson().fromJson<PracticeModeResponse>(jsonString, type).practice_modes
    }

    private fun setupQuestionMode() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            modes
        )
        val modeSpinner = view?.findViewById<Spinner>(R.id.modeSpinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner?.adapter = adapter

        val modeDescription = view?.findViewById<TextView>(R.id.modeDescription)

        modeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = modes[position]
                modeDescription?.text = "Details: ${selected.details}"
                totalQuestions = selected.question_count
                timeLimit = selected.timer_seconds

                if (timeLimit > 0) {
                    // Logic for timed mode (e.g., show a clock icon)
                    println("User has ${timeLimit / 60} minutes to finish $totalQuestions questions.")
                } else {
                    // Logic for untimed mode
                    println("User can take their time with $totalQuestions questions.")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun setupChapterGrid(view: View) {
        try {
            val fileName = "${formattedStd}_${userSubject}.json"
            val jsonString = requireContext().assets.open(fileName).bufferedReader().use { it.readText() }
            val chapterList: List<ChapterConfig> = Gson().fromJson(jsonString, object : TypeToken<List<ChapterConfig>>() {}.type)
            println("MMM chapterList: ${chapterList}")
            // These MUST match the IDs in your activity_main.xml
            val chapterIds = arrayOf(R.id.ch1, R.id.ch2, R.id.ch3, R.id.ch4,
                R.id.ch5)

            for (i in chapterList.indices) {
                if (i < chapterIds.size) {
                    val chapterCard = view.findViewById<MaterialCardView>(chapterIds[i])
                    val chapterContainer = view.findViewById<GridLayout>(R.id.chapterContainer)
                    val title = chapterCard.findViewById<TextView>(R.id.chText)

                    title.text = chapterList[i].name

                    chapterCard.setOnClickListener {
                        if (chapterList.isNotEmpty()) {
                            currentChapter = chapterList[i].id.toInt()
                            currentIndex = 0
//                            val selectedCount = spinner.selectedItem.toString()

                            // Tell the Activity to swap fragments
                            (activity as? QuizActivity)?.switchToQuestions(currentChapter, totalQuestions, timeLimit)

                        } else {
                            Toast.makeText(requireContext(), "Error loading Chapters!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Check <>.json and XML IDs!", Toast.LENGTH_SHORT).show()
        }
    }


}