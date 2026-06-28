package com.fabio.eagleyes.funcionalidades.analiseIA

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AnaliseViewModel : ViewModel() {

    private val _respostaIA = MutableLiveData<String>()
    val respostaIA: LiveData<String> get() = _respostaIA

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    // Aumentando o timeout para evitar erro de conexão com a IA
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Configurações da API (Podem ser passadas por parâmetro se desejar)
    private val apiKey = "AQ.Ab8RN6IyK7AqD43nhhz8LQ9lm9Tf_vD1vm9FZfpVnUiXqGRUDQ"
    private val baseUrlDirect = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private val backendUrl = "https://gemini-render-backend.onrender.com/chat"

    fun analisar(prompt: String, usarBackend: Boolean = true) {
        _carregando.value = true
        viewModelScope.launch {
            try {
                val url = if (usarBackend) backendUrl else "$baseUrlDirect?key=$apiKey"
                val bodyJson = if (usarBackend) {
                    JSONObject().put("mensagem", prompt).toString()
                } else {
                    montarJsonDirect(prompt)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toRequestBody("application/json".toMediaType()))
                    .build()

                val responseText = withContext(Dispatchers.IO) {
                    try {
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                return@use when (response.code) {
                                    401 -> "Erro de autenticação. Verifique a chave da API."
                                    403 -> "Acesso negado. Verifique as permissões da conta."
                                    404 -> "Recurso não encontrado. Verifique a URL da API."
                                    429 -> "Limite de requisições excedido. Aguarde um minuto antes de tentar novamente."
                                    500 -> "Erro interno do servidor. Tente novamente mais tarde."
                                    503 -> "Serviço temporariamente indisponível. Tente novamente mais tarde."
                                    else -> "Erro desconhecido. Código de resposta: ${response.code}"
                                }
                            }
                            val body = response.body?.string() ?: ""
                            if (usarBackend)
                                JSONObject(body).optString("resposta", "Sem resposta")
                            else
                                extrairTextoDirect(body)
                        }
                    } catch (e: Exception) {
                        "Erro na requisição: ${e.message}"
                    }
                }
                _respostaIA.value = responseText
            } catch (e: Exception) {
                _respostaIA.value = "Erro ao conectar com a IA: ${e.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }

    private fun montarJsonDirect(prompt: String): String {
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }.toString()
    }

    private fun extrairTextoDirect(jsonBruto: String): String {
        return try {
            val json = JSONObject(jsonBruto)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Erro ao processar resposta da IA."
        }
    }
}
