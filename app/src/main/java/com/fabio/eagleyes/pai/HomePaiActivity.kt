package com.fabio.eagleyes.pai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleyes.R
import com.fabio.eagleyes.alerta.AlertasActivity
import com.fabio.eagleyes.adapter.FilhoAdapter
import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.auth.activity.LoginActivity
import com.fabio.eagleyes.auth.activity.CadastroFilhoActivity

class HomePaiActivity : AppCompatActivity() {

    private lateinit var viewModel: HomePaiViewModel
    private var adapter: FilhoAdapter? = null
    private lateinit var progressBar: ProgressBar
    private val PERMISSION_REQUEST_CODE = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_pai)

        progressBar = findViewById(R.id.progressBar)

        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(HomePaiViewModel::class.java)

        val emailPai = intent.getStringExtra("USER_EMAIL") ?: FirebaseConfig.getAuth().currentUser?.email

        prepararRecyclerView()
        configurarCliques(emailPai)
        configurarObservers()
        prepararAcoes(emailPai)
        
        // Iniciar serviço de escuta de alertas em tempo real
        verificarPermissaoENotificar()
    }

    private fun verificarPermissaoENotificar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            } else {
                iniciarServicoAlertas()
            }
        } else {
            iniciarServicoAlertas()
        }
    }

    private fun iniciarServicoAlertas() {
        val intent = Intent(this, AlertListenerService::class.java)
        startService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarServicoAlertas()
        }
    }

    private fun prepararAcoes(emailPai: String?) {
        if (emailPai != null)
            viewModel.carregarFilhos(emailPai)
        else
            Toast.makeText(this, "E-mail do pai não encontrado.", Toast.LENGTH_SHORT).show()
    }

    private fun prepararRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvFilhos)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun configurarObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            progressBar.visibility = if (estaCarregando) View.VISIBLE else View.GONE
        }

        viewModel.listaFilhos.observe(this) { filhos ->
            val recyclerView = findViewById<RecyclerView>(R.id.rvFilhos)
            if (!filhos.isNullOrEmpty()) {
                val filhosArrayList = filhos.toCollection(ArrayList())
                adapter = FilhoAdapter(filhosArrayList)
                recyclerView.adapter = adapter
            } else {
                recyclerView.adapter = null
            }
        }

        viewModel.erro.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarCliques(emailPai: String?) {
        val btnCadastrarFilho = findViewById<Button>(R.id.btnCadastrarFilho)
        val btnInstrucoes = findViewById<Button>(R.id.btnInstrucoesVinculo)
        val btnVerAlertas = findViewById<Button>(R.id.btnVerAlertas)
        val btnLogout = findViewById<Button>(R.id.btnVoltar)

        btnCadastrarFilho.setOnClickListener {
            val intent = Intent(this, CadastroFilhoActivity::class.java)
            intent.putExtra("EMAIL_PAI", emailPai)
            startActivity(intent)
        }

        btnInstrucoes.setOnClickListener {
            Toast.makeText(this, "Para vincular, seu filho deve cadastrar o e-mail: $emailPai", Toast.LENGTH_LONG).show()
        }

        btnVerAlertas.setOnClickListener {
            startActivity(Intent(this, AlertasActivity::class.java))
        }

        btnLogout.setOnClickListener {
            stopService(Intent(this, AlertListenerService::class.java))
            FirebaseConfig.getAuth().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}