package com.fabio.eagleeyes.cadastro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.databinding.ActivityRegistroBinding
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.usuario.UsuarioPai
import com.fabio.eagleeyes.usuario.UsuarioFilho
import com.fabio.eagleeyes.usuario.UsuarioRepositorio
import com.fabio.eagleeyes.pai.home.HomePaiActivity
import com.fabio.eagleeyes.filho.home.HomeFilhoActivity
import com.fabio.eagleeyes.home.HomeUsuario

open class CadastroActivity : AppCompatActivity() {

    protected lateinit var viewModel: CadastroViewModel
    protected lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout usando o binding
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repositorio = UsuarioRepositorio()
        val factory = CadastroViewModelFactory(repositorio)
        viewModel = ViewModelProvider(this, factory).get(CadastroViewModel::class.java)

        val tipoUsuario = intent.getStringExtra("TIPO_USUARIO") ?: "Usuario"
        configurarViews(tipoUsuario)
        configurarObservers()
        configurarCliques(tipoUsuario)
    }

    protected open fun configurarViews(tipoUsuario: String) {
        if (tipoUsuario == "Filho") {
            binding.edtEmailResponsavel.visibility = View.VISIBLE
            binding.edtNumeroFilhos.visibility = View.GONE
        } else if (tipoUsuario == "Pai") {
            binding.edtEmailResponsavel.visibility = View.GONE
            binding.edtNumeroFilhos.visibility = View.VISIBLE
        } else {
            binding.edtEmailResponsavel.visibility = View.GONE
            binding.edtNumeroFilhos.visibility = View.GONE
        }
    }

    protected open fun configurarCliques(tipoUsuario: String) {
        binding.btnCadastrar.setOnClickListener {
            val usuario = criarUsuario(tipoUsuario)
            if (usuario != null) {
                when (tipoUsuario) {
                    "Pai" -> viewModel.cadastrarPai(usuario as UsuarioPai, "pais")
                    "Filho" -> viewModel.cadastrarFilho(usuario as UsuarioFilho, "filhos")
                    else -> Toast.makeText(this, "Tipo de usuário não suportado para cadastro direto", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    protected open fun criarUsuario(tipoUsuario: String): Usuario? {
        val nome = binding.edtNome.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val senha = binding.edtSenha.text.toString().trim()
        val telefone = binding.edtTelefone.text.toString().trim()
        val cpf = binding.edtCPF.text.toString().trim()
        val genero = when (binding.rgGenero.checkedRadioButtonId) {
            R.id.rbMasculino -> "Masculino"
            R.id.rbFeminino -> "Feminino"
            else -> "Outro"
        }

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return null
        }

        return when (tipoUsuario) {
            "Pai" -> {
                val numeroFilhos = binding.edtNumeroFilhos.text.toString().toIntOrNull() ?: 0
                UsuarioPai(nome, email, senha, telefone, cpf, genero, numeroFilhos)
            }
            "Filho" -> {
                val emailResponsavel = binding.edtEmailResponsavel.text.toString().trim()
                if (emailResponsavel.isEmpty()) {
                    Toast.makeText(this, "E-mail do responsável é obrigatório!", Toast.LENGTH_SHORT).show()
                    return null
                }
                UsuarioFilho(nome, email, senha, telefone, cpf, genero, emailResponsavel)
            }
            else -> Usuario(
                nome = nome,
                email = email,
                senha = senha,
                telefone = telefone,
                cpf = cpf,
                genero = genero,
                tipo = "Usuario"
            )
        }
    }

    protected open fun configurarObservers() {
        viewModel.sucesso.observe(this) { caminho ->
            if (caminho != null) {
                Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                redirecionarParaHome()
            }
        }

        viewModel.erro.observe(this) { mensagem ->
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
    }

    protected open fun redirecionarParaHome() {
        val tipo = intent.getStringExtra("TIPO_USUARIO") ?: "Usuario"
        val intent = when (tipo) {
            "Pai" -> Intent(this, HomePaiActivity::class.java)
            "Filho" -> Intent(this, HomeFilhoActivity::class.java)
            else -> Intent(this, HomeUsuario::class.java)
        }
        startActivity(intent)
        finish()
    }
}
