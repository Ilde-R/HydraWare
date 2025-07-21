package com.app.hydraware

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    // TextViews para los valores
    private lateinit var textWelcomeMessage: TextView
    private lateinit var textTempValue: TextView
    private lateinit var textPhValue: TextView
    private lateinit var textDateTimeValue: TextView

    // TextViews para los estados
    private lateinit var textTempStatus: TextView
    private lateinit var textPhStatus: TextView

    private lateinit var database: DatabaseReference

    // Botón para abrir AnalisisActivity
    private lateinit var btnVerHistorial: Button

    // Barra y FAB
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabCenter: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference

        // Vincular TextViews con el layout
        textWelcomeMessage = findViewById(R.id.textWelcomeMessage)
        textTempValue = findViewById(R.id.textTempValue)
        textPhValue = findViewById(R.id.textPhValue)
        textDateTimeValue = findViewById(R.id.textDateTimeValue)
        textTempStatus = findViewById(R.id.textTempStatus)
        textPhStatus = findViewById(R.id.textPhStatus)

        // BottomNavigationView y FAB
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        fabCenter = findViewById(R.id.fab_center)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home seleccionado", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_analysis -> {
                    // Abrir AnalisisActivity
                    val intent = Intent(this, AnalisisActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fabCenter.setOnClickListener {
            Toast.makeText(this, "Botón central (FAB) pulsado", Toast.LENGTH_SHORT).show()
            // Aquí tu acción para el FAB, ej. abrir pantalla para crear post
        }

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
                        "Frío", "Caliente" -> textTempStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        "Estable" -> textTempStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    }
                } else {
                    textTempValue.text = "N/A"
                    textTempStatus.text = "Cargando..."
                    textTempStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
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
                        "Ácido", "Alcalino" -> textPhStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        "Ideal" -> textPhStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    }
                } else {
                    textPhValue.text = "N/A"
                    textPhStatus.text = "Cargando..."
                    textPhStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
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
                textTempStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                textPhStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        })
    }
}
