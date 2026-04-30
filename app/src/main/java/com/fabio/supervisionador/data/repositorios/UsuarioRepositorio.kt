package com.fabio.supervisionador.data.repositorios
import com.fabio.supervisionador.data.dao.UsuarioDAO
import com.fabio.supervisionador.data.model.rnUsuarios

class rnUsuariosRepositorio {
    private val usuarioDao = UsuarioDAO()

    fun cadastrarUsuario(uid: String, usuario: rnUsuarios, callback: (Boolean, String?) -> Unit) {
        val mensagemErro = validarDadosUsuario(usuario)

        if (mensagemErro == null) {
            usuarioDao.adicionarUsuario(uid, usuario) { sucesso ->
                if (sucesso) callback(true, null)
                else callback(false, "Erro ao conectar com o Firebase")
            }
        }
        else
            callback(false, mensagemErro)
    }

    private fun validarDadosUsuario (usuario: rnUsuarios): String?
    {
        if (usuario.nome.isEmpty()) return "O nome não pode estar vazio"
        else if (usuario.email.isEmpty() || !usuario.email.contains("@")) return "Email inválido"
        else if (usuario.senha.length < 6) return "A senha deve ter pelo menos 6 caracteres"
        else if (usuario.cpf.length != 11) return "CPF inválido"
        else if (usuario.telefone.isEmpty()) return "Telefone é obrigatório"
        else if (usuario.genero.isEmpty()) return "Gênero é obrigatório"
        else return null
    }
}