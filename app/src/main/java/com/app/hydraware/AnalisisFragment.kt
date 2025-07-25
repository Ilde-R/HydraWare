package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AnalisisFragment : Fragment() {

    private lateinit var rvHistorialLecturas: RecyclerView
    private lateinit var database: DatabaseReference

    private val listaLecturas = mutableListOf<Lectura>()
    private lateinit var adapter: LecturaAdapter

    // Para controlar qué ítem está expandido (-1 significa ninguno)
    private var expandedPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analisis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHistorialLecturas = view.findViewById(R.id.recyclerHistorial)
        rvHistorialLecturas.layoutManager = LinearLayoutManager(requireContext())

        adapter = LecturaAdapter(
            listaLecturas,
            expandedPositionProvider = { expandedPosition },
            onItemClick = { clickedPosition ->
                val previousExpanded = expandedPosition
                expandedPosition = if (expandedPosition == clickedPosition) -1 else clickedPosition
                if (previousExpanded != -1) adapter.notifyItemChanged(previousExpanded)
                adapter.notifyItemChanged(expandedPosition)

                // Mostrar datos seleccionados en los cards principales
                mostrarLecturaSeleccionada(listaLecturas[clickedPosition])
            }
        )

        rvHistorialLecturas.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        val historialRef = database.child("historial")

        historialRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaLecturas.clear()
                for (lecturaSnap in snapshot.children) {
                    val lectura = lecturaSnap.getValue(Lectura::class.java)
                    if (lectura != null) {
                        listaLecturas.add(lectura)
                    }
                }
                // Mostrar datos más recientes primero
                listaLecturas.reverse()

                // Resetear expandido al actualizar lista para evitar inconsistencia
                expandedPosition = -1

                adapter.notifyDataSetChanged()

                // Opcional: mostrar el primer dato si existe
                if (listaLecturas.isNotEmpty()) {
                    mostrarLecturaSeleccionada(listaLecturas[0])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer historial", error.toException())
            }
        })
    }

    private fun mostrarLecturaSeleccionada(lectura: Lectura) {
        val tvFechaHora = view?.findViewById<TextView>(R.id.tvFechaHora)
        val tvValorPH = view?.findViewById<TextView>(R.id.tvValorPH)
        val tvRangoPH = view?.findViewById<TextView>(R.id.tvRangoPH)
        val tvRecomendacionPH = view?.findViewById<TextView>(R.id.tvRecomendacionPH)

        val tvValorTemp = view?.findViewById<TextView>(R.id.tvValorTemp)
        val tvRangoTemp = view?.findViewById<TextView>(R.id.tvRangoTemp)
        val tvRecomendacionTemp = view?.findViewById<TextView>(R.id.tvRecomendacionTemp)

        val tvEstadoGeneral = view?.findViewById<TextView>(R.id.tvEstadoGeneral)

        tvFechaHora?.text = "Fecha: ${lectura.fecha ?: "--"} / Hora: ${lectura.hora ?: "--"}"
        tvValorPH?.text = lectura.ph?.toString() ?: "No hay datos"
        val ph = lectura.ph ?: 0.0
        when {
            ph < 6.5 -> {
                tvRangoPH?.text = "pH bajo"
                tvRecomendacionPH?.text = "Ajustar pH al alza"
            }
            ph in 6.5..8.5 -> {
                tvRangoPH?.text = "pH normal"
                tvRecomendacionPH?.text = "Condiciones óptimas"
            }
            else -> {
                tvRangoPH?.text = "pH alto"
                tvRecomendacionPH?.text = "Ajustar pH a la baja"
            }
        }

        tvValorTemp?.text = lectura.temperatura?.toString() ?: "No hay datos"
        val temp = lectura.temperatura ?: 0.0
        when {
            temp < 20 -> {
                tvRangoTemp?.text = "Temperatura baja"
                tvRecomendacionTemp?.text = "Aumentar temperatura"
            }
            temp in 20.0..30.0 -> {
                tvRangoTemp?.text = "Temperatura normal"
                tvRecomendacionTemp?.text = "Condiciones óptimas"
            }
            else -> {
                tvRangoTemp?.text = "Temperatura alta"
                tvRecomendacionTemp?.text = "Reducir temperatura"
            }
        }

        tvEstadoGeneral?.text = if (ph in 6.5..8.5 && temp in 20.0..30.0) {
            "Estado: Óptimo"
        } else {
            "Estado: Requiere atención"
        }
    }
}
