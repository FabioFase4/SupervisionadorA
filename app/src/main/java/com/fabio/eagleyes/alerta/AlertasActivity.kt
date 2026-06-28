package com.fabio.eagleyes.alerta

import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleyes.R

class AlertasActivity : AppCompatActivity() {

    private lateinit var viewModel: AlertaViewModel
    private lateinit var adapter: AlertaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualizar_alertas)

        // Configuração do ViewModel com Factory
        val repositorio = AlertaRepositorio()
        val factory = AlertaViewModelFactory(repositorio)
        viewModel = ViewModelProvider(this, factory).get(AlertaViewModel::class.java)

        configurarRecyclerView()
        configurarObservers()

        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish() // Apenas volta para a tela anterior (HomePai)
        }

        // Inicia a escuta dos alertas
        viewModel.carregarAlertas()
    }

    private fun configurarRecyclerView() {
        val rvAlertas = findViewById<RecyclerView>(R.id.rvAlertas)
        adapter = AlertaAdapter(emptyList())
        rvAlertas.layoutManager = LinearLayoutManager(this)
        rvAlertas.adapter = adapter
    }

    private fun configurarObservers() {
        viewModel.alertas.observe(this) { listaDeAlertas ->
            if (listaDeAlertas != null) {
                adapter.atualizarLista(listaDeAlertas)
                if (listaDeAlertas.isEmpty())
                    Toast.makeText(this, "Nenhum alerta encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}