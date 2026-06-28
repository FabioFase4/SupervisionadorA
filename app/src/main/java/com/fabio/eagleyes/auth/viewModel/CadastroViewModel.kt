package com.fabio.eagleyes.auth.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.fabio.eagleyes.auth.model.Usuario
import com.fabio.eagleyes.auth.repository.CadastroRepository

import kotlinx.coroutines.launch

class CadastroViewModel : ViewModel() {
    private val repository = CadastroRepository()

    private val _sucesso = MutableLiveData<String?>()
    val sucesso: LiveData<String?> get() = _sucesso

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    fun cadastrar(usuario: Usuario, noDestino: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val sucesso = repository.salvarUsuario(usuario, noDestino)
                if (sucesso) {
                    _sucesso.value = noDestino
                } else {
                    _erro.value = "Erro ao processar o cadastro. Tente novamente."
                }
            } catch (e: Exception) {
                _erro.value = e.localizedMessage ?: "Erro desconhecido no cadastro."
            } finally {
                _carregando.value = false
            }
        }
    }
}