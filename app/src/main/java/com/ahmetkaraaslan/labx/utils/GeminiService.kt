package com.ahmetkaraaslan.labx.utils

import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyCfqu4gm7gT4yQiSTzE96ILnZCRgqwbpeY"
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