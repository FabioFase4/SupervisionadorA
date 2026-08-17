package com.fabio.eagleeyes.pai.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.activitiy.AlertasActivity
import com.fabio.eagleeyes.login.LoginActivity
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.ListaFilhosActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomePaiActivity : AppCompatActivity() {

    private lateinit var viewModel: HomePaiViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPerguntarIA: Button
    private lateinit var edtPesquisaIA: EditText
    private lateinit var txtRespostaIA: TextView
    private var emailPaiOficial: String? = null

    private val PERMISSION_REQUEST_CODE = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_pai)

        vincularViews()

        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(HomePaiViewModel::class.java)

        configurarObservers()
        recuperarDadosEPriorizar()
        verificarPermissaoENotificar()
    }

    private fun vincularViews() {
        progressBar = findViewById(R.id.progressBar)
        edtPesquisaIA = findViewById(R.id.edtPesquisaIA)
        btnPerguntarIA = findViewById<Button>(R.id.btnPerguntarIA)
        txtRespostaIA = findViewById(R.id.txtRespostaIA)
    }

    private fun recuperarDadosEPriorizar() {
        val user = FirebaseConfig.getAuth().currentUser
        if (user == null) {
            irParaLogin()
            return
        }

        lifecycleScope.launch {
            try {
                // Busca o e-mail cadastrado no nó do pai para garantir consistência
                val snap = FirebaseConfig.getDatabase().child("usuarios/pais").child(user.uid).get().await()
                emailPaiOficial = snap.child("email").getValue(String::class.java) ?: user.email

                Log.d("HomePaiActivity", "Pai identificado: $emailPaiOficial")

                configurarCliques(emailPaiOficial)

                if (emailPaiOficial != null)
                    viewModel.carregarFilhos(emailPaiOficial!!)

            } catch (e: Exception) {
                Log.e("HomePaiActivity", "Erro ao recuperar perfil do pai: ${e.message}")
                configurarCliques(user.email)
                user.email?.let { viewModel.carregarFilhos(it) }
            }
        }
    }

    private fun irParaLogin() {
        Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun configurarObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            progressBar.visibility = if (estaCarregando) View.VISIBLE else View.GONE
            btnPerguntarIA.isEnabled = !estaCarregando
        }

        viewModel.respostaIA.observe(this) { resposta ->
            if (resposta.isNotEmpty()) {
                txtRespostaIA.visibility = View.VISIBLE
                txtRespostaIA.text = resposta
            }
        }

        viewModel.erro.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarCliques(emailPai: String?) {
        val btnMeusFilhos = findViewById<Button>(R.id.btnMeusFilhos)
        val btnVerAlertas = findViewById<Button>(R.id.btnVerAlertas)
        val btnInstrucoesVinculo = findViewById<Button>(R.id.btnInstrucoesVinculo)
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)

        btnMeusFilhos.setOnClickListener {
            val intent = Intent(this, ListaFilhosActivity::class.java)
            intent.putExtra("EMAIL_PAI", emailPai)
            startActivity(intent)
        }

        btnVerAlertas.setOnClickListener {
            val intent = Intent(this, AlertasActivity::class.java)
            startActivity(intent)
        }

        btnInstrucoesVinculo.setOnClickListener {
            Toast.makeText(this, "Para vincular, seu filho deve cadastrar o e-mail: $emailPai", Toast.LENGTH_LONG).show()
        }

        btnPerguntarIA.setOnClickListener {
            val pergunta = edtPesquisaIA.text.toString().trim()
            if (pergunta.isNotEmpty()) {
                viewModel.analisarComportamentoComIA(pergunta)
            }
        }

        btnVoltar.setOnClickListener {
            FirebaseConfig.getAuth().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun verificarPermissaoENotificar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }
}