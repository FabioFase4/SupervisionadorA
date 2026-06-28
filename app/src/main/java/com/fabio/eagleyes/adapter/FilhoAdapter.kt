package com.fabio.eagleyes.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleyes.R
import com.fabio.eagleyes.auth.model.Usuario
import com.fabio.eagleyes.filho.DetalhesFilhoActivity

class FilhoAdapter(private val lista: List<Usuario>) :
    RecyclerView.Adapter<FilhoAdapter.FilhoViewHolder>() {

    class FilhoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.txtNomeFilho)
        val email: TextView = view.findViewById(R.id.txtEmailFilho)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilhoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filho, parent, false)
        return FilhoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilhoViewHolder, position: Int) {
        val filho = lista[position]
        holder.nome.text = filho.nome
        holder.email.text = filho.email

        holder.itemView.setOnClickListener {
            // Quando clicar no filho, abre a tela de detalhes/monitoramento
            val intent = Intent(holder.itemView.context, DetalhesFilhoActivity::class.java)
            intent.putExtra("FILHO_NOME", filho.nome)
            intent.putExtra("FILHO_EMAIL", filho.email)
            intent.putExtra("FILHO_UID", filho.uid) // Passando o UID para buscar o histórico
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = lista.size
}
