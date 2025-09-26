package com.example.fitconnect.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class CloudinaryService {

    private val cloudName = "dansyarsa"
    private val uploadPreset = "fqasgarp"

    private val client = OkHttpClient()

    suspend fun uploadImage(file: File): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Cloudinary upload failed: ${response.code} - ${response.message}")
                }

                val responseBody = response.body?.string()
                responseBody?.let {
                    val json = JSONObject(it)
                    return@withContext json.getString("secure_url")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
        return@withContext null
    }
}