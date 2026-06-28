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
import com.fabio.eagleyes.filho.HomeFilhoActivity

class CadastroFilhoActivity : AppCompatActivity() {
    private lateinit var viewModel: CadastroViewModel
    private lateinit var btnCadastrar: Button
    private lateinit var btnVoltar: Button
    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var editTelefone: EditText
    private lateinit var editCPF: EditText
    private lateinit var rgGenero: RadioGroup
    private lateinit var emailResponsavel: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_filho)

        viewModel = ViewModelProvider(this).get(CadastroViewModel::class.java)

        configurarViews()
        configurarObservers()
        configurarCliques()
    }

    private fun configurarViews() {
        btnCadastrar = findViewById(R.id.btnCadastrar)
        editNome = findViewById(R.id.edtNome)
        editEmail = findViewById(R.id.edtEmail)
        editSenha = findViewById(R.id.edtSenha)
        editTelefone = findViewById(R.id.edtTelefone)
        editCPF = findViewById(R.id.edtCPF)
        rgGenero = findViewById(R.id.rgGenero)
        btnVoltar = findViewById(R.id.btnVoltar)
        emailResponsavel = findViewById(R.id.edtEmailResponsavel)

        if(intent.hasExtra("EMAIL_PAI")) {
            emailResponsavel.setText(intent.getStringExtra("EMAIL_PAI"))
            emailResponsavel.isEnabled = false
        }
    }

    private fun configurarCliques() {
        btnCadastrar.setOnClickListener { cadastrar() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun cadastrar() {
        val nome = editNome.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString().trim()
        val telefone = editTelefone.text.toString().trim()
        val cpf = editCPF.text.toString().trim()
        val resp = emailResponsavel.text.toString().trim()

        val genero = when (rgGenero.checkedRadioButtonId) {
            R.id.rbMasculino -> "Masculino"
            R.id.rbFeminino -> "Feminino"
            else -> "Outro"
        }

        if (!validarCampos(nome, email, senha, telefone, cpf, resp)) return

        btnCadastrar.isEnabled = false

        val novoUsuario = Usuario(
            nome = nome,
            email = email,
            senha = senha,
            telefone = telefone,
            cpf = cpf,
            genero = genero,
            qnt_filhos = "0",
            emailResponsavel = resp,
            tipo = "Filho"
        )

        viewModel.cadastrar(novoUsuario, "usuarios/filhos")
    }

    private fun validarCampos(nome: String, email: String, senha: String, telefone: String, cpf: String, resp: String): Boolean {
        val erro = when {
            nome.isEmpty() || email.isEmpty() || senha.isEmpty() || telefone.isEmpty() || cpf.isEmpty() || resp.isEmpty() -> "Preencha todos os campos!"
            !email.contains("@") || !email.contains(".") -> "E-mail do filho inválido!"
            !resp.contains("@") || !resp.contains(".") -> "E-mail do responsável inválido!"
            senha.length < 6 -> "A senha deve ter pelo menos 6 caracteres!"
            telefone.length < 11 -> "Telefone inválido!"
            cpf.length < 11 -> "CPF inválido!"
            else -> null
        }

        if (erro != null) {
            Toast.makeText(this, erro, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun configurarObservers() {
        viewModel.sucesso.observe(this) { caminho ->
            if (caminho != null) {
                Toast.makeText(this, "Filho cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeFilhoActivity::class.java))
                finish()
            }
        }
        viewModel.erro.observe(this) { mensagem ->
            btnCadastrar.isEnabled = true
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
    }
}