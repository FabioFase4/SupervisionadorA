package com.fabio.eagleyes.funcionalidades.regra

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleyes.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CriarRegraActivity : AppCompatActivity() {

    private lateinit var viewModel: RegraViewModel
    private var emailFilho: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_regra)

        viewModel = ViewModelProvider(this).get(RegraViewModel::class.java)
        emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""

        val edtNomeAppRegra = findViewById<TextInputEditText>(R.id.edtNomeAppRegra)
        val rgTipoRegra = findViewById<RadioGroup>(R.id.rgTipoRegra)
        val edtValorRegra = findViewById<TextInputEditText>(R.id.edtValorRegra)
        val btnSalvarRegra = findViewById<MaterialButton>(R.id.btnSalvarRegra)
        val btnCancelarRegra = findViewById<MaterialButton>(R.id.btnCancelarRegra)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnSalvarRegra.setOnClickListener {
            val nomeApp = edtNomeAppRegra.text.toString().trim()
            val valor = edtValorRegra.text.toString().trim()
            val selectedId = rgTipoRegra.checkedRadioButtonId
            
            if (nomeApp.isEmpty() || valor.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipo = when (selectedId) {
                R.id.rbBloqueio -> "BLOQUEIO"
                R.id.rbLimiteTempo -> "LIMITE_TEMPO"
                R.id.rbHorario -> "HORARIO"
                else -> ""
            }

            val novaRegra = Regra(
                emailFilho = emailFilho,
                nomeApp = nomeApp,
                tipo = tipo,
                valor = valor,
                ativa = true
            )

            progressBar.visibility = View.VISIBLE
            viewModel.salvarRegra(novaRegra)
        }

        btnCancelarRegra.setOnClickListener {
            finish()
        }

        viewModel.sucesso.observe(this) { sucesso ->
            progressBar.visibility = View.GONE
            if (sucesso) {
                Toast.makeText(this, "Regra salva com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.erro.observe(this) { erro ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, erro, Toast.LENGTH_SHORT).show()
        }
    }
}
