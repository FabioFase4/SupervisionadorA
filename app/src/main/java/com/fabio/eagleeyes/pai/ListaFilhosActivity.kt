package com.fabio.eagleeyes.pai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.adapter.FilhoAdapter
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.home.HomePaiDAO
import com.fabio.eagleeyes.pai.home.HomePaiRepository
import com.fabio.eagleeyes.pai.home.HomePaiViewModel
import com.fabio.eagleeyes.pai.home.HomePaiViewModelFactory
import com.fabio.eagleeyes.cadastro.CadastroActivity
import com.google.android.material.button.MaterialButton

class ListaFilhosActivity : AppCompatActivity() {

    private lateinit var viewModel: HomePaiViewModel
    private lateinit var adapter: FilhoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCadastrarFilho: MaterialButton
    private lateinit var btnVoltar: MaterialButton
    private var emailPai: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_filhos)

        emailPai = intent.getStringExtra("EMAIL_PAI")

        vincularViews()
        configurarViewModel()
        prepararRecyclerView()
        configurarObservers()
        configurarCliques()

        emailPai?.let { viewModel.carregarFilhos(it) }
    }

    private fun vincularViews() {
        progressBar = findViewById(R.id.progressBar)
        btnCadastrarFilho = findViewById(R.id.btnCadastrarFilho)
        btnVoltar = findViewById(R.id.btnVoltar)
    }

    private fun configurarViewModel() {
        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(HomePaiViewModel::class.java)
    }

    private fun prepararRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvFilhos)
        adapter = FilhoAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun configurarObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            progressBar.visibility = if (estaCarregando) View.VISIBLE else View.GONE
        }

        viewModel.listaFilhos.observe(this) { filhos ->
            adapter.updateList(filhos ?: emptyList())
        }

        viewModel.erro.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarCliques() {
        btnCadastrarFilho.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            intent.putExtra("EMAIL_PAI", emailPai)
            startActivity(intent)
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }
}
