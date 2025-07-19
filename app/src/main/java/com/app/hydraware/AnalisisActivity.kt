package com.app.hydraware

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AnalisisActivity : AppCompatActivity() {

    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var database: DatabaseReference
    private val listaHistorial = mutableListOf<Lectura>()
    private lateinit var adapter: LecturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisis)

        // Inicializar RecyclerView
        recyclerHistorial = findViewById(R.id.recyclerHistorial)
        recyclerHistorial.layoutManager = LinearLayoutManager(this)
        adapter = LecturaAdapter(listaHistorial)
        recyclerHistorial.adapter = adapter

        // Conectarse a Firebase
        database = FirebaseDatabase.getInstance().reference.child("historial")

        // Escuchar cambios
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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnalisisActivity", "Error al leer Firebase: ${error.message}")
            }
        })
    }
}
