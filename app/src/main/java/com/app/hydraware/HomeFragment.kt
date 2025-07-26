package com.app.hydraware

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var containerTanques: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        containerTanques = view.findViewById(R.id.containerTanques)
        database = FirebaseDatabase.getInstance().reference.child("tanques")
        return view
    }

    override fun onResume() {
        super.onResume()
        cargarTanques()
    }

    private fun cargarTanques() {
        containerTanques.removeAllViews() // Limpiar para evitar duplicados

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Total tanques: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    Toast.makeText(context, "No hay tanques disponibles", Toast.LENGTH_SHORT).show()
                    return
                }

                for (tanqueSnapshot in snapshot.children) {
                    val config = tanqueSnapshot.child("config")
                    val lectura = tanqueSnapshot.child("ultimaLectura")

                    val name = config.child("name").getValue(String::class.java) ?: tanqueSnapshot.key ?: "Sin nombre"
                    val ph = lectura.child("ph").getValue(Double::class.java) ?: 0.0
                    val temperatura = lectura.child("temperatura").getValue(Double::class.java) ?: 0.0
                    val fecha = lectura.child("fecha").getValue(String::class.java) ?: "-"
                    val hora = lectura.child("hora").getValue(String::class.java) ?: "-"

                    Toast.makeText(context, "Cargado: $name", Toast.LENGTH_SHORT).show()

                    val itemView = layoutInflater.inflate(R.layout.item_tanque, containerTanques, false)

                    val textName = itemView.findViewById<TextView>(R.id.textTanqueName)
                    val textPh = itemView.findViewById<TextView>(R.id.textPhValue)
                    val textPhStatus = itemView.findViewById<TextView>(R.id.textPhStatus)
                    val textTemp = itemView.findViewById<TextView>(R.id.textTempValue)
                    val textTempStatus = itemView.findViewById<TextView>(R.id.textTempStatus)
                    val textFechaHora = itemView.findViewById<TextView>(R.id.textDateTimeValue)

                    textName.text = name
                    textPh.text = "pH: $ph"
                    textTemp.text = "Temp: $temperatura °C"
                    textFechaHora.text = "Última lectura: $fecha $hora"

                    // Estado de pH
                    if (ph < 6.5 || ph > 8.5) {
                        textPhStatus.text = "pH FUERA DE RANGO"
                        textPhStatus.setTextColor(Color.RED)
                    } else {
                        textPhStatus.text = "pH Normal"
                        textPhStatus.setTextColor(Color.GREEN)
                    }

                    // Estado de Temperatura
                    if (temperatura < 24 || temperatura > 30) {
                        textTempStatus.text = "TEMP FUERA DE RANGO"
                        textTempStatus.setTextColor(Color.RED)
                    } else {
                        textTempStatus.text = "TEMP Normal"
                        textTempStatus.setTextColor(Color.GREEN)
                    }

                    // Acción al hacer clic: ir a análisis via MainActivity
                    itemView.setOnClickListener {
                        val fragment = AnalisisFragment()
                        val bundle = Bundle().apply {
                            putString("tanqueId", tanqueSnapshot.key)
                            putString("nombreTanque", name)
                            putDouble("ph", ph)
                            putDouble("temperatura", temperatura)
                            putString("fecha", fecha)
                            putString("hora", hora)
                        }
                        fragment.arguments = bundle

                        // Llama la función pública de MainActivity para hacer el cambio y actualizar el menú
                        (activity as? MainActivity)?.switchFragment(fragment, R.id.nav_analysis)
                    }

                    containerTanques.addView(itemView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar tanques", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", error.message)
            }
        })
    }
}
