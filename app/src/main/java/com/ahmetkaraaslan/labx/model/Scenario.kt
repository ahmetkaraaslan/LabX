package com.ahmetkaraaslan.labx.model

data class Scenario(
    val id: Int,
    val title: String,
    val difficulty: String,
    val description: String,
    val allChemicals: List<String>,
    val correctChemicals: List<String>,
    val correctRatio: Map<String, Float>,
    val tempRange: TempRange,
    val pressureRange: PressureRange,
    val successMessage: String,
    val failureMessages: Map<String, String>
)

data class TempRange(
    val start: Float,
    val endInclusive: Float
)

data class PressureRange(
    val start: Float,
    val endInclusive: Float
)
