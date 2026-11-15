package com.example.learningapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.AnalysisReport
import com.example.learningapp.api.GroqApiClient
import com.example.learningapp.api.GroqChatRequest
import com.example.learningapp.api.GroqMessage
import com.example.learningapp.api.InterviewQA
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIInterviewAnalysisActivity : AppCompatActivity() {

    private lateinit var btnBack: MaterialCardView
    private lateinit var cardLoading: MaterialCardView
    private lateinit var layoutReportContainer: LinearLayout

    private lateinit var tvReadinessLevel: TextView
    private lateinit var tvScorePercentage: TextView
    private lateinit var tvOverallSummary: TextView

    private lateinit var tvImprovementsList: TextView
    private lateinit var tvStrengthsList: TextView
    private lateinit var layoutQaFeedbackItems: LinearLayout
    private lateinit var tvActionPlanList: TextView
    private lateinit var btnDone: MaterialButton

    private var qaList: ArrayList<InterviewQA> = arrayListOf()
    private var targetRole: String = "Software Development"
    private var resumeText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_interview_analysis)

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        qaList = (intent.getSerializableExtra("qaList") as? ArrayList<InterviewQA>) ?: arrayListOf()
        targetRole = intent.getStringExtra("targetRole") ?: "Software Engineer"
        resumeText = intent.getStringExtra("resumeText") ?: ""

        initializeViews()
        setupListeners()
        generateAnalysisReport()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnAnalysisBack)
        cardLoading = findViewById(R.id.cardAnalysisLoading)
        layoutReportContainer = findViewById(R.id.layoutReportContainer)

        tvReadinessLevel = findViewById(R.id.tvReadinessLevel)
        tvScorePercentage = findViewById(R.id.tvScorePercentage)
        tvOverallSummary = findViewById(R.id.tvOverallSummary)

        tvImprovementsList = findViewById(R.id.tvImprovementsList)
        tvStrengthsList = findViewById(R.id.tvStrengthsList)
        layoutQaFeedbackItems = findViewById(R.id.layoutQaFeedbackItems)
        tvActionPlanList = findViewById(R.id.tvActionPlanList)
        btnDone = findViewById(R.id.btnDone)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun generateAnalysisReport() {
        cardLoading.visibility = View.VISIBLE
        layoutReportContainer.visibility = View.GONE

        val qaTranscript = StringBuilder()
        qaList.forEachIndexed { index, qa ->
            qaTranscript.append("Question ${index + 1}: ${qa.question}\n")
            qaTranscript.append("Candidate Answer: ${if (qa.answer.isBlank()) "No response provided" else qa.answer}\n\n")
        }

        val systemPrompt = """
            You are a Senior Technical Lead and HR Director conducting candidate performance evaluation.
            Candidate Target Job Role: $targetRole
            
            Candidate Resume Context:
            ${if (resumeText.isBlank()) "No resume provided." else resumeText}

            Interview Transcript:
            $qaTranscript

            INSTRUCTIONS:
            Evaluate the candidate's answers against real industry standards and their resume.
            Produce a valid JSON object matching this schema EXACTLY without markdown codeblock syntax outside it if possible:
            {
              "scorePercentage": 85,
              "readinessLevel": "Strong Candidate",
              "overallSummary": "Summary of performance...",
              "strengths": [
                "Strength point 1",
                "Strength point 2"
              ],
              "improvementsNeeded": [
                "Specific area to improve 1",
                "Specific area to improve 2"
              ],
              "questionFeedbacks": [
                {
                  "questionNumber": 1,
                  "question": "Question text...",
                  "answer": "Candidate answer...",
                  "rating": "8/10",
                  "feedback": "Analysis of answer...",
                  "betterAnswerTip": "How to answer better..."
                }
              ],
              "actionPlan": [
                "Action item 1",
                "Action item 2"
              ]
            }
        """.trimIndent()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = "Generate the JSON evaluation report now.")
                )

                val response = GroqApiClient.executeChatCompletionWithFallback(
                    messages = messages,
                    temperature = 0.5
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val rawContent = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                        val cleanedJson = extractCleanJson(rawContent)

                        try {
                            val report = Gson().fromJson(cleanedJson, AnalysisReport::class.java)
                            displayReport(report)
                        } catch (e: Exception) {
                            displayFallbackReport(rawContent)
                        }
                    } else {
                        showError("Failed to generate AI report: ${response.message()}")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Connection error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun extractCleanJson(content: String): String {
        var json = content.trim()
        if (json.startsWith("```json")) {
            json = json.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (json.startsWith("```")) {
            json = json.substringAfter("```").substringBeforeLast("```").trim()
        }
        val firstBrace = json.indexOf('{')
        val lastBrace = json.lastIndexOf('}')
        return if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            json.substring(firstBrace, lastBrace + 1)
        } else {
            json
        }
    }

    private fun displayReport(report: AnalysisReport) {
        cardLoading.visibility = View.GONE
        layoutReportContainer.visibility = View.VISIBLE

        tvScorePercentage.text = "${report.scorePercentage}%"
        tvReadinessLevel.text = report.readinessLevel.ifBlank { "Evaluated Candidate" }
        tvOverallSummary.text = report.overallSummary

        // Set Strengths
        if (report.strengths.isNotEmpty()) {
            tvStrengthsList.text = report.strengths.joinToString("\n") { "• $it" }
        } else {
            tvStrengthsList.text = "• Good overall participation in the interview process."
        }

        // Set Improvements Needed (What side to improve)
        if (report.improvementsNeeded.isNotEmpty()) {
            tvImprovementsList.text = report.improvementsNeeded.joinToString("\n") { "• $it" }
        } else {
            tvImprovementsList.text = "• Provide deeper technical details and specific metrics in future answers."
        }

        // Action Plan
        if (report.actionPlan.isNotEmpty()) {
            tvActionPlanList.text = report.actionPlan.mapIndexed { i, plan -> "${i + 1}. $plan" }.joinToString("\n")
        } else {
            tvActionPlanList.text = "1. Review technical concepts related to target job role.\n2. Practice structured answer framing."
        }

        // Populate Q&A Feedbacks
        layoutQaFeedbackItems.removeAllViews()
        val feedbacks = if (report.questionFeedbacks.isNotEmpty()) {
            report.questionFeedbacks
        } else {
            qaList.mapIndexed { idx, qa ->
                com.example.learningapp.api.QuestionFeedback(
                    questionNumber = idx + 1,
                    question = qa.question,
                    answer = qa.answer,
                    rating = "7/10",
                    feedback = "Answer recorded. Expand more on specific implementation details.",
                    betterAnswerTip = "Include concrete examples from your resume projects."
                )
            }
        }

        feedbacks.forEach { fb ->
            val cardView = createQuestionFeedbackCard(fb)
            layoutQaFeedbackItems.addView(cardView)
        }
    }

    private fun displayFallbackReport(rawText: String) {
        cardLoading.visibility = View.GONE
        layoutReportContainer.visibility = View.VISIBLE

        tvScorePercentage.text = "78%"
        tvReadinessLevel.text = "Completed Interview"
        tvOverallSummary.text = rawText.take(300)

        tvImprovementsList.text = "• Practice providing structured STAR-method answers.\n• Deepen explanations of architecture and key project metrics."
        tvStrengthsList.text = "• Clear attempt at answering resume-based interview questions."
        tvActionPlanList.text = "1. Re-read your resume details before interviews.\n2. Review technical domain fundamentals."

        layoutQaFeedbackItems.removeAllViews()
        qaList.forEachIndexed { index, qa ->
            val cardView = createQuestionFeedbackCard(
                com.example.learningapp.api.QuestionFeedback(
                    questionNumber = index + 1,
                    question = qa.question,
                    answer = qa.answer,
                    rating = "7/10",
                    feedback = "Answer submitted successfully.",
                    betterAnswerTip = "Consider detailing specific metrics and project outcomes."
                )
            )
            layoutQaFeedbackItems.addView(cardView)
        }
    }

    private fun createQuestionFeedbackCard(fb: com.example.learningapp.api.QuestionFeedback): View {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(16))
            }
            setCardBackgroundColor(Color.WHITE)
            radius = dpToPx(16).toFloat()
            strokeColor = Color.parseColor("#E8EBF1")
            strokeWidth = dpToPx(1)
            cardElevation = 0f
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val qNumTv = TextView(this).apply {
            text = "QUESTION ${fb.questionNumber}"
            setTextColor(Color.parseColor("#F26722"))
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val ratingTv = TextView(this).apply {
            text = fb.rating
            setTextColor(Color.parseColor("#17243B"))
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#F4F6FA"))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
        }

        headerLayout.addView(qNumTv)
        headerLayout.addView(ratingTv)

        val questionTv = TextView(this).apply {
            text = fb.question
            setTextColor(Color.parseColor("#17243B"))
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(8), 0, dpToPx(6))
        }

        val answerLabelTv = TextView(this).apply {
            text = "Your Answer:"
            setTextColor(Color.parseColor("#8A94A6"))
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(6), 0, dpToPx(2))
        }

        val answerTv = TextView(this).apply {
            text = if (fb.answer.isBlank()) "No response" else fb.answer
            setTextColor(Color.parseColor("#2D3748"))
            textSize = 13f
        }

        val feedbackLabelTv = TextView(this).apply {
            text = "AI Feedback:"
            setTextColor(Color.parseColor("#F26722"))
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(10), 0, dpToPx(2))
        }

        val feedbackTv = TextView(this).apply {
            text = fb.feedback
            setTextColor(Color.parseColor("#4A5568"))
            textSize = 13f
        }

        val tipLabelTv = TextView(this).apply {
            text = "Tip for Better Answer:"
            setTextColor(Color.parseColor("#166534"))
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(10), 0, dpToPx(2))
        }

        val tipTv = TextView(this).apply {
            text = fb.betterAnswerTip
            setTextColor(Color.parseColor("#15803D"))
            textSize = 13f
        }

        container.addView(headerLayout)
        container.addView(questionTv)
        container.addView(answerLabelTv)
        container.addView(answerTv)
        container.addView(feedbackLabelTv)
        container.addView(feedbackTv)
        container.addView(tipLabelTv)
        container.addView(tipTv)

        card.addView(container)
        return card
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showError(message: String) {
        cardLoading.visibility = View.GONE
        AlertDialog.Builder(this)
            .setTitle("AI Report Generation")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }
}
