package com.fabio.eagleyes.filho

import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.funcionalidades.historico.HistoricoUso
import com.fabio.eagleyes.funcionalidades.regra.Regra
import com.google.firebase.database.GenericTypeIndicator

import kotlinx.coroutines.tasks.await

class FilhoDAO {
    private val rootDb = FirebaseConfig.getDatabase()

    suspend fun gerarHistoricoTeste(emailFilho: String): Boolean {
        val emailChave = emailFilho.replace(".", ",")
        val listaTeste = listOf(
            HistoricoUso("Uso normal", System.currentTimeMillis(), "Instagram", 45, "2023-10-27"),
            HistoricoUso(
                "Uso normal",
                System.currentTimeMillis() - 3600000,
                "YouTube",
                120,
                "2023-10-27"
            ),
            HistoricoUso(
                "Uso normal",
                System.currentTimeMillis() - 7200000,
                "WhatsApp",
                15,
                "2023-10-27"
            ),
            HistoricoUso(
                "Bloqueado",
                System.currentTimeMillis() - 10800000,
                "TikTok",
                5,
                "2023-10-27"
            )
        )

        return try {
            val ref = rootDb.child("historico_uso").child(emailChave)
            ref.removeValue().await()
            listaTeste.forEach { ref.push().setValue(it).await() }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registrarAtividade(uidFilho: String, historico: HistoricoUso): Boolean {
        return try {
            val chave = uidFilho.replace(".", ",")
            rootDb.child("historico_uso")
                .child(chave)
                .push()
                .setValue(historico)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun salvarRegra(regra: Regra): Boolean {
        return try {
            val emailChave = regra.emailFilho.replace(".", ",")
            val ref = rootDb.child("regras").child(emailChave)
            val novaChave = ref.push().key ?: return false
            regra.id = novaChave
            ref.child(novaChave).setValue(regra).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun buscarRegras(uidFilho: String): Map<String, Any>? {
        return try {
            val chave = uidFilho.replace(".", ",")
            val snapshot = rootDb.child("regras")
                .child(chave)
                .get()
                .await()

            if (snapshot.exists()) {
                val t = object : GenericTypeIndicator<Map<String, Any>>() {}
                snapshot.getValue(t)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun buscarHistorico(uidFilho: String): List<HistoricoUso> {
        return try {
            val chave = uidFilho.replace(".", ",")
            val snapshot = rootDb.child("historico_uso")
                .child(chave)
                .get()
                .await()

            val lista = mutableListOf<HistoricoUso>()
            if (snapshot.exists()) {
                for (item in snapshot.children) {
                    val historico = item.getValue(HistoricoUso::class.java)
                    if (historico != null) {
                        lista.add(historico)
                    }
                }
            }
            lista.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }
}