package com.app.hydraware

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var containerTanques: LinearLayout
    private var tanquesListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        containerTanques = view.findViewById(R.id.containerTanques)
        database = FirebaseDatabase.getInstance().reference.child("tanques")
        return view
    }

    override fun onStart() {
        super.onStart()
        cargarTanques()
    }

    override fun onStop() {
        super.onStop()
        tanquesListener?.let { database.removeEventListener(it) }
    }

    private fun cargarTanques() {
        containerTanques.removeAllViews()

        tanquesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerTanques.removeAllViews()

                if (!snapshot.exists()) {
                    Toast.makeText(context, "No hay tanques disponibles", Toast.LENGTH_SHORT).show()
                    return
                }

                for (tanqueSnapshot in snapshot.children) {
                    val config = tanqueSnapshot.child("config")
                    val lectura = tanqueSnapshot.child("ultimaLectura")
                    val tanqueId = tanqueSnapshot.key ?: continue

                    val name = config.child("name").getValue(String::class.java) ?: "Sin nombre"
                    val ph = lectura.child("ph").getValue(Double::class.java) ?: 0.0
                    val temperatura = lectura.child("temperatura").getValue(Double::class.java) ?: 0.0
                    val fecha = lectura.child("fecha").getValue(String::class.java) ?: "-"
                    val hora = lectura.child("hora").getValue(String::class.java) ?: "-"

                    val phMin = config.child("phMin").getValue(Double::class.java) ?: 0.0
                    val phMax = config.child("phMax").getValue(Double::class.java) ?: 14.0
                    val tempMin = config.child("tempMin").getValue(Double::class.java) ?: -50.0
                    val tempMax = config.child("tempMax").getValue(Double::class.java) ?: 100.0

                    val itemView = layoutInflater.inflate(R.layout.item_tanque, containerTanques, false)

                    val textName = itemView.findViewById<TextView>(R.id.textTanqueName)
                    val textPh = itemView.findViewById<TextView>(R.id.textPhValue)
                    val textPhStatus = itemView.findViewById<TextView>(R.id.textPhStatus)
                    val textTemp = itemView.findViewById<TextView>(R.id.textTempValue)
                    val textTempStatus = itemView.findViewById<TextView>(R.id.textTempStatus)
                    val textFechaHora = itemView.findViewById<TextView>(R.id.textDateTimeValue)

                    textName.text = name
                    textPh.text = "pH: %.2f".format(ph)
                    textTemp.text = "Temp: %.2f°C".format(temperatura)
                    textFechaHora.text = "Última lectura: $fecha $hora"

                    // Validar pH
                    if (ph < phMin || ph > phMax) {
                        textPhStatus.text = "pH FUERA DE RANGO"
                        textPhStatus.setTextColor(Color.RED)
                    } else {
                        textPhStatus.text = "pH Normal"
                        textPhStatus.setTextColor(Color.GREEN)
                    }

                    // Validar Temp
                    if (temperatura < tempMin || temperatura > tempMax) {
                        textTempStatus.text = "TEMP FUERA DE RANGO"
                        textTempStatus.setTextColor(Color.RED)
                    } else {
                        textTempStatus.text = "TEMP Normal"
                        textTempStatus.setTextColor(Color.GREEN)
                    }

                    val btnEdit = itemView.findViewById<Button>(R.id.btnEdit)
                    val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

                    itemView.setOnClickListener {
                        val fragment = AnalisisFragment()
                        val bundle = Bundle().apply {
                            putString("tanqueId", tanqueId)
                            putString("nombreTanque", name)
                            putDouble("ph", ph)
                            putDouble("temperatura", temperatura)
                            putString("fecha", fecha)
                            putString("hora", hora)
                        }
                        fragment.arguments = bundle
                        (activity as? MainActivity)?.switchFragment(fragment, R.id.nav_analysis)
                    }

                    btnEdit.setOnClickListener {
                        val fragment = TankFragment()
                        val bundle = Bundle().apply {
                            putString("modo", "editar")
                            putString("tanqueId", tanqueId)
                            putString("nombreTanque", name)
                        }
                        fragment.arguments = bundle
                        (activity as? MainActivity)?.switchFragment(fragment, R.id.nav_home)
                    }

                    btnDelete.setOnClickListener {
                        database.child(tanqueId).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Tanque eliminado correctamente", Toast.LENGTH_SHORT).show()
                                cargarTanques()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar tanque", Toast.LENGTH_SHORT).show()
                            }
                    }

                    containerTanques.addView(itemView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar tanques", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", error.message)
            }
        }

        database.addValueEventListener(tanquesListener!!)
    }
}
