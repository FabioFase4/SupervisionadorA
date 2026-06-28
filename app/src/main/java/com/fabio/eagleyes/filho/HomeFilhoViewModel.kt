package com.fabio.eagleyes.filho

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.repositorios.UsoRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HomeFilhoViewModel(private val usoRepositorio: UsoRepositorio) : ViewModel() {

    private val _respostaIA = MutableLiveData<String>()
    val respostaIA: LiveData<String> get() = _respostaIA

    private val _carregandoIA = MutableLiveData<Boolean>()
    val carregandoIA: LiveData<Boolean> get() = _carregandoIA

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    init {
        val user = FirebaseConfig.getAuth().currentUser
        _userEmail.value = user?.email ?: "email@filho.com"
        _userName.value = user?.displayName ?: user?.email?.substringBefore("@") ?: "Usuário"
    }

    fun realizarAnaliseIA() {
        val stats = usoRepositorio.buscarDadosUso()
        if (stats.isEmpty()) {
            _respostaIA.value = "Nenhum dado de uso encontrado nas últimas 24h."
            return
        }

        var textoFinal = ""
        for (app in stats) {
            val nome = app.first
            val tempoApp = usoRepositorio.formatarTempo(app.second)
            textoFinal += "$nome ficou por $tempoApp\n"
        }

        val prompt = "O texto abaixo indica o tempo de uso de tela dos aplicativos no período das últimas 24 horas. Analise estes números e me indique o principal ponto de atenção, caso tenha algum sinal negativo de uso. Liste os principais apps da lista com o tempo de tela de forma resumida, focando APENAS em REDES SOCIAIS:\n$textoFinal"

        geminiAnalise(prompt)
    }

    private fun geminiAnalise(prompt: String) {
        _carregandoIA.value = true
        _respostaIA.value = "Carregando análise do servidor seguro..."

        val urlBackend = "https://gemini-render-backend.onrender.com/chat"

        viewModelScope.launch {
            try {
                val jsonObjeto = JSONObject()
                jsonObjeto.put("mensagem", prompt)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonObjeto.toString().toRequestBody(mediaType)

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(urlBackend)
                    .post(body)
                    .build()

                val responseText = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use "Erro no servidor: ${response.code}"

                        val respostaServidor = response.body?.string() ?: ""
                        val jsonResposta = JSONObject(respostaServidor)
                        jsonResposta.optString("resposta", "Sem resposta do Gemini")
                    }
                }

                _respostaIA.value = responseText

            } catch (e: Exception) {
                _respostaIA.value = "Erro ao conectar com o servidor: ${e.message}"
                e.printStackTrace()
            } finally {
                _carregandoIA.value = false
            }
        }
    }
}