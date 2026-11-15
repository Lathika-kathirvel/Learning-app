
package com.example.learningapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class AptitudeSetupActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var actTopic: MaterialAutoCompleteTextView
    private lateinit var actQuestionCount: MaterialAutoCompleteTextView

    private lateinit var cardEasy: MaterialCardView
    private lateinit var cardMedium: MaterialCardView
    private lateinit var cardHard: MaterialCardView

    private lateinit var tvEasy: TextView
    private lateinit var tvMedium: TextView
    private lateinit var tvHard: TextView

    private lateinit var btnStart: MaterialButton

    private var selectedDifficulty = "Medium"

    private val topics = arrayOf(
        "Quantitative Aptitude",
        "Logical Reasoning",
        "Verbal Ability",
        "Data Interpretation",
        "Puzzles"
    )

    private val questionCounts = arrayOf(
        "10 Questions",
        "15 Questions",
        "20 Questions"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContentView(R.layout.activity_aptitude_setup)

        initializeViews()
        setupWindowInsets()
        setupDropdowns()
        setupDifficulty()

        findViewById<MaterialCardView>(
            R.id.btnAptitudeBack
        ).setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            startAptitudePractice()
        }
    }

    private fun initializeViews() {

        scrollView = findViewById(R.id.aptitudeScrollView)

        actTopic = findViewById(R.id.actAptitudeTopic)

        actQuestionCount =
            findViewById(R.id.actQuestionCount)

        cardEasy = findViewById(R.id.cardAptitudeEasy)
        cardMedium = findViewById(R.id.cardAptitudeMedium)
        cardHard = findViewById(R.id.cardAptitudeHard)

        tvEasy = findViewById(R.id.tvAptitudeEasy)
        tvMedium = findViewById(R.id.tvAptitudeMedium)
        tvHard = findViewById(R.id.tvAptitudeHard)

        btnStart = findViewById(R.id.btnStartAptitude)
    }

    // SYSTEM BAR AND KEYBOARD SPACING
    private fun setupWindowInsets() {

        val left = scrollView.paddingLeft
        val right = scrollView.paddingRight
        val bottom = scrollView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) {
                view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val keyboard = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            view.setPadding(
                left,
                systemBars.top + dp(12),
                right,
                maxOf(systemBars.bottom, keyboard.bottom) + bottom + dp(12)
            )

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    // TOPIC AND QUESTION COUNT DROPDOWNS
    private fun setupDropdowns() {

        val topicAdapter = ArrayAdapter(
            this,
            R.layout.item_technology_dropdown,
            topics
        )

        actTopic.setAdapter(topicAdapter)
        actTopic.threshold = 1

        actTopic.setOnClickListener {
            actTopic.showDropDown()
        }

        val countAdapter = ArrayAdapter(
            this,
            R.layout.item_technology_dropdown,
            questionCounts
        )

        actQuestionCount.setAdapter(countAdapter)
        actQuestionCount.threshold = 100

        actQuestionCount.setOnClickListener {
            actQuestionCount.showDropDown()
        }

        // Default question count
        actQuestionCount.setText(
            questionCounts[0],
            false
        )
    }

    // DIFFICULTY SELECTION
    private fun setupDifficulty() {

        selectedDifficulty = "Medium"

        updateDifficultyUI()

        cardEasy.setOnClickListener {
            selectedDifficulty = "Easy"
            updateDifficultyUI()
        }

        cardMedium.setOnClickListener {
            selectedDifficulty = "Medium"
            updateDifficultyUI()
        }

        cardHard.setOnClickListener {
            selectedDifficulty = "Hard"
            updateDifficultyUI()
        }
    }

    private fun updateDifficultyUI() {

        updateCard(
            cardEasy,
            tvEasy,
            selectedDifficulty == "Easy"
        )

        updateCard(
            cardMedium,
            tvMedium,
            selectedDifficulty == "Medium"
        )

        updateCard(
            cardHard,
            tvHard,
            selectedDifficulty == "Hard"
        )
    }

    private fun updateCard(
        card: MaterialCardView,
        text: TextView,
        selected: Boolean
    ) {

        if (selected) {

            card.setCardBackgroundColor(
                android.graphics.Color.parseColor("#F26722")
            )

            card.strokeColor =
                android.graphics.Color.parseColor("#F26722")

            text.setTextColor(
                android.graphics.Color.WHITE
            )

        } else {

            card.setCardBackgroundColor(
                android.graphics.Color.WHITE
            )

            card.strokeColor =
                android.graphics.Color.parseColor("#E0E5ED")

            text.setTextColor(
                android.graphics.Color.parseColor("#17243B")
            )
        }
    }

    // START APTITUDE PRACTICE
    private fun startAptitudePractice() {

        val topic = actTopic.text.toString().trim()

        val countText =
            actQuestionCount.text.toString().trim()

        if (topic.isEmpty()) {

            actTopic.error = "Please select a topic"
            actTopic.requestFocus()
            return
        }

        if (topic !in topics) {

            actTopic.error = "Please select a valid topic"
            actTopic.requestFocus()
            return
        }

        if (countText !in questionCounts) {

            actQuestionCount.error =
                "Please select the number of questions"
            return
        }

        val count = countText.substringBefore(" ").toInt()

        val intent = Intent(
            this,
            AptitudeQuestionsActivity::class.java
        )

        intent.putExtra("aptitudeTopic", topic)
        intent.putExtra("difficulty", selectedDifficulty)
        intent.putExtra("questionCount", count)
        intent.putExtra("questionType", "Aptitude")

        startActivity(intent)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
