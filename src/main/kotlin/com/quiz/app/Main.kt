package com.quiz.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private var selectedStandard: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Setup Theme Switch
        val themeSwitch = findViewById<SwitchMaterial>(R.id.themeSwitch)
        // Set switch state based on current theme
        themeSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // 2. Setup Standard Selection Dropdown
        val standards = arrayOf("Standard 1", "Standard 2", "Standard 3", "Standard 4", "Standard 5")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, standards)
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.standardSelector)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedStandard = standards[position]
        }

        // 3. Setup Cards
        val cardQuiz = findViewById<MaterialCardView>(R.id.cardQuiz)
        val cardFormula = findViewById<MaterialCardView>(R.id.cardFormula)
        // (Add other 6 card finds here if needed for specific logic)

        cardQuiz.setOnClickListener {
            if (selectedStandard.isEmpty()) {
                Toast.makeText(this, "Please select your Standard first!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, QuizActivity::class.java)
                // Pass the selected standard to the Quiz page
                intent.putExtra("SELECTED_STANDARD", selectedStandard)
                startActivity(intent)
            }
        }

        cardFormula.setOnClickListener { showToast("Math Formulas coming soon!") }
        findViewById<MaterialCardView>(R.id.cardTable).setOnClickListener { showToast("Tables coming soon!") }
        findViewById<MaterialCardView>(R.id.cardRiddle).setOnClickListener { showToast("Riddles coming soon!") }
        findViewById<MaterialCardView>(R.id.cardConverter).setOnClickListener { showToast("Converter coming soon!") }
        findViewById<MaterialCardView>(R.id.cardSymbols).setOnClickListener { showToast("Symbols coming soon!") }
        findViewById<MaterialCardView>(R.id.cardNotes).setOnClickListener { showToast("Notes coming soon!") }
        findViewById<MaterialCardView>(R.id.cardCreator).setOnClickListener { showToast("Creator coming soon!") }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}