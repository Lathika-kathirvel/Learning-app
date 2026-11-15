package com.example.learningapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {

    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>

    @GET("models")
    suspend fun getModels(
        @Header("Authorization") authorization: String
    ): Response<GroqModelsResponse>
}