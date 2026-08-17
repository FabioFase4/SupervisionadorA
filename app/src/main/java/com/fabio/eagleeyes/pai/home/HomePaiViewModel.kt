package com.fabio.eagleeyes.pai.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.repositorio.AnaliseRepositorio
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomePaiViewModel(
    private val repository: HomePaiRepository,
    analiseRepositorio: AnaliseRepositorio = AnaliseRepositorio()
) : HomeViewModel(analiseRepositorio) {

    private val _listaFilhos = MutableLiveData<List<Usuario>>()
    val listaFilhos: LiveData<List<Usuario>> get() = _listaFilhos

    fun carregarFilhos(emailPai: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val filhos = repository.listarFilhos(emailPai)
                _listaFilhos.value = filhos
            } catch (e: Exception) {
                _erro.value = "Erro ao buscar filhos: ${e.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }

    fun analisarComportamentoComIA(pergunta: String) {
        val filhos = _listaFilhos.value
        if (filhos.isNullOrEmpty()) {
            _erro.value = "Nenhum filho cadastrado para analisar."
            return
        }

        viewModelScope.launch {
            _carregando.value = true
            try {
                val resumoUso = coletarResumoUsoFilhos(filhos)
                val promptFinal = """
                    Você é o assistente EagleEyes.
                    Dados de uso do(s) filho(s):
                    $resumoUso

                    Pergunta do responsável: $pergunta

                    Responda de forma curta e prática.
                """.trimIndent()

                Log.d("IA_DEBUG", "Enviando prompt: $promptFinal")

                realizarAnaliseComIAParaPai(promptFinal)
            } catch (e: Exception) {
                _erro.value = "Erro na análise: ${e.message}"
            } finally {
                _carregando.value = false
            }
        }
    }

    private suspend fun coletarResumoUsoFilhos(filhos: List<Usuario>): String {
        var resumo = ""

        for (filho in filhos) {
            if (filho.uid.isEmpty()) {
                Log.w("HomePaiViewModel", "UID do filho ${filho.nome} está vazio. Pulando...")
                continue
            }

            try {
                // CAMINHO CORRIGIDO: Deve ser o mesmo onde o MonitoramentoService salva
                val snapshot = FirebaseConfig.getFilhosRef()
                    .child(filho.uid)
                    .child("historicoUso")
                    .get()
                    .await()

                if (snapshot.exists()) {
                    resumo += "Filho(a) ${filho.nome}:\n"
                    snapshot.children.forEach { diaSnapshot ->
                        val data = diaSnapshot.key ?: "Data desconhecida"
                        
                        // Soma o tempo de todos os apps do dia
                        var tempoTotalDia: Long = 0
                        diaSnapshot.children.forEach { appSnapshot ->
                            val tempoApp = appSnapshot.value as? Long ?: 0
                            tempoTotalDia += tempoApp
                        }
                        
                        resumo += "- $data: ${formatarTempo(tempoTotalDia)}\n"
                    }
                    resumo += "\n"
                } else {
                    resumo += "Filho(a) ${filho.nome}: Nenhum dado de uso encontrado no banco.\n\n"
                }
            } catch (e: Exception) {
                Log.e("HomePaiViewModel", "Erro ao buscar dados de uso do filho ${filho.nome}", e)
                resumo += "Filho(a) ${filho.nome}: Erro ao carregar dados de uso.\n\n"
            }
        }

        return if (resumo.isEmpty()) "Nenhum dado de uso disponível para os filhos cadastrados." else resumo
    }

    private fun formatarTempo(millis: Long): String {
        val segundos = millis / 1000
        val horas = segundos / 3600
        val minutos = (segundos % 3600) / 60
        return when {
            horas > 0 -> "$horas hora(s) e $minutos minuto(s)"
            minutos > 0 -> "$minutos minuto(s)"
            else -> "menos de 1 minuto"
        }
    }
}
