package com.app.hydraware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.hydraware.databinding.FragmentTankBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TankFragment : Fragment() {

    private var _binding: FragmentTankBinding? = null
    private val binding get() = _binding!!

    private var tanqueIdEdit: String? = null  // ID del tanque en edición

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar/Ocultar rangos según checkbox
        binding.cbPh.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutPhRange.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.cbTemperature.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutTempRange.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Leer argumentos para modo edición
        val args = arguments
        if (args != null && args.getString("modo") == "editar") {
            tanqueIdEdit = args.getString("tanqueId")
            tanqueIdEdit?.let { cargarDatosTanque(it) }
            binding.etTankName.setText(args.getString("nombreTanque") ?: "")
        }

        binding.btnSaveTank.setOnClickListener {
            guardarConfigTanque()
        }

        binding.btnEnviarFalso.setOnClickListener {
            enviarRegistroFalso()
        }
    }

    private fun cargarDatosTanque(tanqueId: String) {
        if (tanqueId.isBlank()) return

        val configRef = FirebaseDatabase.getInstance()
            .getReference("tanques")
            .child(tanqueId)
            .child("config")

        configRef.get().addOnSuccessListener { snapshot ->
            val b = _binding ?: return@addOnSuccessListener

            if (snapshot.exists()) {
                b.etTankName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                b.cbPh.isChecked = snapshot.child("hasPh").getValue(Boolean::class.java) ?: false
                b.cbTemperature.isChecked = snapshot.child("hasTemperatura").getValue(Boolean::class.java) ?: false
                b.etPhMin.setText(snapshot.child("phMin").getValue(Double::class.java)?.toString() ?: "0.0")
                b.etPhMax.setText(snapshot.child("phMax").getValue(Double::class.java)?.toString() ?: "0.0")
                b.etTempMin.setText(snapshot.child("tempMin").getValue(Double::class.java)?.toString() ?: "0.0")
                b.etTempMax.setText(snapshot.child("tempMax").getValue(Double::class.java)?.toString() ?: "0.0")

                // Mostrar/ocultar rangos
                b.layoutPhRange.visibility = if (b.cbPh.isChecked) View.VISIBLE else View.GONE
                b.layoutTempRange.visibility = if (b.cbTemperature.isChecked) View.VISIBLE else View.GONE
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }
    private fun guardarConfigTanque() {
        val name = binding.etTankName.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        // Si está editando usa tanqueIdEdit, si no usa nombre limpio
        val nodeId = tanqueIdEdit ?: name.replace(".", "_")

        val configRef = database.getReference("tanques").child(nodeId).child("config")

        val hasPh = binding.cbPh.isChecked
        val hasTemperatura = binding.cbTemperature.isChecked
        val phMin = binding.etPhMin.text.toString().toDoubleOrNull() ?: 0.0
        val phMax = binding.etPhMax.text.toString().toDoubleOrNull() ?: 0.0
        val tempMin = binding.etTempMin.text.toString().toDoubleOrNull() ?: 0.0
        val tempMax = binding.etTempMax.text.toString().toDoubleOrNull() ?: 0.0

        val configData = mapOf(
            "name" to name,
            "hasPh" to hasPh,
            "hasTemperatura" to hasTemperatura,
            "phMin" to phMin,
            "phMax" to phMax,
            "tempMin" to tempMin,
            "tempMax" to tempMax
        )

        configRef.setValue(configData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun enviarRegistroFalso() {
        val name = binding.etTankName.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val nodeId = tanqueIdEdit ?: name.replace(".", "_")
        val database = FirebaseDatabase.getInstance()
        val historialRef = database.getReference("tanques").child(nodeId).child("historial")
        val ultimaLecturaRef = database.getReference("tanques").child(nodeId).child("ultimaLectura")

        val sdfTimestamp = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val now = Date()

        val timestamp = sdfTimestamp.format(now)
        val fecha = sdfFecha.format(now)
        val hora = sdfHora.format(now)

        val phFalso = 7.2
        val tempFalso = 24.5

        val lecturaData = mapOf(
            "fecha" to fecha,
            "hora" to hora,
            "ph" to phFalso,
            "temperatura" to tempFalso
        )

        historialRef.child(timestamp).setValue(lecturaData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Registro falso enviado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        ultimaLecturaRef.setValue(lecturaData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Última lectura actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
