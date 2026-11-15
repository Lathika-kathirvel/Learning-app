
package com.example.learningapp.api

import java.io.Serializable

// ==========================================
// RESUME UPLOAD RESPONSE
// ==========================================

data class ResumeUploadResponse(
    val filename: String? = null,
    val message: String? = null,
    val success: Boolean? = null,
    val filePath: String? = null
)

// ==========================================
// RESUME EXTRACTION REQUEST
// ==========================================

data class ResumeExtractRequest(
    val filename: String
)

// ==========================================
// RESUME EXTRACTION RESPONSE
// ==========================================

data class ResumeExtractResponse(
    val extractedText: String? = null,
    val text: String? = null,
    val message: String? = null,
    val success: Boolean? = null
)

// ==========================================
// HR QUESTION GENERATION REQUEST
// ==========================================

data class HRGenerateQuestionsRequest(
    val extractedText: String,
    val categories: List<String>,
    val difficulty: String,
    val questionCount: Int
)

// ==========================================
// HR QUESTION ITEM
// ==========================================

data class HRQuestionItem(
    val questionNumber: Int? = null,
    val category: String? = null,
    val question: String? = null
) : Serializable

// ==========================================
// HR QUESTION GENERATION RESPONSE
// ==========================================

data class HRGenerateQuestionsResponse(
    val success: Boolean? = null,
    val questions: List<HRQuestionItem>? = null,
    val message: String? = null,
    val error: String? = null
)

