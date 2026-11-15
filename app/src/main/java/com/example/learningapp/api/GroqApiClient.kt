package com.example.learningapp.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GroqApiClient {

    // Keep API key outside the Android APK in production.
    // Do not paste your secret key into shared code.
    const val API_KEY = ""

    // Groq API endpoint
    private const val BASE_URL =
        "https://api.groq.com/openai/v1/"

    // Models to try, in priority order.
    // Verify availability in your Groq account.
    // Used only if the live model list cannot be fetched.
    private val FALLBACK_MODELS = listOf(
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant"
    )

    // Preferred models, best first. Only used when the key can access them.
    private val PREFERRED = listOf(
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "openai/gpt-oss-20b",
        "openai/gpt-oss-120b",
        "meta-llama/llama-4-scout-17b-16e-instruct"
    )

    // Non-chat models that must never be tried.
    private val EXCLUDED = listOf(
        "whisper", "tts", "guard", "orpheus", "playai", "safeguard", "embed"
    )

    @Volatile
    private var cachedModels: List<String>? = null

    /** Asks Groq which models THIS key can use, so no name is hardcoded. */
    private suspend fun availableModels(): List<String> {
        cachedModels?.let { return it }
        return try {
            val resp = apiService.getModels(getAuthHeader())
            val ids = resp.body()?.data.orEmpty()
                .filter { it.active != false }
                .map { it.id }
                .filter { id -> EXCLUDED.none { id.contains(it, ignoreCase = true) } }
            Log.d("GROQ_AI", "Available models: $ids")
            if (ids.isEmpty()) {
                Log.e("GROQ_AI", "Models list HTTP ${resp.code()}: ${resp.errorBody()?.string()}")
                FALLBACK_MODELS
            } else {
                val ordered = PREFERRED.filter { it in ids } + ids.filter { it !in PREFERRED }
                cachedModels = ordered
                ordered
            }
        } catch (e: Exception) {
            Log.e("GROQ_AI", "Could not fetch models", e)
            FALLBACK_MODELS
        }
    }

    // Errors that only mean "this model is unavailable" -> try the next one.
    private val MODEL_UNAVAILABLE_MARKERS = listOf(
        "model_decommissioned",
        "model_not_found",
        "model_not_active"
    )

    // Enable body logging for debugging AI requests/responses.
    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GroqApiService::class.java)
    }

    // Authorization header
    fun getAuthHeader(): String {

        require(
            API_KEY.isNotBlank()
        ) {
            "Groq API key is not configured."
        }

        return "Bearer $API_KEY"
    }

    // Execute AI request with model fallback
    suspend fun executeChatCompletionWithFallback(
        messages: List<GroqMessage>,
        temperature: Double = 0.7
    ): Response<GroqChatResponse> {

        var lastResponse: Response<GroqChatResponse>? = null
        var lastException: Exception? = null

        for (modelName in availableModels()) {

            try {

                Log.d(
                    "GROQ_AI",
                    "Trying model: $modelName"
                )

                val request = GroqChatRequest(
                    model = modelName,
                    messages = messages,
                    temperature = temperature
                )

                val response = apiService.getChatCompletion(
                    authorization = getAuthHeader(),
                    request = request
                )

                Log.d(
                    "GROQ_AI",
                    "HTTP response: ${response.code()}"
                )

                if (
                    response.isSuccessful &&
                    !response.body()?.choices.isNullOrEmpty()
                ) {

                    Log.d(
                        "GROQ_AI",
                        "AI response received successfully"
                    )

                    return response
                }

                // HTTP 200 but no choices: nothing to rebuild, try next model.
                if (response.isSuccessful) {
                    if (lastResponse == null) lastResponse = response
                    continue
                }

                // errorBody() can only be read ONCE. Read it here and rebuild
                // the response so callers can still read the message later.
                val errorMessage = response.errorBody()?.string().orEmpty()

                Log.e(
                    "GROQ_AI",
                    "Model: $modelName, " +
                            "HTTP: ${response.code()}, " +
                            "Error: $errorMessage"
                )

                val rebuilt: Response<GroqChatResponse> = Response.error(
                    response.code(),
                    errorMessage.toResponseBody("application/json".toMediaTypeOrNull())
                )

                // Keep the FIRST real failure: later fallback models must not
                // overwrite it with a less useful error.
                if (lastResponse == null) lastResponse = rebuilt

                val modelUnavailable = MODEL_UNAVAILABLE_MARKERS.any {
                    errorMessage.contains(it, ignoreCase = true)
                }

                // Only move on to the next model when this model is unavailable.
                // Auth, rate-limit and other bad-request errors would fail the
                // same way on every model, so return them immediately.
                if (!modelUnavailable) {
                    return rebuilt
                }

            } catch (e: Exception) {

                lastException = e

                Log.e(
                    "GROQ_AI",
                    "Request failed for model: $modelName",
                    e
                )
            }
        }

        // Return the last HTTP response if available.
        if (lastResponse != null) {
            return lastResponse
        }

        // All network attempts failed.
        throw Exception(
            "Groq connection failed: " +
                    (
                            lastException?.localizedMessage
                                ?: "Unknown network error"
                            ),
            lastException
        )
    }
}