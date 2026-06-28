package com.fabio.eagleyes.alerta

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleyes.R

class AlertasFilhoActivity : AppCompatActivity() {

    private lateinit var viewModel: AlertaViewModel
    private lateinit var adapter: AlertaAdapter
    private var emailFilho: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualizar_alertas_filho)

        emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""
        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Filho"

        findViewById<TextView>(R.id.txtTituloAlertasFilho).text = "Alertas de $nomeFilho"

        val repositorio = AlertaRepositorio()
        val factory = AlertaViewModelFactory(repositorio)
        viewModel = ViewModelProvider(this, factory).get(AlertaViewModel::class.java)

        configurarRecyclerView()
        configurarObservers()

        findViewById<Button>(R.id.btnVoltarAlertasFilho).setOnClickListener {
            finish()
        }

        viewModel.carregarAlertas()
    }

    private fun configurarRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvAlertasFilho)
        adapter = AlertaAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun configurarObservers() {
        viewModel.alertas.observe(this) { listaTotal ->
            // Filtra os alertas para mostrar apenas os deste filho
            val listaFiltrada = listaTotal.filter { it.emailFilho == emailFilho }
            adapter.atualizarLista(listaFiltrada)

            if (listaFiltrada.isEmpty()) {
                Toast.makeText(this, "Nenhum alerta para este filho.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}