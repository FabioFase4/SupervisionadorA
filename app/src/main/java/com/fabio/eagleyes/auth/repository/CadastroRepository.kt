package com.fabio.eagleyes.auth.repository

import com.fabio.eagleyes.auth.dao.CadastroDAO
import com.fabio.eagleyes.auth.model.Usuario

class CadastroRepository {
    private val cadastroDAO = CadastroDAO()

    suspend fun salvarUsuario(usuario: Usuario, noDestino: String): Boolean
    {
        return cadastroDAO.salvarUsuario(usuario, noDestino)
    }
}