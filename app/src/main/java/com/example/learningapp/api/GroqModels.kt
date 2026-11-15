package com.example.learningapp.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Groq API Request Body
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,
)

// Message payload for Chat Completions
data class GroqMessage(
    val role: String,
    val content: String,
)

// Groq API Response Body
data class GroqChatResponse(
    val id: String? = null,
    val choices: List<GroqChoice>? = null
)

data class GroqChoice(
    val index: Int? = null,
    val message: GroqMessage? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

// Track Question and Candidate Answer
data class InterviewQA(
    val questionNumber: Int,
    val question: String,
    var answer: String = ""
) : Serializable

// Structured Analysis Report
data class AnalysisReport(
    val scorePercentage: Int = 0,
    val readinessLevel: String = "Not Evaluated",
    val overallSummary: String = "",
    val strengths: List<String> = emptyList(),
    val improvementsNeeded: List<String> = emptyList(),
    val questionFeedbacks: List<QuestionFeedback> = emptyList(),
    val actionPlan: List<String> = emptyList()
) : Serializable

data class QuestionFeedback(
    val questionNumber: Int,
    val question: String,
    val answer: String,
    val rating: String,
    val feedback: String,
    val betterAnswerTip: String
) : Serializable

// GET /models response
data class GroqModelsResponse(
    val data: List<GroqModelInfo>? = null
)

data class GroqModelInfo(
    val id: String,
    val active: Boolean? = null
)
