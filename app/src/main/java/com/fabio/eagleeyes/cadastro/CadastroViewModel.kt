package com.fabio.eagleeyes.cadastro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.usuario.UsuarioFilho
import com.fabio.eagleeyes.usuario.UsuarioPai
import com.fabio.eagleeyes.usuario.UsuarioRepositorio
import kotlinx.coroutines.launch

class CadastroViewModel(private val repositorio: UsuarioRepositorio) : ViewModel() {

    private val _carregando = MutableLiveData(false)
    val carregando: LiveData<Boolean> = _carregando

    private val _sucesso = MutableLiveData<String?>()
    val sucesso: LiveData<String?> = _sucesso

    private val _erro = MutableLiveData<String?>()
    val erro: LiveData<String?> = _erro

    fun cadastrarPai(usuario: UsuarioPai, noDestino: String) {
        cadastrar(usuario, "Pai", noDestino)
    }

    fun cadastrarFilho(usuario: UsuarioFilho, noDestino: String) {
        cadastrar(usuario, "Filho", noDestino)
    }

    private fun cadastrar(usuario: Usuario, tipo: String, noDestino: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val uid = repositorio.cadastrar(usuario, "usuarios/$noDestino")
                if (uid != null) {
                    _sucesso.value = noDestino
                } else {
                    _erro.value = "Erro ao cadastrar usuário $tipo. Tente novamente."
                }
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message ?: "Desconhecido"}"
            } finally {
                _carregando.value = false
            }
        }
    }
}
