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

    private var expandedPosition = -1

    // Parámetros del tanque
    private var tanqueId: String? = null
    private var tanqueName: String? = null
    private var phMin: Double = 6.5  // valores por defecto
    private var phMax: Double = 8.5
    private var tempMin: Double = 20.0
    private var tempMax: Double = 30.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tanqueId = arguments?.getString("tanqueId")
        tanqueName = arguments?.getString("tanqueName") // clave corregida

        // Recibir parámetros rango si se pasaron
        phMin = arguments?.getDouble("phMin") ?: phMin
        phMax = arguments?.getDouble("phMax") ?: phMax
        tempMin = arguments?.getDouble("tempMin") ?: tempMin
        tempMax = arguments?.getDouble("tempMax") ?: tempMax
    }

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

        database = FirebaseDatabase.getInstance().reference

        tanqueId?.let { id ->
            val historialRef = database.child("tanques").child(id).child("historial")
            historialRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaLecturas.clear()
                    for (lecturaSnap in snapshot.children) {
                        val lectura = lecturaSnap.getValue(Lectura::class.java)
                        if (lectura != null) {
                            listaLecturas.add(lectura)
                        }
                    }
                    listaLecturas.reverse()

                    expandedPosition = -1
                    adapter.notifyDataSetChanged()

                    if (listaLecturas.isNotEmpty()) {
                        mostrarLecturaSeleccionada(listaLecturas[0], view)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al leer historial", error.toException())
                }
            })

            val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
            tvTitulo.text = "Análisis del Tanque: ${tanqueName ?: id}"
        } ?: run {
            Log.e("AnalisisFragment", "No se recibió tanqueId")
        }
    }

    private fun mostrarLecturaSeleccionada(lectura: Lectura, rootView: View) {
        val tvFechaHora = rootView.findViewById<TextView>(R.id.tvFechaHoraActual)
        val tvValorPH = rootView.findViewById<TextView>(R.id.tvValorPHActual)
        val tvRangoPH = rootView.findViewById<TextView>(R.id.tvRangoPHActual)
        val tvRecomendacionPH = rootView.findViewById<TextView>(R.id.tvRecomendacionPHActual)

        val tvValorTemp = rootView.findViewById<TextView>(R.id.tvValorTempActual)
        val tvRangoTemp = rootView.findViewById<TextView>(R.id.tvRangoTempActual)
        val tvRecomendacionTemp = rootView.findViewById<TextView>(R.id.tvRecomendacionTempActual)

        val tvEstadoGeneral = rootView.findViewById<TextView>(R.id.tvEstadoGeneralActual)

        tvFechaHora.text = "Fecha: ${lectura.fecha ?: "--"} / Hora: ${lectura.hora ?: "--"}"
        tvValorPH.text = lectura.ph?.toString() ?: "No hay datos"
        val ph = lectura.ph ?: 0.0
        when {
            ph < phMin -> {
                tvRangoPH.text = "pH bajo"
                tvRecomendacionPH.text = "Ajustar pH al alza"
            }
            ph in phMin..phMax -> {
                tvRangoPH.text = "pH normal"
                tvRecomendacionPH.text = "Condiciones óptimas"
            }
            else -> {
                tvRangoPH.text = "pH alto"
                tvRecomendacionPH.text = "Ajustar pH a la baja"
            }
        }

        tvValorTemp.text = lectura.temperatura?.toString() ?: "No hay datos"
        val temp = lectura.temperatura ?: 0.0
        when {
            temp < tempMin -> {
                tvRangoTemp.text = "Temperatura baja"
                tvRecomendacionTemp.text = "Aumentar temperatura"
            }
            temp in tempMin..tempMax -> {
                tvRangoTemp.text = "Temperatura normal"
                tvRecomendacionTemp.text = "Condiciones óptimas"
            }
            else -> {
                tvRangoTemp.text = "Temperatura alta"
                tvRecomendacionTemp.text = "Reducir temperatura"
            }
        }

        tvEstadoGeneral.text = if (ph in phMin..phMax && temp in tempMin..tempMax) {
            "Estado: Óptimo"
        } else {
            "Estado: Requiere atención"
        }
    }
}
