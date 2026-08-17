package com.fabio.eagleeyes.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.SessionManager
import com.fabio.eagleeyes.cadastro.CadastroOpcoesActivity
import com.fabio.eagleeyes.login.LoginDAO
import com.fabio.eagleeyes.login.LoginViewModelFactory
import com.fabio.eagleeyes.login.LoginRepository
import com.fabio.eagleeyes.login.LoginViewModel
import com.fabio.eagleeyes.filho.home.HomeFilhoActivity
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.home.HomePaiActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var btnCadastrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o gerenciador de sessão
        sessionManager = SessionManager(this)

        // Verifica a sessão antes de desenhar a tela
        if (verificarSessaoEAlmejarRedirecionamento()) return

        setContentView(R.layout.activity_login)

        val dao = LoginDAO()
        val repository = LoginRepository(dao)
        val factory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        configurarViews()
        configurarObservers()
        configurarCliques()

        // Recupera o e-mail usando o novo manager
        editEmail.setText(sessionManager.recuperarEmail())
    }

    private fun verificarSessaoEAlmejarRedirecionamento(): Boolean {
        val user = FirebaseConfig.getAuth().currentUser

        if (user != null) {
            val tipo = sessionManager.recuperarTipoUsuario()
            val email = sessionManager.recuperarEmail()

            val destino = when (tipo.lowercase()) {
                "pai" -> HomePaiActivity::class.java
                "filho" -> HomeFilhoActivity::class.java
                else -> null
            }

            if (destino != null) {
                val intent = Intent(this, destino).apply {
                    putExtra("USER_EMAIL", email)
                }
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }

    private fun configurarCliques() {
        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (validarCampos(email, senha)) {
                viewModel.logar(email, senha)
            }
        }

        btnCadastrar.setOnClickListener {
            startActivity(Intent(this, CadastroOpcoesActivity::class.java))
        }
    }

    private fun configurarViews() {
        btnEntrar = findViewById(R.id.btnEntrar)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        editEmail = findViewById(R.id.edtEmail)
        editSenha = findViewById(R.id.edtSenha)
    }

    private fun configurarObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            btnEntrar.isEnabled = !estaCarregando
            editEmail.isEnabled = !estaCarregando
            editSenha.isEnabled = !estaCarregando
        }

        viewModel.sucesso.observe(this) { tipo ->
            tipo?.let {
                val emailLogado = editEmail.text.toString().trim().lowercase()

                // Salva os dados usando o novo manager
                sessionManager.salvarDadosLogin(emailLogado, it)

                val destino = when (it.lowercase()) {
                    "pai" -> HomePaiActivity::class.java
                    "filho" -> HomeFilhoActivity::class.java
                    else -> null
                }

                if (destino != null) {
                    val intent = Intent(this, destino).apply {
                        putExtra("USER_EMAIL", emailLogado)
                    }
                    startActivity(intent)
                    viewModel.limparEstado()
                    finish()
                }
            }
        }

        viewModel.erro.observe(this) { mensagem ->
            mensagem?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCampos(email: String, senha: String): Boolean {
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}