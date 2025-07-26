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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar/Ocultar rangos
        binding.cbPh.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutPhRange.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.cbTemperature.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutTempRange.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnSaveTank.setOnClickListener {
            guardarConfigTanque()
        }

        binding.btnEnviarFalso.setOnClickListener {
            enviarRegistroFalso()
        }
    }

    private fun guardarConfigTanque() {
        val name = binding.etTankName.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        // Usar el nombre como nodo raíz, cuidando caracteres no válidos (opcional)
        val sanitizedName = name.replace(".", "_")  // Firebase no permite puntos en claves
        val configRef = database.getReference("tanques").child(sanitizedName).child("config")

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

        val sanitizedName = name.replace(".", "_")
        val database = FirebaseDatabase.getInstance()
        val historialRef = database.getReference("tanques").child(sanitizedName).child("historial")
        val ultimaLecturaRef = database.getReference("tanques").child(sanitizedName).child("ultimaLectura")

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
