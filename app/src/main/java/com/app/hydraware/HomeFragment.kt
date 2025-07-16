package com.app.hydraware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var textTempValue: TextView
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        textTempValue = view.findViewById(R.id.textTempValue)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        database = FirebaseDatabase.getInstance().reference
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