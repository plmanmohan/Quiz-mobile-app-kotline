package com.quiz.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial

// Configuration for Subject

data class SubjectConfig(
    val id: String,
    val name: String,
    val icon: String  // Add this line
)

class MainActivity : AppCompatActivity() {

    private var selectedStandard: String = ""
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        selectedStandard = prefs.getString("user_std", "") ?: ""
// --- THEME TOGGLE CODE START ---
        val themeSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.themeSwitch)

        // Check if Dark Mode is currently active to set the switch position
        val isDark = prefs.getBoolean("is_dark", true)
        themeSwitch.isChecked = isDark

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                prefs.edit().putBoolean("is_dark", true).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                prefs.edit().putBoolean("is_dark", false).apply()
            }
        }
        // --- THEME TOGGLE CODE END ---
        setupDropdown()

        // Show popup only if standard is not yet selected
        if (selectedStandard.isEmpty()) {
            showStandardPopup()
        }

        setupSubjectsGrid()
    }

    private fun setupDropdown() {
        val standards = arrayOf("Standard 4", "Standard 5", "Standard 6", "Standard 8")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, standards)
        val selector = findViewById<AutoCompleteTextView>(R.id.standardSelector)

        selector.setAdapter(adapter)
        if (selectedStandard.isNotEmpty()) selector.setText(selectedStandard, false)

        selector.setOnItemClickListener { _, _, position, _ ->
            saveStandard(standards[position])
            val newSelection = adapter.getItem(position).toString()
            saveStandard(newSelection)

            // 3. IMPORTANT: Reset the text again with 'false' filter to keep list full
            selector.setText(newSelection, false)
        }
    }

    private fun showStandardPopup() {
        val standards = arrayOf("Standard 4", "Standard 5", "Standard 6", "Standard 8")
        AlertDialog.Builder(this)
            .setTitle("Choose Your Class")
            .setItems(standards) { _, which ->
                saveStandard(standards[which])
                findViewById<AutoCompleteTextView>(R.id.standardSelector).setText(standards[which], false)
            }
            .setCancelable(false)
            .show()
    }

    private fun saveStandard(std: String) {
        selectedStandard = std
        prefs.edit().putString("user_std", std).apply()
    }

    private fun setupSubjectsGrid() {
        try {
            val jsonString = assets.open("subjects.json").bufferedReader().use { it.readText() }
            val subjectList: List<SubjectConfig> = Gson().fromJson(jsonString, object : TypeToken<List<SubjectConfig>>() {}.type)

            // These MUST match the IDs in your activity_main.xml
            val cardIds = arrayOf(R.id.card1, R.id.card2, R.id.card3, R.id.card4,
                R.id.card5, R.id.card6, R.id.card7, R.id.card8)

            for (i in subjectList.indices) {
                if (i < cardIds.size) {
                    val card = findViewById<MaterialCardView>(cardIds[i])

                    // FIX: Find the TextView specifically INSIDE this card
                    // Even if they have the same ID name, calling it ON the card object
                    // limits the search to just that one box.
                    val title = card.findViewById<TextView>(R.id.cardText)
                    val iconView = card.findViewById<ImageView>(R.id.cardIcon)

                    title.text = subjectList[i].name
                    val resourceId = resources.getIdentifier(subjectList[i].icon, "drawable", packageName)
                    if (resourceId != 0) {
                        iconView.setImageResource(resourceId)
                    } else {
                        // Optional: set a default icon if the specific one is missing
                        iconView.setImageResource(android.R.drawable.ic_menu_help)
                    }
                    card.setOnClickListener {
                        if (selectedStandard.isEmpty()) {
                            showStandardPopup()
                        } else {
                            val intent = Intent(this, QuizActivity::class.java)
                            intent.putExtra("STD", selectedStandard)
                            intent.putExtra("SUBJECT", subjectList[i].id) // This sends "science", "gk", etc.
                            startActivity(intent)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Check subjects.json and XML IDs!", Toast.LENGTH_SHORT).show()
        }
    }
}