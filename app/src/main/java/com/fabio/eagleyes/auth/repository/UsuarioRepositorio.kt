package com.fabio.eagleyes.auth.repository

import com.fabio.eagleyes.auth.dao.UsuarioDAO
import com.fabio.eagleyes.auth.model.Usuario

class UsuarioRepositorio {
    private val usuarioDao = UsuarioDAO()

    suspend fun login (email: String, senha: String): String?
    {
        return usuarioDao.login(email, senha)
    }

    suspend fun cadastrar (usuario: Usuario, local: String): String?
    {
        return usuarioDao.cadastrar(usuario, local)
    }
}