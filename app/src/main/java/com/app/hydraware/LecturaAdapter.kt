package com.app.hydraware

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LecturaAdapter(
    private val listaLecturas: List<Lectura>,
    private val onItemClick: (Lectura) -> Unit
) : RecyclerView.Adapter<LecturaAdapter.LecturaViewHolder>() {

    inner class LecturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val tvPh: TextView = itemView.findViewById(R.id.tvPh)
        val tvTemperatura: TextView = itemView.findViewById(R.id.tvTemperatura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lectura, parent, false)
        return LecturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LecturaViewHolder, position: Int) {
        val lectura = listaLecturas[position]
        holder.tvFechaHora.text = "Fecha: ${lectura.fecha} Hora: ${lectura.hora}"
        holder.tvPh.text = "pH: ${lectura.ph}"
        holder.tvTemperatura.text = "Temperatura: ${lectura.temperatura} °C"

        holder.itemView.setOnClickListener {
            onItemClick(lectura)
        }
    }

    override fun getItemCount(): Int = listaLecturas.size
}
