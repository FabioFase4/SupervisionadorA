package com.fabio.eagleeyes.cadastro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fabio.eagleeyes.databinding.ActivitySelecaoPerfilBinding
import com.fabio.eagleeyes.login.LoginActivity

class CadastroOpcoesActivity : AppCompatActivity() {
    // 1. Declara o binding CORRETAMENTE (ActivitySelecaoPerfilBinding)
    private lateinit var binding: ActivitySelecaoPerfilBinding

    // 2. Enum para os tipos de usuário
    enum class TipoUsuario {
        PAI,
        FILHO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. Infla o layout usando View Binding
        binding = ActivitySelecaoPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root = raiz do layout

        // 4. Configura os cliques dos cards e do botão
        binding.cardPai.setOnClickListener {
            abrirCadastroUsuarioPorTipo(TipoUsuario.PAI)
        }

        binding.cardFilho.setOnClickListener {
            abrirCadastroUsuarioPorTipo(TipoUsuario.FILHO)
        }

        binding.btnVoltar.setOnClickListener {
            voltar()
        }
    }

    // 5. Função para abrir a Activity de cadastro com o tipo de usuário
    private fun abrirCadastroUsuarioPorTipo(tipo: TipoUsuario) {
        val intent = Intent(this, CadastroActivity::class.java)
        intent.putExtra("TIPO_USUARIO", tipo.toString())
        startActivity(intent)
        finish() // Fecha esta Activity para não voltar com back button
    }

    // 6. Função para voltar à tela de login
    private fun voltar() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Fecha esta Activity
    }
}
