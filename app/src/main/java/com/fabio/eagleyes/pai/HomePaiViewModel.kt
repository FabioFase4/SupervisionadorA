package com.fabio.eagleyes.pai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleyes.auth.model.Usuario
import kotlinx.coroutines.launch

class HomePaiViewModel(private val repository: HomePaiRepository) : ViewModel() {
    private val _listaFilhos = MutableLiveData<List<Usuario>>()
    val listaFilhos: LiveData<List<Usuario>> get() = _listaFilhos

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    fun carregarFilhos(emailPai: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val filhos = repository.listarFilhos(emailPai)
                _listaFilhos.value = filhos
            } catch (e: Exception) {
                _erro.value = "Erro ao buscar dados dos filhos: ${e.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }
}
