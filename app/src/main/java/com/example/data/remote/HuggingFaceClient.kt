package com.example.data.remote

import com.example.data.model.ModelSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HuggingFaceClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateText(
        prompt: String,
        settings: ModelSettings
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api-inference.huggingface.co/models/${settings.modelRepoId}"
            
            val jsonBody = JSONObject().apply {
                put("inputs", prompt)
                val params = JSONObject().apply {
                    put("max_new_tokens", settings.maxNewTokens)
                    put("temperature", settings.temperature.toDouble())
                    put("top_p", settings.topP.toDouble())
                    put("return_full_text", false)
                }
                put("parameters", params)
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))

            if (settings.hfApiToken.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${settings.hfApiToken.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val obj = JSONObject(responseBody)
                    obj.optString("error", "HTTP ${response.code}: ${response.message}")
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            // Parse HF Inference response: usually [{"generated_text": "..."}]
            val generatedText = try {
                if (responseBody.trim().startsWith("[")) {
                    val arr = JSONArray(responseBody)
                    if (arr.length() > 0) {
                        arr.getJSONObject(0).optString("generated_text", "")
                    } else ""
                } else if (responseBody.trim().startsWith("{")) {
                    val obj = JSONObject(responseBody)
                    obj.optString("generated_text", obj.optString("error", responseBody))
                } else {
                    responseBody
                }
            } catch (e: Exception) {
                responseBody
            }

            if (generatedText.isBlank()) {
                Result.failure(Exception("لم يتم استلام رد من النموذج السحابي"))
            } else {
                Result.success(generatedText.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
