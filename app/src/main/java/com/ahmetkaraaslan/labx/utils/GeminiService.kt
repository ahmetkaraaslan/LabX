package com.ahmetkaraaslan.labx.utils

import com.ahmetkaraaslan.labx.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = com.ahmetkaraaslan.labx.BuildConfig.GEMINI_API_KEY
        )
    }

    suspend fun getResponse(query: String): String {
        return try {
            val prompt = """Sen bir kimya laboratuvarı asistanısın. Cevapların her zaman kısa, eğitici ve bir öğrenciye uygun olsun. Kimya dışında bir soru gelirse, 'Bu benim uzmanlık alanımın dışında.' diye cevap ver. 

Soru: $query"""
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Yapay zekadan bir cevap alınamadı."
        } catch (e: Exception) {
            "Hata: ${e.localizedMessage}"
        }
    }
}