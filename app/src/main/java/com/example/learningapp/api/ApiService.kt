
package com.example.learningapp.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // CHECK BACKEND CONNECTION
    @GET("api/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    // UPLOAD RESUME
    @Multipart
    @POST("api/resume/upload")
    suspend fun uploadResume(
        @Part resume: MultipartBody.Part
    ): Response<ResumeUploadResponse>

    // EXTRACT RESUME CONTENT
    @POST("api/resume/extract")
    suspend fun extractResume(
        @Body request: ResumeExtractRequest
    ): Response<ResumeExtractResponse>

    // GENERATE HR QUESTIONS
    @POST("api/hr/generate-questions")
    suspend fun generateHRQuestions(
        @Body request: HRGenerateQuestionsRequest
    ): Response<HRGenerateQuestionsResponse>

    // GENERATE TECHNICAL QUESTIONS
    @POST("api/technical/generate-questions")
    suspend fun generateTechnicalQuestions(
        @Body request: TechnicalGenerateQuestionsRequest
    ): Response<TechnicalGenerateQuestionsResponse>
}
