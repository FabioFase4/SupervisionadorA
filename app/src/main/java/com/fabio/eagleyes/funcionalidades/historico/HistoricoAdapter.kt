package com.fabio.eagleyes.funcionalidades.historico

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleyes.R

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoAdapter(private val lista: List<HistoricoUso>) :
    RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    class HistoricoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dadosBrutos: TextView = view.findViewById(R.id.txtDadosBrutos)
        val timeStamp: TextView = view.findViewById(R.id.txtTimeStamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val historico = lista[position]
        holder.dadosBrutos.text = historico.dadosBrutos

        // Formata o timestamp para algo legível
        val date = Date(historico.timeStamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        holder.timeStamp.text = sdf.format(date)
    }

    override fun getItemCount() = lista.size
}