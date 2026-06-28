package com.fabio.eagleyes.alerta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleyes.R

class AlertaAdapter(private var lista: List<Alerta>) : RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder>() {

    class AlertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtFilho: TextView = view.findViewById(R.id.txtAlertaFilho)
        val txtMensagem: TextView = view.findViewById(R.id.txtAlertaMensagem)
        val txtHorario: TextView = view.findViewById(R.id.txtAlertaHorario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerta, parent, false)
        return AlertaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        val alerta = lista[position]
        holder.txtFilho.text = "Filho: ${alerta.emailFilho}"
        holder.txtMensagem.text = alerta.mensagem
        holder.txtHorario.text = alerta.horario
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Alerta>) {
        this.lista = novaLista
        notifyDataSetChanged() // Notifica a RecyclerView para redesenhar na tela
    }
}
