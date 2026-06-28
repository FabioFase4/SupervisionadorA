package com.fabio.eagleyes.filho

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleyes.R
import com.fabio.eagleyes.alerta.AlertasFilhoActivity
import com.fabio.eagleyes.funcionalidades.historico.RelatorioFilho
import com.fabio.eagleyes.funcionalidades.regra.CriarRegraActivity

class DetalhesFilhoActivity : AppCompatActivity() {

    private lateinit var viewModel: DetalhesFilhoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_filho)

        viewModel = ViewModelProvider(this).get(DetalhesFilhoViewModel::class.java)

        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Filho"
        val emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""

        setupViews(nomeFilho, emailFilho)

        // Botão para ver o histórico (Agora em uma tela separada)
        findViewById<Button>(R.id.btnVerHistorico).setOnClickListener {
            val intent = Intent(this, RelatorioFilho::class.java)
            intent.putExtra("FILHO_EMAIL", emailFilho)
            intent.putExtra("FILHO_NOME", nomeFilho)
            startActivity(intent)
        }

        // Botão para visualizar alertas específicos do filho
        findViewById<Button>(R.id.btnVerAlertasFilho).setOnClickListener {
            val intent = Intent(this, AlertasFilhoActivity::class.java)
            intent.putExtra("FILHO_EMAIL", emailFilho)
            intent.putExtra("FILHO_NOME", nomeFilho)
            startActivity(intent)
        }

        // Botão para configurar regras
        findViewById<Button>(R.id.btnCriarRegra).setOnClickListener {
            val intent = Intent(this, CriarRegraActivity::class.java)
            intent.putExtra("FILHO_EMAIL", emailFilho)
            startActivity(intent)
        }

        // Botão para compartilhar relatório direto (opcional, se quiser manter aqui também)
        findViewById<Button>(R.id.btnGerarRelatorio).setOnClickListener {
            Toast.makeText(this, "Acesse o Histórico para gerar o relatório detalhado.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnVoltarDetalhes).setOnClickListener {
            finish()
        }
    }

    private fun setupViews(nome: String, email: String) {
        findViewById<TextView>(R.id.txtNomeDetalhes).text = nome
        findViewById<TextView>(R.id.txtEmailDetalhes).text = email
    }
}
