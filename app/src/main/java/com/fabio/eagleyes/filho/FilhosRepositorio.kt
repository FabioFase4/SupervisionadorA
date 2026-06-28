package com.fabio.eagleyes.filho

import com.fabio.eagleyes.funcionalidades.historico.HistoricoUso
import com.fabio.eagleyes.funcionalidades.regra.Regra

class FilhosRepositorio(private val dao: FilhoDAO = FilhoDAO()) {

    suspend fun buscarHistorico(uidFilho: String): List<HistoricoUso> {
        return dao.buscarHistorico(uidFilho)
    }

    suspend fun registrarAtividade(uidFilho: String, historico: HistoricoUso): Boolean {
        return dao.registrarAtividade(uidFilho, historico)
    }

    suspend fun gerarHistoricoTeste(emailFilho: String): Boolean {
        return dao.gerarHistoricoTeste(emailFilho)
    }

    suspend fun buscarRegras(uidFilho: String): Map<String, Any>? {
        return dao.buscarRegras(uidFilho)
    }

    suspend fun salvarRegra(regra: Regra): Boolean {
        return dao.salvarRegra(regra)
    }
}