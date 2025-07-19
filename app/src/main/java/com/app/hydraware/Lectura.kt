package com.app.hydraware

data class Lectura(
    val fecha: String = "",
    val hora: String = "",
    val ph: Double = 0.0,
    val temperatura: Double = 0.0
)
