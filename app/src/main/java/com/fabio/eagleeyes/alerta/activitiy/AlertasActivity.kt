package com.fabio.eagleeyes.alerta.activitiy

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.AlertaAdapter
import com.fabio.eagleeyes.alerta.AlertaRepositorio
import com.fabio.eagleeyes.alerta.AlertaViewModel
import com.fabio.eagleeyes.alerta.AlertaViewModelFactory

open class AlertasActivity : AppCompatActivity() {

    protected lateinit var viewModel: AlertaViewModel
    protected lateinit var adapter: AlertaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualizar_alertas)

        val repositorio = AlertaRepositorio()
        val factory = AlertaViewModelFactory(repositorio)
        viewModel = ViewModelProvider(this, factory).get(AlertaViewModel::class.java)

        configurarRecyclerView()
        configurarObservers()

        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        viewModel.carregarAlertas()
    }

    protected open fun configurarRecyclerView() {
        val rvAlertas = findViewById<RecyclerView>(R.id.rvAlertas)
        adapter = AlertaAdapter(emptyList())
        rvAlertas.layoutManager = LinearLayoutManager(this)
        rvAlertas.adapter = adapter
    }

    protected open fun configurarObservers() {
        viewModel.alertas.observe(this) { listaDeAlertas ->
            Log.d("AlertasActivity", "Exibindo ${listaDeAlertas.size} alertas na lista.")
            if (listaDeAlertas != null) {
                adapter.atualizarLista(listaDeAlertas)
                if (listaDeAlertas.isEmpty())
                    Toast.makeText(this, "Nenhum alerta encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}