package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var textWelcomeMessage: TextView
    private lateinit var textTempValue: TextView
    private lateinit var textPhValue: TextView
    private lateinit var textDateTimeValue: TextView
    private lateinit var textTempStatus: TextView
    private lateinit var textPhStatus: TextView

    private lateinit var database: DatabaseReference
    private lateinit var ultimaLecturaRef: DatabaseReference
    private lateinit var lecturaListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)
        textTempValue = view.findViewById(R.id.textTempValue)
        textPhValue = view.findViewById(R.id.textPhValue)
        textDateTimeValue = view.findViewById(R.id.textDateTimeValue)
        textTempStatus = view.findViewById(R.id.textTempStatus)
        textPhStatus = view.findViewById(R.id.textPhStatus)

        database = FirebaseDatabase.getInstance().reference
        ultimaLecturaRef = database.child("ultimaLectura")

        lecturaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temperatura").getValue(Double::class.java)
                val ph = snapshot.child("ph").getValue(Double::class.java)
                val fecha = snapshot.child("fecha").getValue(String::class.java)
                val hora = snapshot.child("hora").getValue(String::class.java)

                context?.let {
                    // Temperatura
                    if (temp != null) {
                        val tempStatusText = when {
                            temp < 20 -> "Frío"
                            temp <= 30 -> "Estable"
                            else -> "Caliente"
                        }
                        textTempValue.text = "$temp°C"
                        textTempStatus.text = tempStatusText
                        val color = when (tempStatusText) {
                            "Frío", "Caliente" -> it.getColor(android.R.color.holo_red_dark)
                            else -> it.getColor(android.R.color.holo_green_dark)
                        }
                        textTempStatus.setTextColor(color)
                    } else {
                        textTempValue.text = "N/A"
                        textTempStatus.text = "Cargando..."
                        textTempStatus.setTextColor(it.getColor(android.R.color.darker_gray))
                    }

                    // pH
                    if (ph != null) {
                        val phStatusText = when {
                            ph < 6.5 -> "Ácido"
                            ph <= 8.5 -> "Ideal"
                            else -> "Alcalino"
                        }
                        textPhValue.text = "$ph"
                        textPhStatus.text = phStatusText
                        val color = when (phStatusText) {
                            "Ácido", "Alcalino" -> it.getColor(android.R.color.holo_red_dark)
                            else -> it.getColor(android.R.color.holo_green_dark)
                        }
                        textPhStatus.setTextColor(color)
                    } else {
                        textPhValue.text = "N/A"
                        textPhStatus.text = "Cargando..."
                        textPhStatus.setTextColor(it.getColor(android.R.color.darker_gray))
                    }

                    // Fecha y hora
                    if (fecha != null && hora != null) {
                        textDateTimeValue.text = "$fecha - $hora"
                    } else {
                        textDateTimeValue.text = "N/A"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer datos de ultimaLectura", error.toException())
                context?.let {
                    textTempValue.text = "Error"
                    textTempStatus.text = "Error"
                    textPhValue.text = "Error"
                    textPhStatus.text = "Error"
                    textDateTimeValue.text = "Error"
                    textTempStatus.setTextColor(it.getColor(android.R.color.holo_red_dark))
                    textPhStatus.setTextColor(it.getColor(android.R.color.holo_red_dark))
                }
            }
        }

        ultimaLecturaRef.addValueEventListener(lecturaListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 🔥 Importante: eliminar el listener al destruir la vista
        ultimaLecturaRef.removeEventListener(lecturaListener)
    }
}
