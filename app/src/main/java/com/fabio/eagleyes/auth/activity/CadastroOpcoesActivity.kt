package com.fabio.eagleyes.auth.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.fabio.eagleyes.R

class CadastroOpcoesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecao_perfil)

        val btnEntrarPai = findViewById<CardView>(R.id.cardPai)
        val btnEntrarFilho = findViewById<CardView>(R.id.cardFilho)
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)


        btnEntrarPai.setOnClickListener {
            AbrirCadastroPai()
        }

        btnEntrarFilho.setOnClickListener {
            AbrirCadastroFilho()
        }

        btnVoltar.setOnClickListener {
            Voltar()
        }
    }

    fun AbrirCadastroPai()
    {
        val intent = Intent(this, CadastroPaiActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun AbrirCadastroFilho ()
    {
        val intent = Intent(this, CadastroFilhoActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun Voltar ()
    {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
