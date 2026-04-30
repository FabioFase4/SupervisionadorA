package com.fabio.supervisionador.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fabio.supervisionador.data.model.rnUsuarios
import com.fabio.supervisionador.data.repositorios.rnUsuariosRepositorio
import com.fabio.supervisionador.data.dao.UsuarioDAO

class rnUsuariosViewModel : ViewModel() {
    private val dao = UsuarioDAO()

    val loginSucesso = MutableLiveData<String>()
    val erro = MutableLiveData<String>()

    fun realizarLogin(email: String, senha: String) {
        viewModelScope.launch {
            val tipo = dao.logar(email, senha)

            if (tipo != null) {
                loginSucesso.value = tipo
            } else {
                erro.value = "Falha no login ou perfil inexistente."
            }
        }
    }
}