package com.fabio.eagleyes.funcionalidades.historico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleyes.R
import com.fabio.eagleyes.filho.DetalhesFilhoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RelatorioFilho : AppCompatActivity() {

    private lateinit var viewModel: DetalhesFilhoViewModel
    private lateinit var adapter: HistoricoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorio_filho)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Filho"
        val emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""

        setupViews(nomeFilho, emailFilho)
        
        viewModel = ViewModelProvider(this).get(DetalhesFilhoViewModel::class.java)
        
        setupRecyclerView()
        setupObservers()

        if (emailFilho.isNotEmpty()) {
            viewModel.carregarHistorico(emailFilho)
        }

        findViewById<Button>(R.id.btnExportar).setOnClickListener {
            val lista = viewModel.historico.value
            if (!lista.isNullOrEmpty()) {
                compartilharRelatorio(nomeFilho, lista)
            } else {
                Toast.makeText(this, "Não há dados para exportar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViews(nome: String, email: String) {
        findViewById<TextView>(R.id.txtNomeFilho).text = nome
        findViewById<TextView>(R.id.txtEmailFilho).text = email
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvHistorico)
        rv.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.historico.observe(this) { lista ->
            adapter = HistoricoAdapter(lista)
            findViewById<RecyclerView>(R.id.rvHistorico).adapter = adapter
        }
    }

    private fun compartilharRelatorio(nome: String, lista: List<HistoricoUso>) {
        val relatorio = StringBuilder()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        relatorio.append("📊 RELATÓRIO DE ATIVIDADES - $nome\n")
        relatorio.append("Gerado em: ${sdf.format(Date())}\n")
        relatorio.append("------------------------------------------\n\n")

        lista.forEach { item ->
            val dataFormatada = sdf.format(Date(item.timeStamp))
            relatorio.append("📅 $dataFormatada\n")
            relatorio.append("📝 ${item.dadosBrutos}\n")
            relatorio.append("------------------------------------------\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, relatorio.toString())
        startActivity(Intent.createChooser(shareIntent, "Enviar Relatório via:"))
    }
}
