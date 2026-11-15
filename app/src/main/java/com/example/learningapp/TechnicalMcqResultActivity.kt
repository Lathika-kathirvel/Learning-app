package com.example.learningapp

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
import org.json.JSONArray

class TechnicalMcqResultActivity : AppCompatActivity() {

    private lateinit var tvResultTechnology: TextView
    private lateinit var tvResultDifficulty: TextView
    private lateinit var tvFinalScore: TextView
    private lateinit var tvScorePercentage: TextView
    private lateinit var progressAIAnalysis: ProgressBar
    private lateinit var tvAIAnalysis: TextView
    private lateinit var tvAnswerReview: TextView
    private lateinit var btnResultBack: View
    private lateinit var btnFinishTechnicalResult: Button

    private var technology = ""
    private var difficulty = "Medium"
    private var questionCount = 5
    private var score = 0
    private var reviewJson = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technical_mcq_result)

        technology = intent.getStringExtra("technology") ?: "General"
        difficulty = intent.getStringExtra("difficulty") ?: "Medium"
        questionCount = intent.getIntExtra("questionCount", 5)
        score = intent.getIntExtra("score", 0)
        reviewJson = intent.getStringExtra("review") ?: "[]"

        initializeViews()
        setupListeners()
        populateResults()
        generateAiPerformanceAnalysis()
    }

    private fun initializeViews() {
        tvResultTechnology = findViewById(R.id.tvResultTechnology)
        tvResultDifficulty = findViewById(R.id.tvResultDifficulty)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        tvScorePercentage = findViewById(R.id.tvScorePercentage)
        progressAIAnalysis = findViewById(R.id.progressAIAnalysis)
        tvAIAnalysis = findViewById(R.id.tvAIAnalysis)
        tvAnswerReview = findViewById(R.id.tvAnswerReview)
        btnResultBack = findViewById(R.id.btnResultBack)
        btnFinishTechnicalResult = findViewById(R.id.btnFinishTechnicalResult)
    }

    private fun setupListeners() {
        btnResultBack.setOnClickListener {
            finish()
        }

        btnFinishTechnicalResult.setOnClickListener {
            val intent = android.content.Intent(
                this,
                DashboardActivity::class.java
            ).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun populateResults() {
        tvResultTechnology.text = "Technology: $technology"
        tvResultDifficulty.text = "Difficulty: $difficulty"
        tvFinalScore.text = "$score / $questionCount"

        val percentage = if (questionCount > 0) {
            (score * 100) / questionCount
        } else {
            0
        }
        tvScorePercentage.text = "$percentage% Score"

        // Parse and display answer review
        try {
            val array = JSONArray(reviewJson)
            val reviewBuilder = StringBuilder()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val qNum = item.optInt("questionNumber", i + 1)
                val question = item.optString("question")
                val topic = item.optString("topic")
                val selected = item.optString("selectedAnswer")
                val correct = item.optString("correctAnswer")
                val isCorrect = item.optBoolean("isCorrect")
                val explanation = item.optString("explanation")

                val statusSymbol = if (isCorrect) "✅ Correct" else "❌ Incorrect"

                reviewBuilder.append("Q$qNum: $question\n")
                reviewBuilder.append("Topic: $topic\n")
                reviewBuilder.append("Your Answer: $selected ($statusSymbol)\n")
                if (!isCorrect) {
                    reviewBuilder.append("Correct Answer: $correct\n")
                }
                reviewBuilder.append("Explanation: $explanation\n\n")
            }

            if (reviewBuilder.isNotEmpty()) {
                tvAnswerReview.text = reviewBuilder.toString().trim()
            } else {
                tvAnswerReview.text = "No review details available."
            }
        } catch (e: Exception) {
            tvAnswerReview.text = "Unable to load review details."
        }
    }

    private fun generateAiPerformanceAnalysis() {
        progressAIAnalysis.visibility = View.VISIBLE
        tvAIAnalysis.text = "Groq AI is analyzing your performance..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val percentage = if (questionCount > 0) (score * 100) / questionCount else 0

                val prompt = """
                    You are an expert technical interviewer and career mentor.
                    A candidate just completed a $technology technical quiz at $difficulty difficulty level.
                    
                    Score: $score out of $questionCount ($percentage%).
                    
                    Provide a concise, encouraging, and insightful performance analysis (3-4 sentences), highlighting strengths, areas to improve, and study recommendations for $technology.
                """.trimIndent()

                val messages = listOf(
                    GroqMessage(role = "system", content = "You are a professional technical mentor. Provide clear performance feedback."),
                    GroqMessage(role = "user", content = prompt)
                )

                val response = GroqApiClient.executeChatCompletionWithFallback(
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
                        ?: "Good effort! Continue practicing core concepts in $technology to strengthen your technical proficiency."
                } else {
                    "You achieved a score of $score/$questionCount ($percentage%) in $technology. Keep practicing to master advanced topics."
                }

                withContext(Dispatchers.Main) {
                    progressAIAnalysis.visibility = View.GONE
                    tvAIAnalysis.text = analysis
                }

            } catch (e: Exception) {
                val percentage = if (questionCount > 0) (score * 100) / questionCount else 0
                withContext(Dispatchers.Main) {
                    progressAIAnalysis.visibility = View.GONE
                    tvAIAnalysis.text = "You scored $score out of $questionCount ($percentage%). Great effort in $technology!"
                }
            }
        }
    }
}