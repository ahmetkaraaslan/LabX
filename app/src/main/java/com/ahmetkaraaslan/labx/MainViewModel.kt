package com.ahmetkaraaslan.labx

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for the Main screen
 */
data class MainUiState(
    val placeholder: String = ""
)

class MainViewModel : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
}