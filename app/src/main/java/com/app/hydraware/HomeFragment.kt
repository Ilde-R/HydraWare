package com.app.hydraware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var containerTanques: LinearLayout
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        containerTanques = view.findViewById(R.id.containerTanques)
        database = FirebaseDatabase.getInstance().reference

        cargarTanques()
    }

    private fun cargarTanques() {
        containerTanques.removeAllViews()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val context = requireContext()

                for (tanqueSnapshot in snapshot.children) {
                    val tanqueName = tanqueSnapshot.key ?: continue
                    val configSnapshot = tanqueSnapshot.child("config")
                    val ultimaLecturaSnapshot = tanqueSnapshot.child("ultimaLectura")

                    val name = configSnapshot.child("name").getValue(String::class.java) ?: tanqueName
                    val ph = ultimaLecturaSnapshot.child("ph").getValue(Double::class.java)
                    val temp = ultimaLecturaSnapshot.child("temperatura").getValue(Double::class.java)
                    val fecha = ultimaLecturaSnapshot.child("fecha").getValue(String::class.java)
                    val hora = ultimaLecturaSnapshot.child("hora").getValue(String::class.java)

                    val itemView = layoutInflater.inflate(R.layout.item_tanque, containerTanques, false)

                    val textName = itemView.findViewById<TextView>(R.id.textTanqueName)
                    val textPh = itemView.findViewById<TextView>(R.id.textPhValue)
                    val textTemp = itemView.findViewById<TextView>(R.id.textTempValue)
                    val textFechaHora = itemView.findViewById<TextView>(R.id.textDateTimeValue)
                    val textPhStatus = itemView.findViewById<TextView>(R.id.textPhStatus)
                    val textTempStatus = itemView.findViewById<TextView>(R.id.textTempStatus)

                    textName.text = name

                    // pH
                    if (ph != null) {
                        val phStatusText = when {
                            ph < 6.5 -> "Ácido"
                            ph <= 8.5 -> "Ideal"
                            else -> "Alcalino"
                        }
                        textPh.text = String.format("%.2f", ph)
                        textPhStatus.text = phStatusText
                        val color = when (phStatusText) {
                            "Ácido", "Alcalino" -> context.getColor(android.R.color.holo_red_dark)
                            else -> context.getColor(android.R.color.holo_green_dark)
                        }
                        textPhStatus.setTextColor(color)
                        textPh.setTextColor(color)
                    } else {
                        textPh.text = "N/A"
                        textPhStatus.text = "Cargando..."
                        val gray = context.getColor(android.R.color.darker_gray)
                        textPhStatus.setTextColor(gray)
                        textPh.setTextColor(gray)
                    }

                    // Temperatura
                    if (temp != null) {
                        val tempStatusText = when {
                            temp < 20 -> "Frío"
                            temp <= 30 -> "Estable"
                            else -> "Caliente"
                        }
                        textTemp.text = String.format("%.1f°C", temp)
                        textTempStatus.text = tempStatusText
                        val color = when (tempStatusText) {
                            "Frío", "Caliente" -> context.getColor(android.R.color.holo_red_dark)
                            else -> context.getColor(android.R.color.holo_green_dark)
                        }
                        textTempStatus.setTextColor(color)
                        textTemp.setTextColor(color)
                    } else {
                        textTemp.text = "N/A"
                        textTempStatus.text = "Cargando..."
                        val gray = context.getColor(android.R.color.darker_gray)
                        textTempStatus.setTextColor(gray)
                        textTemp.setTextColor(gray)
                    }

                    // Fecha y hora
                    textFechaHora.text = if (fecha != null && hora != null) "$fecha - $hora" else "N/A"

                    containerTanques.addView(itemView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar tanques: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
