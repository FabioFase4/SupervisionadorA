package com.fabio.supervisionador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.fabio.supervisionador.data.viewModel.UsuarioViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: UsuarioViewModel

    private lateinit var btnLogin: Button
    private lateinit var editEmail: android.widget.EditText
    private lateinit var editSenha: android.widget.EditText
    private lateinit var btnCadastro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        viewModel = ViewModelProvider(this).get(UsuarioViewModel::class.java)

        configurarViews()
        configurarObservers()
        configurarCliques()
    }

    private fun configurarViews() {
        btnLogin = findViewById(R.id.btnLogin)
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        btnCadastro = findViewById(R.id.btnCadastro)
    }

    private fun configurarCliques() {
        btnCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (validarCampos(email, senha)) {
                btnLogin.isEnabled = false
                viewModel.logar(email, senha)
            }
        }
    }

    private fun configurarObservers() {
        viewModel.sucesso.observe(this) { tipo ->
            if (tipo != null) {
                val intent = when (tipo) {
                    "Pai" -> Intent(this, HomePaiActivity::class.java)
                    "Filho" -> Intent(this, HomeFilhoActivity::class.java)
                    else -> null
                }

                if (intent != null) {
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Perfil não reconhecido", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                }
            }
        }

        viewModel.erro.observe(this) { mensagem ->
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
            btnLogin.isEnabled = true
        }
    }

    private fun validarCampos(email: String, senha: String): Boolean {
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}