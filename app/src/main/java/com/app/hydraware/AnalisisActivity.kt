package com.app.hydraware

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AnalisisActivity : AppCompatActivity() {

    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var database: DatabaseReference
    private val listaHistorial = mutableListOf<Lectura>()
    private lateinit var adapter: LecturaAdapter

    // Views de pantalla grande
    private lateinit var tvFechaHora: TextView
    private lateinit var tvValorPH: TextView
    private lateinit var tvValorTemp: TextView
    private lateinit var tvEstadoGeneral: TextView
    private lateinit var tvRecomendacionPH: TextView
    private lateinit var tvRecomendacionTemp: TextView

    // Barra y FAB
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabCenter: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisis)

        // Inicializar Views grandes
        tvFechaHora = findViewById(R.id.tvFechaHora)
        tvValorPH = findViewById(R.id.tvValorPH)
        tvValorTemp = findViewById(R.id.tvValorTemp)
        tvEstadoGeneral = findViewById(R.id.tvEstadoGeneral)
        tvRecomendacionPH = findViewById(R.id.tvRecomendacionPH)
        tvRecomendacionTemp = findViewById(R.id.tvRecomendacionTemp)

        // RecyclerView
        recyclerHistorial = findViewById(R.id.recyclerHistorial)
        recyclerHistorial.layoutManager = LinearLayoutManager(this)

        // Crear adapter con listener para selección
        adapter = LecturaAdapter(listaHistorial) { lecturaSeleccionada ->
            actualizarDatosPantallaGrande(lecturaSeleccionada)
        }
        recyclerHistorial.adapter = adapter

        // Firebase DB
        database = FirebaseDatabase.getInstance().reference.child("historial")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaHistorial.clear()
                for (dato in snapshot.children) {
                    val lectura = dato.getValue(Lectura::class.java)
                    if (lectura != null) {
                        listaHistorial.add(lectura)
                    }
                }
                adapter.notifyDataSetChanged()

                if (listaHistorial.isNotEmpty()) {
                    actualizarDatosPantallaGrande(listaHistorial[0])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnalisisActivity", "Error al leer Firebase: ${error.message}")
            }
        })

        // BottomNavigationView y FAB
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        fabCenter = findViewById(R.id.fab_center)

        bottomNavigationView.selectedItemId = R.id.nav_analysis

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> true // Ya estamos aquí
                else -> false
            }
        }

        fabCenter.setOnClickListener {
            Toast.makeText(this, "Botón central (FAB) pulsado en Analisis", Toast.LENGTH_SHORT).show()
            // Aquí puedes agregar la acción que quieres para el FAB en AnalisisActivity
        }
    }

    private fun actualizarDatosPantallaGrande(lectura: Lectura) {
        tvFechaHora.text = "Fecha: ${lectura.fecha} / Hora: ${lectura.hora}"
        tvValorPH.text = lectura.ph.toString()
        tvValorTemp.text = "${lectura.temperatura} °C"

        val phOk = lectura.ph in 6.5..7.5
        val tempOk = lectura.temperatura in 24.0..28.0

        if (phOk && tempOk) {
            tvEstadoGeneral.text = "Estado: Normal"
            tvEstadoGeneral.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            tvRecomendacionPH.text = "El nivel de pH es adecuado."
            tvRecomendacionPH.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            tvRecomendacionTemp.text = "Temperatura dentro del rango ideal."
            tvRecomendacionTemp.setTextColor(getColorCompat(android.R.color.holo_green_dark))
        } else {
            tvEstadoGeneral.text = "Estado: Alerta"
            tvEstadoGeneral.setTextColor(getColorCompat(android.R.color.holo_red_dark))

            if (!phOk) {
                tvRecomendacionPH.text = "Revisar nivel de pH."
                tvRecomendacionPH.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            } else {
                tvRecomendacionPH.text = "pH adecuado."
                tvRecomendacionPH.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            }

            if (!tempOk) {
                tvRecomendacionTemp.text = "Revisar temperatura."
                tvRecomendacionTemp.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            } else {
                tvRecomendacionTemp.text = "Temperatura adecuada."
                tvRecomendacionTemp.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            }
        }
    }

    private fun getColorCompat(colorId: Int): Int {
        return androidx.core.content.ContextCompat.getColor(this, colorId)
    }
}
