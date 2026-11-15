package com.example.learningapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.GroqApiClient
import com.example.learningapp.api.GroqMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AptitudeResultActivity : AppCompatActivity() {

    private lateinit var tvResultTopic: TextView
    private lateinit var tvResultDifficulty: TextView
    private lateinit var tvFinalScore: TextView
    private lateinit var tvScorePercentage: TextView
    private lateinit var progressAIAnalysis: ProgressBar
    private lateinit var tvAIAnalysis: TextView

    private lateinit var btnResultBack: View
    private lateinit var btnFinishAptitudeResult: Button

    private var topic = "Quantitative Aptitude"
    private var difficulty = "Medium"
    private var questionCount = 10
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_aptitude_result)

        topic = intent.getStringExtra("topic")
            ?: "Quantitative Aptitude"

        difficulty = intent.getStringExtra("difficulty")
            ?: "Medium"

        questionCount = intent.getIntExtra("questionCount", 10)

        score = intent.getIntExtra("score", 0)

        initializeViews()

        setupListeners()

        populateResults()

        generateAiPerformanceAnalysis()
    }

    private fun initializeViews() {

        tvResultTopic = findViewById(R.id.tvResultTopic)

        tvResultDifficulty = findViewById(R.id.tvResultDifficulty)

        tvFinalScore = findViewById(R.id.tvFinalScore)

        tvScorePercentage = findViewById(R.id.tvScorePercentage)

        progressAIAnalysis = findViewById(R.id.progressAIAnalysis)

        tvAIAnalysis = findViewById(R.id.tvAIAnalysis)

        btnResultBack = findViewById(R.id.btnResultBack)

        btnFinishAptitudeResult =
            findViewById(R.id.btnFinishAptitudeResult)
    }

    // BUTTON LISTENERS
    private fun setupListeners() {

        btnResultBack.setOnClickListener {
            goToDashboard()
        }

        btnFinishAptitudeResult.setOnClickListener {
            goToDashboard()
        }
    }

    // NAVIGATE TO DASHBOARD
    private fun goToDashboard() {

        val intent = Intent(
            this,
            DashboardActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        startActivity(intent)

        finish()
    }

    // DISPLAY QUIZ RESULTS
    private fun populateResults() {

        tvResultTopic.text = "Topic: $topic"

        tvResultDifficulty.text = "Difficulty: $difficulty"

        tvFinalScore.text = "$score / $questionCount"

        val percentage = if (questionCount > 0) {
            (score * 100) / questionCount
        } else {
            0
        }

        tvScorePercentage.text = "$percentage% Score"
    }

    // GROQ AI PERFORMANCE ANALYSIS
    private fun generateAiPerformanceAnalysis() {

        progressAIAnalysis.visibility = View.VISIBLE

        tvAIAnalysis.text =
            "Groq AI is analyzing your performance and areas to improve..."

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val percentage = if (questionCount > 0) {
                    (score * 100) / questionCount
                } else {
                    0
                }

                val prompt = """
                    You are an expert aptitude test evaluator 
                    and career mentor.

                    A candidate just completed a $topic test
                    at $difficulty difficulty level.

                    Score: $score out of $questionCount ($percentage%).

                    Provide a detailed performance evaluation
                    in 3-4 sentences.

                    Highlight:
                    1. Candidate's strengths.
                    2. Areas that need improvement.
                    3. Concepts to practice.
                    4. Actionable suggestions to improve
                       accuracy and problem-solving speed.

                    Provide personalized feedback for $topic.
                """.trimIndent()

                val messages = listOf(

                    GroqMessage(
                        role = "system",
                        content = """
                            You are a professional aptitude test mentor.
                            Analyze student performance and provide
                            clear, practical improvement suggestions.
                        """.trimIndent()
                    ),

                    GroqMessage(
                        role = "user",
                        content = prompt
                    )
                )

                val response =
                    GroqApiClient.executeChatCompletionWithFallback(
                        messages = messages,
                        temperature = 0.4
                    )

                val analysis = if (response.isSuccessful) {

                    response.body()
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?.trim()
                        ?: """
                            Good effort! Continue practicing 
                            core concepts in $topic to improve
                            your problem-solving speed and accuracy.
                        """.trimIndent()

                } else {

                    """
                        You achieved $score out of $questionCount
                        ($percentage%) in $topic.

                        Keep practicing the important concepts
                        and attempt timed mock tests to improve
                        your performance.
                    """.trimIndent()
                }

                withContext(Dispatchers.Main) {

                    progressAIAnalysis.visibility = View.GONE

                    tvAIAnalysis.text = analysis
                }

            } catch (e: Exception) {

                val percentage = if (questionCount > 0) {
                    (score * 100) / questionCount
                } else {
                    0
                }

                withContext(Dispatchers.Main) {

                    progressAIAnalysis.visibility = View.GONE

                    tvAIAnalysis.text = """
                        You scored $score out of $questionCount
                        ($percentage%) in $topic.

                        Great effort! Review the questions you
                        answered incorrectly and practice
                        timed mock tests to improve your accuracy.
                    """.trimIndent()
                }
            }
        }
    }
}