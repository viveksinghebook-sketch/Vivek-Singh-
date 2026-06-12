package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Solves a student's doubt using the Gemini AI, styled as an assistant of Vinay Sir.
     */
    suspend fun solveDoubt(subject: String, question: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Vinay Sir's Assistant: (Offline Preview Mode) - It looks like the GEMINI_API_KEY is not configured yet in your environment! Here is a mock explanation for '$question': Keep practicing daily to master $subject concepts!"
        }

        val systemPrompt = "You are Vinay Sir's AI Teaching Assistant for 'RR COACHING CENTRE BY VINAY SIR'. " +
                "You assist students of Classes 9, 10, 11, and 12 in subjects like Mathematics, Physics, Chemistry, Biology, Social Science, Hindi, and English. " +
                "Explain concepts bilingually (Hinglish: Hindi transliterated or blended with English) which makes it friendly, engaging, and clear. " +
                "Do not write overly long answers. Break it into bullet points, give a fast formula list or step-by-step math solver when necessary, and close with an encouraging sentence like 'Padhte raho, badhte raho!' or 'Shabash, lagatar mehnat karo!'"

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "Subject: $subject\nQuestion: $question")))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Vinay Sir's Assistant: Mujhe is prashn ko samajhne me thodi pareshani hui. Kripya dubaara koshish karein ya seedhe classroom me sir se poochein!"
        } catch (e: Exception) {
            "Vinay Sir's Assistant: Network disconnect ya error aane ke karan call nahi ho saki.\nError details: ${e.localizedMessage ?: "Unknown Error"}\n\n*Conceptual Tip*: $subject concepts is very interesting. Don't worry, keep learning!"
        }
    }
}
