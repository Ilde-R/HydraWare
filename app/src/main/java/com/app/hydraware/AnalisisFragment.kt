package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AnalisisFragment : Fragment() {

    private lateinit var rvHistorialLecturas: RecyclerView
    private lateinit var database: DatabaseReference

    private val listaLecturas = mutableListOf<Lectura>()
    private lateinit var adapter: LecturaAdapter

    private var expandedPosition = -1

    // Parámetros del tanque (valores por defecto)
    private var tanqueId: String? = null
    private var tanqueName: String? = null
    private var phMin: Double = 6.5
    private var phMax: Double = 8.5
    private var tempMin: Double = 20.0
    private var tempMax: Double = 30.0

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

                mostrarLecturaSeleccionada(listaLecturas[clickedPosition], view)
            }
        )
        rvHistorialLecturas.adapter = adapter

        database = FirebaseDatabase.getInstance().reference.child("tanques")

        tanqueId = arguments?.getString("tanqueId")

        if (tanqueId != null) {
            cargarDatosTanque(tanqueId!!, view)
        } else {
            database.limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.e("AnalisisFragment", "No hay tanques en la base de datos")
                        return
                    }

                    val firstTanqueSnap = snapshot.children.first()
                    tanqueId = firstTanqueSnap.key
                    cargarDatosTanque(tanqueId!!, view)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al leer tanques", error.toException())
                }
            })
        }
    }

    private fun cargarDatosTanque(tanqueId: String, rootView: View) {
        val tanqueRef = database.child(tanqueId)
        tanqueRef.child("config").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(configSnap: DataSnapshot) {
                val config = configSnap
                tanqueName = config.child("name").getValue(String::class.java) ?: tanqueId
                phMin = config.child("phMin").getValue(Double::class.java) ?: phMin
                phMax = config.child("phMax").getValue(Double::class.java) ?: phMax
                tempMin = config.child("tempMin").getValue(Double::class.java) ?: tempMin
                tempMax = config.child("tempMax").getValue(Double::class.java) ?: tempMax

                val tvTitulo = rootView.findViewById<TextView>(R.id.tvTitulo)
                tvTitulo.text = "Análisis del Tanque: $tanqueName"

                cargarHistorial(tanqueRef.child("historial"), rootView)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer config", error.toException())
            }
        })
    }

    private fun cargarHistorial(historialRef: DatabaseReference, rootView: View) {
        historialRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(histSnapshot: DataSnapshot) {
                listaLecturas.clear()
                for (lecturaSnap in histSnapshot.children) {
                    val lectura = lecturaSnap.getValue(Lectura::class.java)
                    if (lectura != null) {
                        listaLecturas.add(lectura)
                    }
                }
                listaLecturas.reverse()

                expandedPosition = -1
                adapter.notifyDataSetChanged()

                if (listaLecturas.isNotEmpty()) {
                    mostrarLecturaSeleccionada(listaLecturas[0], rootView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer historial", error.toException())
            }
        })
    }

    private fun mostrarLecturaSeleccionada(lectura: Lectura, rootView: View) {
        if (!isAdded) return // Evitar crash si fragmento no está adjunto

        val tvFechaHora = rootView.findViewById<TextView>(R.id.tvFechaHoraActual)
        val tvValorPH = rootView.findViewById<TextView>(R.id.tvValorPHActual)
        val tvRangoPH = rootView.findViewById<TextView>(R.id.tvRangoPHActual)
        val tvRecomendacionPH = rootView.findViewById<TextView>(R.id.tvRecomendacionPHActual)

        val tvValorTemp = rootView.findViewById<TextView>(R.id.tvValorTempActual)
        val tvRangoTemp = rootView.findViewById<TextView>(R.id.tvRangoTempActual)
        val tvRecomendacionTemp = rootView.findViewById<TextView>(R.id.tvRecomendacionTempActual)

        val tvEstadoGeneral = rootView.findViewById<TextView>(R.id.tvEstadoGeneralActual)

        tvFechaHora.text = "Fecha: ${lectura.fecha ?: "--"} / Hora: ${lectura.hora ?: "--"}"
        // Formatear con 2 decimales
        tvValorPH.text = lectura.ph?.let { String.format("%.2f", it) } ?: "No hay datos"
        val ph = lectura.ph ?: 0.0
        when {
            ph < phMin -> {
                tvRangoPH.text = "pH bajo"
                tvRecomendacionPH.text = "Ajustar pH al alza"
                tvValorPH.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
            ph in phMin..phMax -> {
                tvRangoPH.text = "pH normal"
                tvRecomendacionPH.text = "Condiciones óptimas"
                tvValorPH.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            }
            else -> {
                tvRangoPH.text = "pH alto"
                tvRecomendacionPH.text = "Ajustar pH a la baja"
                tvValorPH.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
        }

        // Formatear temperatura con 2 decimales
        tvValorTemp.text = lectura.temperatura?.let { String.format("%.2f°C", it) } ?: "No hay datos"
        val temp = lectura.temperatura ?: 0.0
        when {
            temp < tempMin -> {
                tvRangoTemp.text = "Temperatura baja"
                tvRecomendacionTemp.text = "Aumentar temperatura"
                tvValorTemp.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
            temp in tempMin..tempMax -> {
                tvRangoTemp.text = "Temperatura normal"
                tvRecomendacionTemp.text = "Condiciones óptimas"
                tvValorTemp.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            }
            else -> {
                tvRangoTemp.text = "Temperatura alta"
                tvRecomendacionTemp.text = "Reducir temperatura"
                tvValorTemp.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
        }

        tvEstadoGeneral.text = if (ph in phMin..phMax && temp in tempMin..tempMax) {
            "Estado: Óptimo"
        } else {
            "Estado: Requiere atención"
        }
    }
}
