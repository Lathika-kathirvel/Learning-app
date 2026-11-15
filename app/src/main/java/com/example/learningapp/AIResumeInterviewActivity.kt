package com.example.learningapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.GroqApiClient
import com.example.learningapp.api.GroqChatRequest
import com.example.learningapp.api.GroqMessage
import com.example.learningapp.api.InterviewQA
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIResumeInterviewActivity : AppCompatActivity() {

    private lateinit var btnBack: MaterialCardView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvQuestionProgress: TextView
    private lateinit var tvTargetRoleBadge: TextView
    private lateinit var pbProgress: ProgressBar
    private lateinit var pbAiLoading: ProgressBar
    private lateinit var tvAiQuestion: TextView
    private lateinit var etCandidateAnswer: TextInputEditText
    private lateinit var btnSubmitAnswer: MaterialButton
    private lateinit var btnFinishEarly: MaterialButton

    private var resumeText: String = ""
    private var targetRole: String = "Software Engineer"
    private var difficulty: String = "Medium"

    private var currentQuestionIndex = 1
    private val maxQuestions = 5

    private val qaList = mutableListOf<InterviewQA>()
    private val groqMessages = mutableListOf<GroqMessage>()

    private var currentQuestionText: String = ""
    private var isGeneratingQuestion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_resume_interview)

        resumeText = intent.getStringExtra("resumeText") ?: ""
        targetRole = intent.getStringExtra("targetRole") ?: "Software Development"
        difficulty = intent.getStringExtra("difficulty") ?: "Medium"

        initializeViews()
        setupListeners()
        startAiInterviewSession()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnAIBack)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress)
        tvTargetRoleBadge = findViewById(R.id.tvTargetRoleBadge)
        pbProgress = findViewById(R.id.pbInterviewProgress)
        pbAiLoading = findViewById(R.id.pbAiLoading)
        tvAiQuestion = findViewById(R.id.tvAiQuestion)
        etCandidateAnswer = findViewById(R.id.etCandidateAnswer)
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer)
        btnFinishEarly = findViewById(R.id.btnFinishEarly)

        tvTargetRoleBadge.text = targetRole
        tvHeaderTitle.text = "Resume AI Interview"
        pbProgress.max = maxQuestions
        updateProgressUI()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            showExitDialog()
        }

        btnSubmitAnswer.setOnClickListener {
            handleSubmitAnswer()
        }

        btnFinishEarly.setOnClickListener {
            handleFinishEarly()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun updateProgressUI() {
        tvQuestionProgress.text = "Question $currentQuestionIndex of $maxQuestions"
        pbProgress.progress = currentQuestionIndex

        if (currentQuestionIndex == maxQuestions) {
            btnSubmitAnswer.text = "Submit Answer & Finish Interview  ✓"
        } else {
            btnSubmitAnswer.text = "Submit Answer & Next Question  →"
        }
    }

    private fun startAiInterviewSession() {
        val systemPrompt = """
            You are an expert AI Technical & HR Interviewer conducting a realistic job interview.
            Candidate Target Job Role: $targetRole
            Difficulty: $difficulty
            Candidate Resume Context:
            ${if (resumeText.isBlank()) "No resume uploaded. Ask general $targetRole questions." else resumeText}

            INSTRUCTIONS:
            - Ask EXACTLY ONE interview question per turn.
            - Questions should be tailored to the candidate's resume, projects, skills, and target role.
            - Do NOT include any introductory greetings, conversational fluff, or preamble. Return ONLY the question text directly.
        """.trimIndent()

        groqMessages.clear()
        groqMessages.add(GroqMessage(role = "system", content = systemPrompt))
        groqMessages.add(
            GroqMessage(
                role = "user",
                content = "Generate Question 1 for the candidate based on their resume and target role ($targetRole)."
            )
        )

        fetchNextQuestionFromGroq()
    }

    private fun fetchNextQuestionFromGroq() {
        if (isGeneratingQuestion) return
        isGeneratingQuestion = true

        setUiLoadingState(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = GroqApiClient.executeChatCompletionWithFallback(
                    messages = groqMessages,
                    temperature = 0.7
                )

                withContext(Dispatchers.Main) {
                    setUiLoadingState(false)
                    isGeneratingQuestion = false

                    if (response.isSuccessful && response.body() != null) {
                        val choices = response.body()?.choices
                        val generatedText = choices?.firstOrNull()?.message?.content?.trim()

                        if (!generatedText.isNullOrBlank()) {
                            currentQuestionText = generatedText
                            tvAiQuestion.text = currentQuestionText

                            // Save assistant message in groq history
                            groqMessages.add(GroqMessage(role = "assistant", content = generatedText))
                        } else {
                            showError("AI returned an empty question. Please try again.")
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string()
                            ?: "Failed to generate AI question (${response.code()})"
                        showError("Groq AI Error: $errorMsg")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setUiLoadingState(false)
                    isGeneratingQuestion = false
                    showError("Connection error: ${e.localizedMessage ?: "Could not connect to Groq API."}")
                }
            }
        }
    }

    private fun handleSubmitAnswer() {
        val answerText = etCandidateAnswer.text.toString().trim()

        if (answerText.isBlank()) {
            Toast.makeText(this, "Please enter your answer before continuing", Toast.LENGTH_SHORT).show()
            etCandidateAnswer.requestFocus()
            return
        }

        // Record the answered Q&A
        qaList.add(
            InterviewQA(
                questionNumber = currentQuestionIndex,
                question = currentQuestionText,
                answer = answerText
            )
        )

        etCandidateAnswer.setText("")

        if (currentQuestionIndex >= maxQuestions) {
            navigateToAnalysisScreen()
        } else {
            currentQuestionIndex++
            updateProgressUI()

            // Prompt Groq for the next question considering candidate's answer
            val nextPrompt = """
                Candidate answer to Question ${currentQuestionIndex - 1}:
                "$answerText"

                Based on their resume, previous questions, and this answer, ask Question $currentQuestionIndex.
                Remember: Ask ONLY the question directly without any extra commentary.
            """.trimIndent()

            groqMessages.add(GroqMessage(role = "user", content = nextPrompt))
            fetchNextQuestionFromGroq()
        }
    }

    private fun handleFinishEarly() {
        val currentAnswer = etCandidateAnswer.text.toString().trim()
        if (currentAnswer.isNotBlank()) {
            qaList.add(
                InterviewQA(
                    questionNumber = currentQuestionIndex,
                    question = currentQuestionText,
                    answer = currentAnswer
                )
            )
        }

        if (qaList.isEmpty()) {
            Toast.makeText(this, "Please answer at least one question before viewing analysis.", Toast.LENGTH_LONG).show()
            return
        }

        navigateToAnalysisScreen()
    }

    private fun navigateToAnalysisScreen() {
        val intent = Intent(this, AIInterviewAnalysisActivity::class.java).apply {
            putExtra("qaList", ArrayList(qaList))
            putExtra("targetRole", targetRole)
            putExtra("resumeText", resumeText)
            putExtra("difficulty", difficulty)
        }
        startActivity(intent)
        finish()
    }

    private fun setUiLoadingState(isLoading: Boolean) {
        if (isLoading) {
            pbAiLoading.visibility = View.VISIBLE
            tvAiQuestion.text = "Groq AI is crafting Question $currentQuestionIndex based on your resume..."
            btnSubmitAnswer.isEnabled = false
            btnFinishEarly.isEnabled = false
        } else {
            pbAiLoading.visibility = View.GONE
            btnSubmitAnswer.isEnabled = true
            btnFinishEarly.isEnabled = true
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("AI Interview Question")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                fetchNextQuestionFromGroq()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit AI Interview?")
            .setMessage("Your current interview progress will be lost. Do you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNegativeButton("Continue", null)
            .show()
    }
}
