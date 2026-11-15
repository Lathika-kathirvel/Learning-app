package com.example.learningapp.api

import java.io.Serializable

// Request sent to the AI question generator
data class TechnicalGenerateQuestionsRequest(

    val technology: String,

    val difficulty: String,

    // Optional fields
    val targetRole: String = "",
    val company: String = "",
    val skills: String = "",
    val experience: String = "",
    val education: String = "",
    val resumeText: String = "",

    // Selected by the user
    val questionCount: Int = 10
)

// Response received from AI
data class TechnicalGenerateQuestionsResponse(

    val success: Boolean = false,

    val questions: List<TechnicalQuestionItem> = emptyList(),

    val message: String? = null,

    val error: String? = null
)

// Individual MCQ
data class TechnicalQuestionItem(

    val questionNumber: Int = 0,

    val question: String = "",

    // Four answer options
    val options: List<String> = emptyList(),

    // Correct option index: 0, 1, 2 or 3
    val correctAnswer: Int = -1,

    // Explanation shown after submission
    val explanation: String = "",

    // Topic associated with the question
    val topic: String = ""

) : Serializable