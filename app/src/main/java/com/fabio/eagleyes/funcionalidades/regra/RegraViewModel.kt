package com.fabio.eagleyes.funcionalidades.regra

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.fabio.eagleyes.filho.FilhosRepositorio

import kotlinx.coroutines.launch

class RegraViewModel(private val repository: FilhosRepositorio = FilhosRepositorio()) : ViewModel() {

    private val _sucesso = MutableLiveData<Boolean>()
    val sucesso: LiveData<Boolean> get() = _sucesso

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    fun salvarRegra(regra: Regra) {
        if (regra.nomeApp.isEmpty() || regra.valor.isEmpty()) {
            _erro.value = "Preencha todos os campos!"
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.salvarRegra(regra)
                if (result) {
                    _sucesso.value = true
                } else {
                    _erro.value = "Erro ao salvar a regra no banco."
                }
            } catch (e: Exception) {
                _erro.value = e.localizedMessage ?: "Erro desconhecido."
            }
        }
    }
}