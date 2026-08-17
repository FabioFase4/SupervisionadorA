package com.fabio.eagleeyes.usuario

class UsuarioFilho (
    nome: String,
    email: String,
    senha: String,
    telefone: String,
    cpf: String,
    genero: String,
    emailResponsavel: String // Campo obrigatório para Filho
) : Usuario(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    genero = genero,
    emailResponsavel = emailResponsavel,
    tipo = "Filho"
)
