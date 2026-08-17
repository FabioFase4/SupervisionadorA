package com.fabio.eagleeyes.filho

import android.util.Log
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.funcionalidades.historico.HistoricoUso
import com.fabio.eagleeyes.funcionalidades.regra.Regra

import kotlinx.coroutines.tasks.await

class FilhoDAO {
    private val rootDb = FirebaseConfig.getDatabase()

    private fun String.toFirebaseKey(): String = this.replace(".", ",")

    suspend fun buscarNomeFilho(uid: String): String? {
        return try {
            val snapshot = rootDb.child("usuarios").child("filhos").child(uid).get().await()
            snapshot.child("nome").getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarRegra(regra: Regra): Boolean {
        return try {
            val emailChave = regra.emailFilho.toFirebaseKey()
            val ref = rootDb.child("regras").child(emailChave)
            
            val chave = if (regra.id.isEmpty()) {
                ref.push().key ?: return false
            } else {
                regra.id
            }
            
            regra.id = chave
            ref.child(chave).setValue(regra).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun excluirRegra(emailFilho: String, idRegra: String): Boolean {
        return try {
            val emailChave = emailFilho.toFirebaseKey()
            rootDb.child("regras").child(emailChave).child(idRegra).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun buscarRegras(emailFilho: String): List<Regra> {
        return try {
            val emailChave = emailFilho.toFirebaseKey()
            val snapshot = rootDb.child("regras").child(emailChave).get().await()
            val lista = mutableListOf<Regra>()
            if (snapshot.exists()) {
                for (item in snapshot.children) {
                    item.getValue(Regra::class.java)?.let { lista.add(it) }
                }
            }
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarHistorico(uidFilho: String): List<HistoricoUso> {
        return try {
            // CAMINHO CORRIGIDO para bater com o MonitoramentoService
            val snapshot = FirebaseConfig.getFilhosRef()
                .child(uidFilho)
                .child("historicoUso")
                .get()
                .await()
            
            val lista = mutableListOf<HistoricoUso>()
            
            if (snapshot.exists()) {
                // Navega pelas datas (ex: 2024-05-22)
                for (diaSnapshot in snapshot.children) {
                    val dataStr = diaSnapshot.key ?: ""
                    
                    // Navega pelos apps de cada data
                    for (appSnapshot in diaSnapshot.children) {
                        val packageName = appSnapshot.key?.replace("_", ".") ?: ""
                        val tempoMs = appSnapshot.value as? Long ?: 0
                        val tempoMinutos = tempoMs / (1000 * 60)
                        
                        if (tempoMinutos > 0) {
                            lista.add(HistoricoUso(
                                nomeApp = packageName,
                                tempoGasto = tempoMinutos,
                                data = dataStr
                            ))
                        }
                    }
                }
            }
            // Retorna os mais recentes primeiro
            lista.sortedByDescending { it.data }
        } catch (e: Exception) {
            Log.e("FilhoDAO", "Erro ao buscar histórico: ${e.message}")
            emptyList()
        }
    }

    suspend fun registrarAtividade(uidFilho: String, historico: HistoricoUso): Boolean {
        return try {
            val ref = FirebaseConfig.getFilhosRef()
                .child(uidFilho)
                .child("historicoUso")
                .child(historico.data)
            
            val packageChave = historico.nomeApp.replace(".", "_")
            val tempoMs = historico.tempoGasto * 60 * 1000
            
            ref.child(packageChave).setValue(tempoMs).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun gerarHistoricoTeste(uidFilho: String): Boolean {
        return try {
            val data = "2024-05-22"
            val ref = FirebaseConfig.getFilhosRef().child(uidFilho).child("historicoUso").child(data)
            
            val dadosTeste = mapOf(
                "com_instagram_android" to 45 * 60 * 1000L,
                "com_google_android_youtube" to 120 * 60 * 1000L,
                "com_whatsapp" to 30 * 60 * 1000L
            )
            
            ref.setValue(dadosTeste).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
