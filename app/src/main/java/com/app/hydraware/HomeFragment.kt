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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var containerTanques: LinearLayout
    private var tanquesListener: ValueEventListener? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var statusIndicator: View
    private lateinit var indicatorPh: View
    private lateinit var indicatorTemp: View
    private lateinit var indicatorSystem: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        containerTanques = view.findViewById(R.id.containerTanques)
        statusIndicator = view.findViewById(R.id.statusIndicator)
        indicatorPh = statusIndicator.findViewById(R.id.indicatorPh)
        indicatorTemp = statusIndicator.findViewById(R.id.indicatorTemp)
        indicatorSystem = statusIndicator.findViewById(R.id.indicatorSystem)
        
        database = FirebaseDatabase.getInstance().reference.child("tanques")
        notificationManager = NotificationManager(requireContext())
        
        // Solicitar permisos de notificación si es necesario
        requestNotificationPermission()
        
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

                    val textName = itemView.findViewById<TextView>(R.id.textEstanqueName)
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
                    
                    // Verificar y mostrar notificaciones
                    notificationManager.checkAndNotify(
                        tanqueId,
                        name,
                        ph,
                        temperatura,
                        phMin,
                        phMax,
                        tempMin,
                        tempMax
                    )
                    
                    // Actualizar indicadores de estado
                    updateStatusIndicators(ph, temperatura, phMin, phMax, tempMin, tempMax)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar tanques", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", error.message)
            }
        }

        database.addValueEventListener(tanquesListener!!)
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
    
    private fun updateStatusIndicators(ph: Double, temperatura: Double, phMin: Double, phMax: Double, tempMin: Double, tempMax: Double) {
        // Actualizar indicador de pH
        val phColor = when {
            ph < phMin || ph > phMax -> Color.RED
            ph in (phMin + 1)..(phMax - 1) -> Color.GREEN
            else -> Color.parseColor("#FFA500") // Naranja para valores cercanos al límite
        }
        indicatorPh.setBackgroundColor(phColor)
        
        // Actualizar indicador de temperatura
        val tempColor = when {
            temperatura < tempMin || temperatura > tempMax -> Color.RED
            temperatura in (tempMin + 2)..(tempMax - 2) -> Color.GREEN
            else -> Color.parseColor("#FFA500") // Naranja para valores cercanos al límite
        }
        indicatorTemp.setBackgroundColor(tempColor)
        
        // Actualizar indicador general del sistema
        val systemColor = when {
            phColor == Color.RED || tempColor == Color.RED -> Color.RED
            phColor == Color.GREEN && tempColor == Color.GREEN -> Color.GREEN
            else -> Color.parseColor("#FFA500") // Naranja si hay alguna advertencia
        }
        indicatorSystem.setBackgroundColor(systemColor)
    }
}
