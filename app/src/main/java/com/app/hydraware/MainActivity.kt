package com.app.hydraware

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabCenter: FloatingActionButton

    // Listener guardado como lambda
    private val navListener: (MenuItem) -> Boolean = { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                loadFragment(HomeFragment())
                true
            }
            R.id.nav_analysis -> {
                loadFragment(AnalisisFragment())
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        fabCenter = findViewById(R.id.fab_center)

        // Asignar listener de navegación
        bottomNavigationView.setOnItemSelectedListener(navListener)

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        // Listener para FAB
        fabCenter.setOnClickListener {
            loadFragment(TankFragment())
            // No actualizamos el ícono porque TankFragment no está en el menú
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    // Función para cambiar fragmento y actualizar ícono seleccionado opcionalmente
    fun switchFragment(fragment: androidx.fragment.app.Fragment, menuItemId: Int? = null) {
        // Evitar disparar listener al cambiar selectedItemId programáticamente
        bottomNavigationView.setOnItemSelectedListener(null)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        if (menuItemId != null) {
            bottomNavigationView.selectedItemId = menuItemId
        }

        // Reasignar listener
        bottomNavigationView.setOnItemSelectedListener(navListener)
    }
}
