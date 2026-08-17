package com.fabio.eagleeyes.alerta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleeyes.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        
        // Exibe a data e hora formatada a partir do timestamp, ou fallback para o campo horario
        val dataHoraFormatada = if (alerta.timestamp != 0L) {
            val datetime = Date(alerta.timestamp)
            val data = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(datetime)
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(datetime)
            "$data $hora"
        } else if (alerta.horario.isNotEmpty()) {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(alerta.timestamp))
        } else {
            alerta.horario
        }
        
        holder.txtHorario.text = dataHoraFormatada
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Alerta>) {
        this.lista = novaLista
        notifyDataSetChanged() // Notifica a RecyclerView para redesenhar na tela
    }
}
