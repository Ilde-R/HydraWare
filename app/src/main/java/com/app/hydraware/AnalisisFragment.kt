package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class AnalisisFragment : Fragment() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvEstadoGeneral: TextView

    private lateinit var tvValorPH: TextView
    private lateinit var tvRangoPH: TextView
    private lateinit var tvRecomendacionPH: TextView

    private lateinit var tvValorTemp: TextView
    private lateinit var tvRangoTemp: TextView
    private lateinit var tvRecomendacionTemp: TextView

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analisis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vincular views (asegúrate IDs coinciden con XML)
        tvTitulo = view.findViewById(R.id.tvTitulo)
        tvFechaHora = view.findViewById(R.id.tvFechaHora)
        tvEstadoGeneral = view.findViewById(R.id.tvEstadoGeneral)

        tvValorPH = view.findViewById(R.id.tvValorPH)
        tvRangoPH = view.findViewById(R.id.tvRangoPH)
        tvRecomendacionPH = view.findViewById(R.id.tvRecomendacionPH)

        tvValorTemp = view.findViewById(R.id.tvValorTemp)
        tvRangoTemp = view.findViewById(R.id.tvRangoTemp)
        tvRecomendacionTemp = view.findViewById(R.id.tvRecomendacionTemp)

        database = FirebaseDatabase.getInstance().reference

        val analisisRef = database.child("ultimaLectura")

        analisisRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ph = snapshot.child("ph").getValue(Double::class.java)
                val temp = snapshot.child("temperatura").getValue(Double::class.java)
                val fecha = snapshot.child("fecha").getValue(String::class.java)
                val hora = snapshot.child("hora").getValue(String::class.java)

                // Actualizar pH
                if (ph != null) {
                    tvValorPH.text = ph.toString()

                    when {
                        ph < 6.5 -> {
                            tvRangoPH.text = "Ácido"
                            tvRecomendacionPH.text = "El pH es ácido, se recomienda alcalinizar el agua."
                            tvValorPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRangoPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRecomendacionPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                        ph <= 8.5 -> {
                            tvRangoPH.text = "Ideal"
                            tvRecomendacionPH.text = "El pH está en rango óptimo."
                            tvValorPH.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                            tvRangoPH.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                            tvRecomendacionPH.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        }
                        else -> {
                            tvRangoPH.text = "Alcalino"
                            tvRecomendacionPH.text = "El pH es alcalino, se recomienda acidificar el agua."
                            tvValorPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRangoPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRecomendacionPH.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                    }
                } else {
                    tvValorPH.text = "N/A"
                    tvRangoPH.text = "-"
                    tvRecomendacionPH.text = "No hay datos de pH."
                }

                // Actualizar Temperatura
                if (temp != null) {
                    tvValorTemp.text = "$temp °C"

                    when {
                        temp < 20 -> {
                            tvRangoTemp.text = "Frío"
                            tvRecomendacionTemp.text = "Temperatura baja, aumenta temperatura del agua."
                            tvValorTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRangoTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRecomendacionTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                        temp <= 30 -> {
                            tvRangoTemp.text = "Estable"
                            tvRecomendacionTemp.text = "Temperatura óptima."
                            tvValorTemp.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                            tvRangoTemp.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                            tvRecomendacionTemp.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        }
                        else -> {
                            tvRangoTemp.text = "Caliente"
                            tvRecomendacionTemp.text = "Temperatura alta, enfría el agua."
                            tvValorTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRangoTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                            tvRecomendacionTemp.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                    }
                } else {
                    tvValorTemp.text = "N/A"
                    tvRangoTemp.text = "-"
                    tvRecomendacionTemp.text = "No hay datos de temperatura."
                }

                // Fecha y Hora
                if (fecha != null && hora != null) {
                    tvFechaHora.text = "Fecha: $fecha / Hora: $hora"
                } else {
                    tvFechaHora.text = "Fecha y hora no disponibles"
                }

                // Estado General (Ejemplo simple: basado en ph y temp)
                val estado = when {
                    ph == null || temp == null -> "Datos insuficientes"
                    ph < 6.5 || ph > 8.5 -> "Atención: pH fuera de rango"
                    temp < 20 || temp > 30 -> "Atención: Temperatura fuera de rango"
                    else -> "Estado normal"
                }
                tvEstadoGeneral.text = estado
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer datos", error.toException())
                tvEstadoGeneral.text = "Error al cargar datos"
                tvFechaHora.text = ""
                tvValorPH.text = "-"
                tvRangoPH.text = "-"
                tvRecomendacionPH.text = "-"
                tvValorTemp.text = "-"
                tvRangoTemp.text = "-"
                tvRecomendacionTemp.text = "-"
            }
        })
    }
}
