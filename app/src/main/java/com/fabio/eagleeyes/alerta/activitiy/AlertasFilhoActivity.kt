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

class AlertasFilhoActivity : AlertasActivity() {
    private var emailFilho: String? = null
    private var tituloSuperior: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualizar_alertas_filho)

        tituloSuperior = findViewById(R.id.txtTituloAlertasFilho)
        val emailIntent = intent.getStringExtra("FILHO_EMAIL")
        emailFilho = if (!emailIntent.isNullOrEmpty()) emailIntent else null

        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Meus Alertas"
        tituloSuperior?.text = if (nomeFilho == "Meus Alertas") nomeFilho else "Alertas de $nomeFilho"

        configurarRecyclerView()
        configurarObservers()

        viewModel.carregarAlertas(emailFilho)
    }

    override fun configurarRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvAlertasFilho)
        adapter = AlertaAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun configurarObservers() {
        super.configurarObservers()

        viewModel.alertas.observe(this) { lista ->
            Log.d("AlertasFilhoActivity", "Exibindo ${lista?.size ?: 0} alertas na lista (classe filha).")
        }
    }
}
