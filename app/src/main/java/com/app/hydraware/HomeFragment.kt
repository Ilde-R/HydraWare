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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout (debes crear este layout con esos IDs)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vincular views
        textWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)
        textTempValue = view.findViewById(R.id.textTempValue)
        textPhValue = view.findViewById(R.id.textPhValue)
        textDateTimeValue = view.findViewById(R.id.textDateTimeValue)
        textTempStatus = view.findViewById(R.id.textTempStatus)
        textPhStatus = view.findViewById(R.id.textPhStatus)

        database = FirebaseDatabase.getInstance().reference

        // Escuchar cambios en 'ultimaLectura' en Firebase
        val ultimaLecturaRef = database.child("ultimaLectura")
        ultimaLecturaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temperatura").getValue(Double::class.java)
                val ph = snapshot.child("ph").getValue(Double::class.java)
                val fecha = snapshot.child("fecha").getValue(String::class.java)
                val hora = snapshot.child("hora").getValue(String::class.java)

                // Actualizar temperatura y estado
                if (temp != null) {
                    val tempStatusText = when {
                        temp < 20 -> "Frío"
                        temp <= 30 -> "Estable"
                        else -> "Caliente"
                    }
                    textTempValue.text = "${temp}°C"
                    textTempStatus.text = tempStatusText
                    when (tempStatusText) {
                        "Frío", "Caliente" -> textTempStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                        "Estable" -> textTempStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                    }
                } else {
                    textTempValue.text = "N/A"
                    textTempStatus.text = "Cargando..."
                    textTempStatus.setTextColor(requireContext().getColor(android.R.color.darker_gray))
                }

                // Actualizar pH y estado
                if (ph != null) {
                    val phStatusText = when {
                        ph < 6.5 -> "Ácido"
                        ph <= 8.5 -> "Ideal"
                        else -> "Alcalino"
                    }
                    textPhValue.text = "$ph"
                    textPhStatus.text = phStatusText
                    when (phStatusText) {
                        "Ácido", "Alcalino" -> textPhStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                        "Ideal" -> textPhStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                    }
                } else {
                    textPhValue.text = "N/A"
                    textPhStatus.text = "Cargando..."
                    textPhStatus.setTextColor(requireContext().getColor(android.R.color.darker_gray))
                }

                // Actualizar fecha y hora
                if (fecha != null && hora != null) {
                    textDateTimeValue.text = "$fecha - $hora"
                } else {
                    textDateTimeValue.text = "N/A"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer datos de ultimaLectura", error.toException())
                textTempValue.text = "Error"
                textTempStatus.text = "Error"
                textPhValue.text = "Error"
                textPhStatus.text = "Error"
                textDateTimeValue.text = "Error"
                textTempStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                textPhStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            }
        })
    }
}
