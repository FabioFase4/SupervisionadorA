package com.fabio.eagleeyes.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.filho.detalhes.DetalhesFilhoActivity

class FilhoAdapter(private var lista: MutableList<Usuario>) :
    RecyclerView.Adapter<FilhoAdapter.FilhoViewHolder>() {

    class FilhoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.txtNomeFilho)
        val email: TextView = view.findViewById(R.id.txtEmailFilho)
    }

    fun updateList(novaLista: List<Usuario>) {
        this.lista.clear()
        this.lista.addAll(novaLista)
        notifyDataSetChanged()
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
            val intent = Intent(holder.itemView.context, DetalhesFilhoActivity::class.java)
            intent.putExtra("FILHO_NOME", filho.nome)
            intent.putExtra("FILHO_EMAIL", filho.email)
            intent.putExtra("FILHO_UID", filho.uid)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = lista.size
}
