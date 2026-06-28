package com.fabio.eagleyes.auth.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.fabio.eagleyes.auth.repository.LoginRepository
import com.fabio.eagleyes.auth.viewModel.LoginViewModel

class LoginViewModelFactory(private val repository: LoginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}