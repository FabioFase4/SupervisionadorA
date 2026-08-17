package com.fabio.eagleeyes.pai.home

import com.fabio.eagleeyes.usuario.Usuario

class HomePaiRepository(private val dao: HomePaiDAO) {
    suspend fun listarFilhos(emailDoPai: String): List<Usuario> {
        return dao.listarFilhos(emailDoPai)
    }
}
