package com.app.hydraware

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabCenter: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        fabCenter = findViewById(R.id.fab_center)

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Item inicial seleccionado
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Listener de navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_analysis -> loadFragment(AnalisisFragment())
                else -> false
            }
        }

        // Acción del FAB
        fabCenter.setOnClickListener {
            loadFragment(TankFragment())
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
