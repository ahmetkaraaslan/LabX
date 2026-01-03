package com.ahmetkaraaslan.labx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetkaraaslan.labx.model.Scenario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI state for the Scenario screen
data class ScenarioUiState(
    val temperature: Float = 25f,
    val pressure: Float = 1f,
    val selectedChemicals: Set<String> = emptySet(),
    val chemicalAmounts: Map<String, Float> = emptyMap(),
    val showReactionEquation: Boolean = false,
)

// Events that the UI can send to the ViewModel
sealed interface ScenarioEvent {
    data class TemperatureChanged(val value: Float) : ScenarioEvent
    data class PressureChanged(val value: Float) : ScenarioEvent
    data class ChemicalSelected(val chemical: String) : ScenarioEvent
    data class ChemicalAmountChanged(val chemical: String, val amount: Float) : ScenarioEvent
    object ToggleReactionEquation : ScenarioEvent
    object StartReaction : ScenarioEvent
}

// Results of the reaction to be shown to the user
sealed interface ReactionResult {
    val title: String
    val message: String

    data class Success(override val title: String, override val message: String) : ReactionResult
    data class Failure(override val title: String, override val message: String) : ReactionResult
}

class ScenarioViewModel(private val scenario: Scenario) : ViewModel() {

    private val _uiState = MutableStateFlow(ScenarioUiState())
    val uiState = _uiState.asStateFlow()

    private val _reactionResult = MutableSharedFlow<ReactionResult>()
    val reactionResult = _reactionResult.asSharedFlow()

    val shuffledChemicals = scenario.allChemicals.shuffled()

    fun onEvent(event: ScenarioEvent) {
        when (event) {
            is ScenarioEvent.TemperatureChanged -> _uiState.update { it.copy(temperature = event.value) }
            is ScenarioEvent.PressureChanged -> _uiState.update { it.copy(pressure = event.value) }
            is ScenarioEvent.ChemicalSelected -> handleChemicalSelection(event.chemical)
            is ScenarioEvent.ChemicalAmountChanged -> {
                _uiState.update { state ->
                    val newAmounts = state.chemicalAmounts + (event.chemical to event.amount)
                    state.copy(chemicalAmounts = newAmounts)
                }
            }
            ScenarioEvent.ToggleReactionEquation -> _uiState.update { it.copy(showReactionEquation = !it.showReactionEquation) }
            ScenarioEvent.StartReaction -> validateAndStartReaction()
        }
    }

    private fun handleChemicalSelection(chemical: String) {
        _uiState.update { state ->
            val newSelected = if (chemical in state.selectedChemicals) {
                state.selectedChemicals - chemical
            } else {
                state.selectedChemicals + chemical
            }

            val newAmounts = if (chemical in state.chemicalAmounts) {
                state.chemicalAmounts - chemical
            } else {
                state.chemicalAmounts + (chemical to 1f) // Default to 1 mol when selected
            }

            state.copy(selectedChemicals = newSelected, chemicalAmounts = newAmounts)
        }
    }

    private fun validateAndStartReaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val areChemicalsCorrect = state.selectedChemicals == scenario.correctChemicals.toSet()
            val isTempCorrect = state.temperature in scenario.tempRange.start..scenario.tempRange.endInclusive
            val isPressureCorrect = state.pressure in scenario.pressureRange.start..scenario.pressureRange.endInclusive

            val isRatioCorrect = checkRatio(state)

            val result = if (areChemicalsCorrect && isRatioCorrect && isTempCorrect && isPressureCorrect) {
                ReactionResult.Success( "Tepkime Başarılı!", scenario.successMessage)
            } else {
                val errorMessage = when {
                    !areChemicalsCorrect -> scenario.failureMessages["chemicals"]!!
                    !isRatioCorrect -> scenario.failureMessages["ratio"]!!
                    !isTempCorrect -> scenario.failureMessages["temperature"]!!
                    !isPressureCorrect -> scenario.failureMessages["pressure"]!!
                    else -> "Bir şeyler ters gitti."
                }
                ReactionResult.Failure("Tepkime Başarısız", errorMessage)
            }
            _reactionResult.emit(result)
        }
    }

    private fun checkRatio(state: ScenarioUiState): Boolean {
        // Avoid division by zero and handle single chemical case
        if (scenario.correctRatio.isEmpty() && state.selectedChemicals.isNotEmpty()) return true
        if (state.selectedChemicals.size != scenario.correctChemicals.size) return false

        val referenceCorrectChemical = scenario.correctRatio.keys.firstOrNull() ?: return false
        val referenceCorrectRatio = scenario.correctRatio[referenceCorrectChemical] ?: return false
        val referenceAmount = state.chemicalAmounts[referenceCorrectChemical] ?: return false
        
        if (referenceAmount == 0f) return false

        // Check if all other chemicals have the correct ratio relative to the reference
        return scenario.correctRatio.all { (chemical, expectedRatio) ->
            val actualAmount = state.chemicalAmounts[chemical] ?: 0f
            val actualRatio = actualAmount / referenceAmount
            val expectedRatioNormalized = expectedRatio / referenceCorrectRatio
            // Allow a 10% tolerance
            actualRatio in (expectedRatioNormalized * 0.9)..(expectedRatioNormalized * 1.1)
        }
    }
}