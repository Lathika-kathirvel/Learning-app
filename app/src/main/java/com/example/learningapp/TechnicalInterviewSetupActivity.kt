package com.example.learningapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
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

class TechnicalInterviewSetupActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView

    private lateinit var actTechnology: MaterialAutoCompleteTextView
    private lateinit var actQuestionCount: MaterialAutoCompleteTextView

    private lateinit var cardEasy: MaterialCardView
    private lateinit var cardMedium: MaterialCardView
    private lateinit var cardHard: MaterialCardView

    private lateinit var tvEasy: TextView
    private lateinit var tvMedium: TextView
    private lateinit var tvHard: TextView

    private lateinit var btnGenerate: MaterialButton

    private var selectedDifficulty = "Medium"

    private val technologies = arrayOf(
        "Java",
        "Python",
        "JavaScript",
        "React.js",
        "Node.js",
        "SQL",
        "MySQL",
        "Android Development",
        "Kotlin",
        "C Programming",
        "C++",
        "HTML and CSS",
        "Spring Boot",
        "MongoDB",
        "Power BI",
        "Excel",
        "Data Structures and Algorithms",
        "Machine Learning",
        "Other"
    )

    private val questionCounts = arrayOf(
        "5",
        "10",
        "15",
        "20",
        "25"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_technical_interview_setup)

        initializeViews()
        setupWindowInsets()
        setupTechnologyDropdown()
        setupQuestionCountDropdown()
        setupDifficultySelection()
        setupClickListeners()
    }

    // INITIALIZE VIEWS
    private fun initializeViews() {

        scrollView = findViewById(R.id.technicalScrollView)

        actTechnology = findViewById(R.id.actTechnology)

        actQuestionCount = findViewById(R.id.actQuestionCount)

        cardEasy = findViewById(R.id.cardEasy)
        cardMedium = findViewById(R.id.cardMedium)
        cardHard = findViewById(R.id.cardHard)

        tvEasy = findViewById(R.id.tvEasy)
        tvMedium = findViewById(R.id.tvMedium)
        tvHard = findViewById(R.id.tvHard)

        btnGenerate = findViewById(R.id.btnGenerateTechnical)
    }

    // CLICK LISTENERS
    private fun setupClickListeners() {

        findViewById<MaterialCardView>(
            R.id.btnTechnicalBack
        ).setOnClickListener {
            finish()
        }

        btnGenerate.setOnClickListener {
            generateTechnicalQuestions()
        }
    }

    // TECHNOLOGY DROPDOWN
    private fun setupTechnologyDropdown() {

        val adapter = ArrayAdapter(
            this,
            R.layout.item_technology_dropdown,
            technologies
        )

        actTechnology.setAdapter(adapter)
        actTechnology.threshold = 0

        actTechnology.setOnClickListener {
            actTechnology.showDropDown()
        }

        actTechnology.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actTechnology.showDropDown()
            }
        }
    }

    // QUESTION COUNT DROPDOWN
    private fun setupQuestionCountDropdown() {

        val adapter = ArrayAdapter(
            this,
            R.layout.item_technology_dropdown,
            questionCounts
        )

        actQuestionCount.setAdapter(adapter)

        actQuestionCount.threshold = 0

        actQuestionCount.setOnClickListener {
            actQuestionCount.showDropDown()
        }

        actQuestionCount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actQuestionCount.showDropDown()
            }
        }
    }

    // DIFFICULTY SELECTION
    private fun setupDifficultySelection() {

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

    // UPDATE DIFFICULTY UI
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

    // UPDATE CARD APPEARANCE
    private fun updateCard(
        card: MaterialCardView,
        textView: TextView,
        isSelected: Boolean
    ) {

        if (isSelected) {

            card.setCardBackgroundColor(
                Color.parseColor("#F26722")
            )

            card.strokeColor = Color.parseColor("#F26722")

            textView.setTextColor(Color.WHITE)

        } else {

            card.setCardBackgroundColor(Color.WHITE)

            card.strokeColor = Color.parseColor("#E0E5ED")

            textView.setTextColor(
                Color.parseColor("#17243B")
            )
        }
    }

    // GENERATE TECHNICAL QUESTIONS
    private fun generateTechnicalQuestions() {

        val technology = actTechnology.text.toString().trim()

        val questionCountText =
            actQuestionCount.text.toString().trim()

        // Validate technology
        if (technology.isEmpty()) {

            actTechnology.error =
                "Please select a technology"

            actTechnology.requestFocus()

            return
        }

        // Validate question count
        val questionCount = questionCountText.toIntOrNull()

        if (questionCount !in listOf(5, 10, 15, 20, 25)) {

            actQuestionCount.error =
                "Please select the number of questions"

            actQuestionCount.requestFocus()

            return
        }

        // Navigate to technical MCQ screen
        val intent = Intent(
            this,
            GeneratedQuestionsActivity::class.java
        )

        intent.putExtra("technology", technology)

        intent.putExtra("difficulty", selectedDifficulty)

        intent.putExtra("questionCount", questionCount)

        intent.putExtra(
            "questionType",
            "Technical MCQ"
        )

        startActivity(intent)
    }

    // WINDOW INSETS
    private fun setupWindowInsets() {

        val originalLeft = scrollView.paddingLeft
        val originalTop = scrollView.paddingTop
        val originalRight = scrollView.paddingRight
        val originalBottom = scrollView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            val bottomSpacing = maxOf(
                systemBars.bottom,
                ime.bottom
            ) + originalBottom

            view.setPadding(
                originalLeft,
                systemBars.top + originalTop,
                originalRight,
                bottomSpacing
            )

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    // SCROLL TO KEEP VIEW VISIBLE
    private fun scrollToView(view: View) {

        scrollView.post {

            if (!view.isShown) return@post

            val rect = Rect()

            view.getDrawingRect(rect)

            scrollView.offsetDescendantRectToMyCoords(
                view,
                rect
            )

            val margin = (
                    24 * resources.displayMetrics.density
                    ).toInt()

            val visibleBottom =
                scrollView.scrollY +
                        scrollView.height -
                        scrollView.paddingBottom

            if (rect.bottom + margin > visibleBottom) {

                scrollView.smoothScrollTo(
                    0,
                    rect.bottom + margin - scrollView.height +
                            scrollView.paddingBottom
                )
            }
        }
    }
}