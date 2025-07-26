package com.app.hydraware

import android.os.Bundle
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
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_analysis -> loadFragment(AnalisisFragment())
                else -> false
            }
        }

        fabCenter.setOnClickListener {
            loadFragment(TankFragment())
            // Si quieres también actualizar el menú al presionar FAB, hazlo aquí si quieres
            // bottomNavigationView.selectedItemId = R.id.nav_some_id
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    // Función pública para cambiar fragmento y actualizar ícono seleccionado
    fun switchFragment(fragment: androidx.fragment.app.Fragment, menuItemId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        bottomNavigationView.selectedItemId = menuItemId
    }
}
