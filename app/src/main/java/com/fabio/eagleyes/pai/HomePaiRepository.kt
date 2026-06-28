package com.fabio.eagleyes.pai

import com.fabio.eagleyes.auth.model.Usuario

class HomePaiRepository(private val dao: HomePaiDAO) {
    suspend fun listarFilhos(emailDoPai: String): List<Usuario> {
        return dao.listarFilhos(emailDoPai)
    }
}
