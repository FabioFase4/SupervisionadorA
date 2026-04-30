package com.fabio.supervisionador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.supervisionador.data.model.Usuario
import com.fabio.supervisionador.data.viewModel.UsuarioViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class CadastroActivity : AppCompatActivity() {
    private lateinit var viewModel: UsuarioViewModel
    private lateinit var btnCadastrar: Button
    private lateinit var editNome: android.widget.EditText
    private lateinit var editEmail: android.widget.EditText
    private lateinit var editSenha: android.widget.EditText
    private lateinit var editTelefone: android.widget.EditText
    private lateinit var editCPF: android.widget.EditText
    private lateinit var rgGenero: RadioGroup
    private lateinit var rgTipoUsuario: RadioGroup
    private lateinit var editQntFilhos: android.widget.EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro)

        viewModel = ViewModelProvider(this).get(UsuarioViewModel::class.java)

        configurarViews()
        configurarObservers()
        configurarCliques()
    }

    private fun configurarViews() {
        btnCadastrar = findViewById(R.id.btnCadastrar)
        editNome = findViewById(R.id.editNome)
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        editTelefone = findViewById(R.id.editTelefone)
        editCPF = findViewById(R.id.editCPF)
        rgGenero = findViewById(R.id.rgGenero)
        rgTipoUsuario = findViewById(R.id.rgTipoUsuario)
        editQntFilhos = findViewById(R.id.editQntFilhos)
    }

    private fun configurarCliques() {
        btnCadastrar.setOnClickListener {
            cadastrar()
        }
    }

    private fun cadastrar() {
        val nome = editNome.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString().trim()
        val telefone = editTelefone.text.toString().trim()
        val cpf = editCPF.text.toString().trim()
        val genero = if (rgGenero.checkedRadioButtonId == R.id.rbMasc) "Masculino" else "Feminino"
        val tipo = if (rgTipoUsuario.checkedRadioButtonId == R.id.rbPai) "Pai" else "Filho"
        val qntFilhos = editQntFilhos.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        btnCadastrar.isEnabled = false
        val novoUsuario = Usuario(nome, email, senha, telefone, cpf, genero, qntFilhos, tipo)
        viewModel.cadastrar(novoUsuario)
    }

    private fun configurarObservers() {
        viewModel.sucesso.observe(this) { tipo ->
            if (tipo != null) {
                Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()

                val intent = when (tipo) {
                    "Pai" -> Intent(this, HomePaiActivity::class.java)
                    "Filho" -> Intent(this, HomeFilhoActivity::class.java)
                    else -> null
                }

                if (intent != null)
                {
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this, "Perfil não reconhecido", Toast.LENGTH_SHORT).show()
                    btnCadastrar.isEnabled = true
                }
            }
        }

        viewModel.erro.observe(this) { mensagem ->
            btnCadastrar.isEnabled = true
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
    }
}