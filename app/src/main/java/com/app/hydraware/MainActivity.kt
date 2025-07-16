package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var textTempValue: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference

        // Asocia el TextView del layout
        textTempValue = findViewById(R.id.textTempValue)

        // Escucha los cambios de temperatura de FIREBASE
        val tempRef = database.child("sensores").child("temperatura").child("temperatura")
        tempRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.getValue(Double::class.java)
                if (temp != null) {
                    val status = when {
                        temp < 20 -> "Frío"
                        temp <= 30 -> "Estable"
                        else -> "Caliente"
                    }
                    textTempValue.text = "Temp: ${temp}°C - $status"
                } else {
                    textTempValue.text = "Temp: N/A"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer temperatura", error.toException())
                textTempValue.text = "Error al cargar"
            }
        })
    }
}
