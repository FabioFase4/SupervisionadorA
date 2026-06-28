package com.fabio.eagleyes.auth.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleyes.R
import com.fabio.eagleyes.auth.model.Usuario
import com.fabio.eagleyes.auth.viewModel.CadastroViewModel
import com.fabio.eagleyes.pai.HomePaiActivity

class CadastroPaiActivity : AppCompatActivity() {
    private lateinit var viewModel: CadastroViewModel
    private lateinit var btnCadastrar: Button
    private lateinit var btnVoltar: Button
    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var editTelefone: EditText
    private lateinit var editCPF: EditText
    private lateinit var rgGenero: RadioGroup
    private lateinit var editQntFilhos: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_pai)

        viewModel = ViewModelProvider(this).get(CadastroViewModel::class.java)

        configurarViews()
        configurarObservers()
        configurarCliques()
    }

    private fun cadastrar() {
        val nome = editNome.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString().trim()
        val telefone = editTelefone.text.toString().trim()
        val cpf = editCPF.text.toString().trim()
        val qntFilhos = editQntFilhos.text.toString().trim()

        val genero = when (rgGenero.checkedRadioButtonId) {
            R.id.rbMasculino -> "Masculino"
            R.id.rbFeminino -> "Feminino"
            else -> "Outro"
        }

        if (!validarCampos(nome, email, senha, telefone, cpf, qntFilhos)) return

        btnCadastrar.isEnabled = false

        val novoUsuario = Usuario(
            nome = nome,
            email = email,
            senha = senha,
            telefone = telefone,
            cpf = cpf,
            genero = genero,
            qnt_filhos = qntFilhos,
            emailResponsavel = "",
            tipo = "Pai"
        )

        viewModel.cadastrar(novoUsuario, "usuarios/pais")
    }


    private fun validarCampos(nome: String, email: String, senha: String, telefone: String, cpf: String, qntFilhos: String): Boolean {
        val avisoErro: String? = when {
            nome.isEmpty() || email.isEmpty() || senha.isEmpty() || telefone.isEmpty() || cpf.isEmpty() || qntFilhos.isEmpty() -> "Preencha todos os campos!"
            !email.contains("@") || !email.contains(".") -> "E-mail inválido!"
            senha.length < 6 -> "Senha deve ter no mínimo 6 caracteres!"
            telefone.length < 11 -> "Telefone inválido!"
            cpf.length < 11 -> "CPF inválido!"
            else -> null
        }

        if (avisoErro != null) {
            Toast.makeText(this, avisoErro, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun configurarViews() {
        btnCadastrar = findViewById(R.id.btnCadastrar)
        btnVoltar = findViewById(R.id.btnVoltar)
        editNome = findViewById(R.id.edtNome)
        editEmail = findViewById(R.id.edtEmail)
        editSenha = findViewById(R.id.edtSenha)
        editTelefone = findViewById(R.id.edtTelefone)
        editCPF = findViewById(R.id.edtCPF)
        editQntFilhos = findViewById(R.id.edtQntFilhos)
        rgGenero = findViewById(R.id.rgGenero)
    }

    private fun configurarCliques() {
        btnCadastrar.setOnClickListener { cadastrar() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun configurarObservers() {
        viewModel.sucesso.observe(this) { tipo ->
            if (tipo != null) {
                Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomePaiActivity::class.java))
                finish()
            }
        }
        viewModel.erro.observe(this) { mensagem ->
            btnCadastrar.isEnabled = true
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
    }
}