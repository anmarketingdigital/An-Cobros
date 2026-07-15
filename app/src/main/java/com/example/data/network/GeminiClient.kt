package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generatePerformanceReport(
        clientName: String,
        service: String,
        impressions: Int,
        clicks: Int,
        conversions: Int,
        spent: Double,
        additionalNotes: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is placeholder or missing. Using local template fallback.")
            return@withContext generateLocalReport(clientName, service, impressions, clicks, conversions, spent, additionalNotes)
        }

        val ctr = if (impressions > 0) (clicks.toDouble() / impressions) * 100 else 0.0
        val cpa = if (conversions > 0) spent / conversions else 0.0

        val prompt = """
            Eres un Director de Marketing Digital de la agencia 'AN Marketing Digital'.
            Genera un reporte mensual de rendimiento ejecutivo, elegante y profesional en español para nuestro cliente '$clientName' para el servicio '$service'.
            
            Métricas de este periodo:
            - Impresiones: $impressions
            - Clics: $clicks
            - Conversiones (Leads/Ventas): $conversions
            - Presupuesto Invertido: $spent USD
            - CTR (Tasa de clics): ${String.format("%.2f", ctr)}%
            - CPA (Costo por Conversión): ${String.format("%.2f", cpa)} USD
            ${if (additionalNotes.isNotBlank()) "- Notas adicionales del periodo: $additionalNotes" else ""}
            
            Por favor, estructura el reporte para que sea fácil de copiar y enviar por WhatsApp o correo. Incluye:
            1. 📊 *REPORTE DE RENDIMIENTO - AN MARKETING DIGITAL* 📊 (Un título formal)
            2. 📈 *RESUMEN EJECUTIVO*: Un breve párrafo destacando el impacto de la campaña de forma positiva.
            3. 🎯 *MÉTRICAS CLAVE*: Presenta las métricas anteriores de forma ordenada usando emojis apropiados.
            4. 💡 *RECOMENDACIONES*: 2 o 3 sugerencias concretas de optimización o expansión del presupuesto para mejorar los resultados el próximo mes.
            
            Usa un tono sumamente profesional, de confianza y motivador para incentivar al cliente a continuar invirtiendo.
        """.trimIndent()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyJson = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val contentObject = JSONObject().apply {
                        val partsArray = org.json.JSONArray().apply {
                            val partObject = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObject)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObject)
                }
                put("contents", contentsArray)
                
                // Add system instructions if needed, or simply let the model generate directly
                val generationConfig = JSONObject().apply {
                    put("temperature", 0.7)
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Call failed with status ${response.code}: $errorBody")
                    return@withContext generateLocalReport(clientName, service, impressions, clicks, conversions, spent, additionalNotes)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")
                
                return@withContext text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating report with Gemini, falling back.", e)
            return@withContext generateLocalReport(clientName, service, impressions, clicks, conversions, spent, additionalNotes)
        }
    }

    fun generateBillingReminder(
        clientName: String,
        service: String,
        fee: Double,
        billingDay: Int,
        tone: String // "friendly", "formal", "urgent"
    ): String {
        val greeting = when (tone) {
            "friendly" -> "¡Hola $clientName! 😊 Espero que estés teniendo un excelente día."
            "formal" -> "Estimado/a $clientName, reciba un cordial saludo de AN Marketing Digital."
            else -> "Hola $clientName, esperamos que se encuentre muy bien."
        }

        val messageBody = when (tone) {
            "friendly" -> "Te escribimos de AN Marketing Digital para recordarte que se aproxima el vencimiento de tu mensualidad por el servicio de *$service* el día *$billingDay* de este mes, por un monto de *$fee USD*.\n\nTe agradecemos de antemano tu apoyo para continuar impulsando tu negocio al siguiente nivel. 🚀"
            "formal" -> "Por medio de la presente, le recordamos que la facturación de su servicio de *$service* vence el próximo *$billingDay* de este mes. El importe correspondiente es de *$fee USD*.\n\nAgradecemos su puntualidad en el pago para asegurar la continuidad de sus campañas publicitarias y optimizaciones en curso."
            else -> "*Recordatorio de Pago Importante*\n\nLe notificamos de manera atenta que el pago de su servicio de *$service* presenta un saldo pendiente de *$fee USD*, con fecha de vencimiento el día *$billingDay* de este mes.\n\nLe solicitamos realizar el depósito a la brevedad para evitar la suspensión de las campañas de marketing digital en curso. Quedamos a su disposición para cualquier duda."
        }

        return """
            $greeting
            
            $messageBody
            
            💳 *Métodos de pago aceptados:* Transferencia bancaria o PayPal.
            
            Si ya realizaste tu pago, por favor envía tu comprobante por este medio para registrarlo. ¡Muchas gracias por tu confianza!
            
            Atentamente,
            *AN Marketing Digital*
        """.trimIndent()
    }

    private fun generateLocalReport(
        clientName: String,
        service: String,
        impressions: Int,
        clicks: Int,
        conversions: Int,
        spent: Double,
        additionalNotes: String
    ): String {
        val ctr = if (impressions > 0) (clicks.toDouble() / impressions) * 100 else 0.0
        val cpa = if (conversions > 0) spent / conversions else 0.0

        return """
            📊 *REPORTE DE RENDIMIENTO - AN MARKETING DIGITAL* 📊
            
            Estimado/a *$clientName*, compartimos el reporte mensual para su servicio de *$service*:
            
            📈 *RESUMEN EJECUTIVO*
            Durante este período, se mantuvieron optimizaciones constantes en las campañas de marketing, logrando canalizar de manera eficiente el presupuesto asignado para capturar la atención de su público objetivo y generar leads de valor.
            
            🎯 *MÉTRICAS CLAVE*
            - 👁️ *Impresiones:* $impressions (Alcance de marca en plataformas digitales)
            - 🖱️ *Clics:* $clicks (Interés directo en las publicaciones/anuncios)
            - 📈 *CTR:* ${String.format("%.2f", ctr)}% (Tasa de efectividad de clics)
            - 📥 *Conversiones:* $conversions (Contactos calificados / Ventas generadas)
            - 💸 *Inversión:* $spent USD (Presupuesto invertido en pauta)
            - 🎯 *Costo por Lead/Conversión (CPA):* ${String.format("%.2f", cpa)} USD
            
            ${if (additionalNotes.isNotBlank()) "📝 *NOTAS DEL PERIODO*\n$additionalNotes\n" else ""}
            💡 *RECOMENDACIONES DE OPTIMIZACIÓN*
            - Ajustar la segmentación de la audiencia para potenciar el CTR y reducir el costo por conversión.
            - Realizar pruebas A/B en las creatividades de los anuncios para identificar los mensajes con mayor enganche.
            - Mantener o incrementar paulatinamente el presupuesto publicitario diario para maximizar el volumen de leads semanales.
            
            Agradecemos enormemente su confianza en *AN Marketing Digital*. ¡Seguimos trabajando juntos por el éxito de su negocio!
        """.trimIndent()
    }
}
