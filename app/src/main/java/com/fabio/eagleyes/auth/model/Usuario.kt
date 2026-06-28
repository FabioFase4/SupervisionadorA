package com.fabio.eagleyes.auth.model

data class Usuario (
    var uid: String = "",
    var nome: String = "",
    var email: String = "",
    var senha: String = "",
    var telefone: String = "",
    var cpf: String = "",
    var genero: String = "",
    var qnt_filhos: String = "",
    var emailResponsavel: String = "",
    var tipo: String = ""
)