package com.example.ai

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistantService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    var customApiKey: String = ""

    private fun getActiveApiKey(): String {
        if (customApiKey.isNotBlank()) return customApiKey
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey != "MY_GEMINI_API_KEY" && buildKey.isNotBlank()) buildKey else ""
    }

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        systemInstruction: String = "Kamu adalah Dots, asisten AI pribadi yang cerdas, ramah, responsif, dan membantu dalam bahasa Indonesia. Kamu pandai mengatur jadwal, merangkum, menulis, memberikan ide, dan menjawab berbagai pertanyaan dengan jelas, terstruktur, dan solutif."
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()

        if (apiKey.isBlank()) {
            // Intelligent local helper fallback when API key is not yet configured in Secrets
            return@withContext Result.success(getSmartOfflineResponse(prompt))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Add previous conversational context (last 6 turns for speed)
            conversationHistory.takeLast(6).forEach { (role, text) ->
                val contentObj = JSONObject().apply {
                    put("role", if (role == "USER") "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(contentObj)
            }

            // Current prompt
            val currentTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                }
                put("parts", parts)
            }
            contentsArray.put(currentTurn)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Gagal menghubungi server AI (Kode ${response.code}): $responseBody")
                )
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success("Dots telah memproses permintaan Anda, namun tidak ada teks yang dihasilkan.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSmartOfflineResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("siapa kamu") || lower.contains("siapakah kamu") ->
                "Halo! Saya **Dots**, asisten AI pribadi Anda. Saya siap membantu Anda mengelola tugas harian, menulis dokumen/pesan, merangkum informasi, menjawab pertanyaan, dan memberikan ide kreatif!\n\n*(Tips: Hubungkan Gemini API Key di pengaturan atau Secrets panel untuk membuka kecerdasan penuh model Gemini 3.5 Flash).* "

            lower.contains("tugas") || lower.contains("jadwal") || lower.contains("ingatkan") ->
                "Tentu! Anda dapat mencatat dan mengelola tugas langsung di tab **Tugas & Jadwal**. Sebutkan apa yang ingin Anda jadwalkan, dan saya akan bantu menyusunnya secara rapi."

            lower.contains("terima kasih") || lower.contains("makasih") ->
                "Sama-sama! Selalu senang bisa membantu Anda. Ada lagi yang bisa Dots bantu hari ini?"

            lower.contains("halo") || lower.contains("hai") || lower.contains("pagi") || lower.contains("sore") || lower.contains("malam") ->
                "Halo! Selamat datang di Asisten Dots. Bagaimana kabarmu hari ini? Ceritakan apa yang sedang ingin kamu kerjakan, dan saya akan bantu menyelesaikannya!"

            lower.contains("ide") || lower.contains("rekomendasi") ->
                "Tentu! Untuk memberikan ide terbaik, coba ceritakan sedikit lebih detail tentang bidang atau proyek yang sedang Anda rencanakan. Apakah tentang konten, bisnis, tugas kuliah, atau rencana harian?"

            else ->
                "Saya menerima pesan Anda: \"$prompt\".\n\nSebagai asisten **Dots**, saya siap membantu menganalisis, merangkum, menulis, atau mengatur tugas Anda. Untuk mengaktifkan respon komprehensif dari Gemini 3.5 Flash, pastikan API Key telah terkonfigurasi di Secrets atau menu Pengaturan."
        }
    }
}
