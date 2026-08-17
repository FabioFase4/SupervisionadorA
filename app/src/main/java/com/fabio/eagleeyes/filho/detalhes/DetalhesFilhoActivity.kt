package com.fabio.eagleeyes.filho.detalhes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.activitiy.AlertasFilhoActivity
import com.fabio.eagleeyes.funcionalidades.historico.RelatorioFilhoActivity
import com.fabio.eagleeyes.funcionalidades.regra.GerenciarRegrasActivity

class DetalhesFilhoActivity : AppCompatActivity() {

    private lateinit var viewModel: DetalhesFilhoViewModel

    private var btnVerHistorico: Button? = null
    private var btnVerAlertasFilho: Button? = null
    private var btnVerHistoricoIA: Button? = null
    private var btnGerenciarRegras: Button? = null
    private var btnVoltarDetalhes: Button? = null

    private var txtNomeDetalhes: TextView? = null
    private var txtEmailDetalhes: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_filho)

        viewModel = ViewModelProvider(this).get(DetalhesFilhoViewModel::class.java)

        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Filho"
        val emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""
        val uidFilho = intent.getStringExtra("FILHO_UID") ?: ""

        setupViews(nomeFilho, emailFilho)
        configurarCliques(nomeFilho, emailFilho, uidFilho)
        configurarObservers()
    }

    private fun setupViews(nome: String, email: String) {
        btnVerHistorico = findViewById(R.id.btnVerHistorico)
        btnVerAlertasFilho = findViewById(R.id.btnVerAlertasFilho)
        btnGerenciarRegras = findViewById(R.id.btnGerenciarRegras)
        btnVoltarDetalhes = findViewById(R.id.btnVoltarDetalhes)

        txtNomeDetalhes = findViewById(R.id.txtNomeDetalhes)
        txtEmailDetalhes = findViewById(R.id.txtEmailDetalhes)

        txtNomeDetalhes?.text = nome
        txtEmailDetalhes?.text = email

        // Habilita botões cujas telas já estão prontas
        btnVerHistorico?.isEnabled = true
        btnVerAlertasFilho?.isEnabled = true
    }

    private fun configurarCliques(nome: String, email: String, uid: String) {
        btnVerHistorico?.setOnClickListener {
            val intent = Intent(this, RelatorioFilhoActivity::class.java).apply {
                putExtra("FILHO_UID", uid)
                putExtra("FILHO_NOME", nome)
            }
            startActivity(intent)
        }

        btnVerAlertasFilho?.setOnClickListener {
            val intent = Intent(this, AlertasFilhoActivity::class.java).apply {
                putExtra("FILHO_UID", uid)
                putExtra("FILHO_NOME", nome)
            }
            startActivity(intent)
        }

        btnGerenciarRegras?.setOnClickListener {
            val intent = Intent(this, GerenciarRegrasActivity::class.java).apply {
                putExtra("FILHO_UID", uid)
                putExtra("FILHO_EMAIL", email)
            }
            startActivity(intent)
        }

        btnVoltarDetalhes?.setOnClickListener {
            finish()
        }
    }

    private fun configurarObservers() {
        viewModel.erro.observe(this) { msg ->
            if (msg.isNotEmpty())
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}