package com.fabio.eagleeyes.funcionalidades.historico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.filho.detalhes.DetalhesFilhoViewModel
import com.fabio.eagleeyes.viewmodel.AnaliseViewModel
import java.lang.StringBuilder

class RelatorioFilhoActivity : AppCompatActivity() {

    private lateinit var viewModelDetalhes: DetalhesFilhoViewModel
    private lateinit var viewModelIA: AnaliseViewModel
    private lateinit var adapter: HistoricoAdapter

    private var btnExportar: Button? = null
    private var btnAnaliseIA: Button? = null
    private var btnVoltar: Button? = null
    private var txtRespostaIA: TextView? = null
    private var cardRespostaIA: CardView? = null
    private var progressBar: View? = null
    private var rvHistorico: RecyclerView? = null

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
        val uidFilho = intent.getStringExtra("FILHO_UID") ?: ""

        setupViews(nomeFilho)
        
        viewModelDetalhes = ViewModelProvider(this).get(DetalhesFilhoViewModel::class.java)
        viewModelIA = ViewModelProvider(this).get(AnaliseViewModel::class.java)
        
        setupRecyclerView()
        setupObservers()

        if (uidFilho.isNotEmpty()) {
            viewModelDetalhes.carregarHistorico(uidFilho)
        }

        btnExportar?.setOnClickListener {
            val lista = viewModelDetalhes.historico.value
            val analiseAtual = viewModelIA.respostaIA.value ?: ""
            
            if (!lista.isNullOrEmpty()) {
                compartilharRelatorioCompleto(nomeFilho, lista, analiseAtual)
            } else {
                Toast.makeText(this, "Nenhum dado para exportar", Toast.LENGTH_SHORT).show()
            }
        }

        btnAnaliseIA?.setOnClickListener {
            solicitarAnaliseIA(nomeFilho)
        }

        btnVoltar?.setOnClickListener {
            finish()
        }
    }

    private fun setupViews(nome: String) {
        btnExportar = findViewById(R.id.btnExportar)
        btnAnaliseIA = findViewById(R.id.btnAnaliseIA)
        btnVoltar = findViewById(R.id.btnVoltar)
        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        cardRespostaIA = findViewById(R.id.cardRespostaIA)
        progressBar = findViewById(R.id.progressBarHistorico)
        rvHistorico = findViewById(R.id.rvHistorico)

        findViewById<TextView>(R.id.txtNomeFilho).text = nome
    }

    private fun setupRecyclerView() {
        rvHistorico?.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModelDetalhes.historico.observe(this) { lista ->
            adapter = HistoricoAdapter(lista)
            rvHistorico?.adapter = adapter
        }

        viewModelIA.respostaIA.observe(this) { resposta ->
            if (resposta.isNullOrEmpty()) {
                cardRespostaIA?.visibility = View.GONE
            } else {
                cardRespostaIA?.visibility = View.VISIBLE
                txtRespostaIA?.text = resposta
            }
        }

        viewModelIA.carregando.observe(this) { carregando ->
            progressBar?.visibility = if (carregando) View.VISIBLE else View.GONE
        }
    }

    private fun solicitarAnaliseIA(nome: String) {
        val historico = viewModelDetalhes.historico.value
        if (historico.isNullOrEmpty()) {
            Toast.makeText(this, "Sem dados para analisar no momento.", Toast.LENGTH_SHORT).show()
            return
        }

        val resumoApps = historico.take(15).joinToString(separator = "\n") { 
            "- ${it.nomeApp} (${it.data}): ${it.tempoGasto} min" 
        }

        val prompt = """
            Aja como um especialista em segurança digital e mentor familiar.
            Analise o histórico de uso do $nome:
            
            $resumoApps
            
            Forneça um relatório explicativo com:
            1. Resumo do comportamento.
            2. Possíveis riscos detectados.
            3. Uma recomendação prática para a família.
            
            O relatório deve ser escrito de acordo com os seguintes critérios:
            1. Maior tempo de uso
            2. Redes Sociais
            3. Aplicativos importantes
            4. Não precisa falar dos aplicativos que não possuem efeitos negativos (ex: Jogos que não tragam impactos negativos reais)
            
            Seja direto e use no máximo 6 linhas.
        """.trimIndent()
        
        viewModelIA.analisar(prompt)
    }

    private fun compartilharRelatorioCompleto(nome: String, lista: List<HistoricoUso>, analiseIA: String) {
        val relatorio = StringBuilder().apply {
            append("📊 RELATÓRIO EAGLEEYES - $nome\n")
            append("----------------------------------\n\n")
            
            if (analiseIA.isNotEmpty()) {
                append("🤖 ANÁLISE DO MENTOR IA:\n")
                append("$analiseIA\n")
                append("----------------------------------\n\n")
            }

            append("📋 DETALHES DE ATIVIDADES:\n")
            lista.take(30).forEach { 
                val tempo = if (it.tempoGasto > 0) " (${it.tempoGasto} min)" else ""
                append("• ${it.data} - ${it.nomeApp}$tempo\n") 
            }
            
            append("\nGerado pelo App EagleEyes")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Uso - $nome")
            putExtra(Intent.EXTRA_TEXT, relatorio.toString())
        }
        startActivity(Intent.createChooser(shareIntent, "Enviar Relatório via..."))
    }
}
