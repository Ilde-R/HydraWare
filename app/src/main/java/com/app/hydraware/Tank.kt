package com.app.hydraware

data class Tank (
    val id: String = "",
    val name: String = "",
    val hasPh: Boolean = false,
    val hasTemperatura: Boolean = false,
    val phMin: Double = 0.0,
    val phMax: Double = 0.0,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0
)