package com.ahmetkaraaslan.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmetkaraaslan.labx.model.Scenario
import com.ahmetkaraaslan.labx.ui.theme.*
import com.ahmetkaraaslan.labx.utils.*
import kotlinx.coroutines.flow.collectLatest

// ViewModel Factory to pass the scenario to the ViewModel
class ScenarioViewModelFactory(private val scenario: Scenario) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScenarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScenarioViewModel(scenario) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ScenarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                ScenarioNavigator { finish() }
            }
        }
    }
}

@Composable
fun ScenarioNavigator(onFinishActivity: () -> Unit) {
    val context = LocalContext.current
    // Explicit type added to fix inference
    val allScenarios: List<Scenario> = remember { loadScenariosFromJson(context) }
    var completedScenarioIds by remember { mutableStateOf<Set<Int>>(loadCompletedScenarios(context)) }
    var currentScenario by remember { mutableStateOf<Scenario?>(null) }

    val onScenarioComplete: (Int) -> Unit = { scenarioId ->
        val newCompletedIds = completedScenarioIds + scenarioId
        saveCompletedScenarios(context, newCompletedIds)
        completedScenarioIds = newCompletedIds
        currentScenario = null // Go back to selection screen
    }

    val selectedScenario = currentScenario

    if (selectedScenario == null) {
        ScenarioSelectionScreen(
            scenarios = allScenarios,
            completedIds = completedScenarioIds,
            onScenarioSelected = { scenario ->
                playClickFeedback(context)
                currentScenario = scenario
            },
            onBackPressed = {
                playClickFeedback(context)
                onFinishActivity()
            }
        )
    } else {
        val viewModel: ScenarioViewModel = viewModel(
            key = "scenario_${selectedScenario.id}",
            factory = ScenarioViewModelFactory(selectedScenario)
        )
        ScenarioExperimentScreen(
            scenario = selectedScenario,
            viewModel = viewModel,
            onComplete = { scenarioId ->
                onScenarioComplete(scenarioId)
            },
            onBackPressed = {
                playClickFeedback(context)
                currentScenario = null // Go back to selection screen
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioSelectionScreen(
    scenarios: List<Scenario>,
    completedIds: Set<Int>,
    onScenarioSelected: (Scenario) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.scenario_selection), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LabX_Background_Gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scenarios, key = { it.id }) { scenario ->
                val isUnlocked = scenario.id == 1 || (scenario.id - 1) in completedIds
                val alpha = if (isUnlocked) 1f else 0.6f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .background(LabX_Button_Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = isUnlocked) { onScenarioSelected(scenario) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scenario.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    when {
                        scenario.id in completedIds -> {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = stringResource(id = R.string.completed), tint = LabX_Success)
                        }
                        !isUnlocked -> {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = stringResource(id = R.string.locked), tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioExperimentScreen(
    scenario: Scenario,
    viewModel: ScenarioViewModel,
    onComplete: (Int) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showResultDialog by remember { mutableStateOf<ReactionResult?>(null) }
    var showInputDialogFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.reactionResult.collectLatest { result ->
            when(result) {
                is ReactionResult.Success -> playSuccessFeedback(context)
                is ReactionResult.Failure -> playErrorFeedback(context)
            }
            showResultDialog = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scenario.title, color = Color.White, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.Default.ArrowBack, stringResource(id = R.string.back), tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LabX_Background_Gradient)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = scenario.description, color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)

            Text(stringResource(id = R.string.required_chemicals), color = Color.White, fontSize = 18.sp)

            // Chemical Buttons
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.shuffledChemicals.chunked(3).forEach { rowChemicals ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowChemicals.forEach { chemical ->
                            ChemicalButton(
                                modifier = Modifier.weight(1f),
                                chemical = chemical,
                                isSelected = chemical in uiState.selectedChemicals,
                                onClick = {
                                    playClickFeedback(context)
                                    viewModel.onEvent(ScenarioEvent.ChemicalSelected(chemical))
                                }
                            )
                        }
                        repeat(3 - rowChemicals.size) {
                            Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                        }
                    }
                }
            }

            if (uiState.selectedChemicals.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.substance_amounts_mol),
                    color = Color.White,
                    fontSize = 18.sp
                )
                uiState.selectedChemicals.forEach { chemical ->
                    val amount = uiState.chemicalAmounts[chemical] ?: 0f
                    val textLabel = stringResource(id = R.string.amount_mol)
                    Text(text = "$chemical: ${String.format("%.2f", amount)} $textLabel", color = Color.White, modifier = Modifier.clickable {
                        playClickFeedback(context)
                        showInputDialogFor = chemical
                    })
                    Slider(
                        value = amount,
                        onValueChange = { newAmount -> viewModel.onEvent(ScenarioEvent.ChemicalAmountChanged(chemical, newAmount)) },
                        valueRange = 0f..5f
                    )
                }
            }

            val tempLabel = stringResource(id = R.string.temperature)
            Text(text = "$tempLabel: ${uiState.temperature.toInt()} °C", color = Color.White, modifier = Modifier.clickable {
                playClickFeedback(context)
                showInputDialogFor = "temperature"
            })
            Slider(value = uiState.temperature, onValueChange = { viewModel.onEvent(ScenarioEvent.TemperatureChanged(it)) }, valueRange = 0f..1000f)

            val pressureLabel = stringResource(id = R.string.pressure)
            Text(text = "$pressureLabel: ${uiState.pressure.toInt()} atm", color = Color.White, modifier = Modifier.clickable {
                playClickFeedback(context)
                showInputDialogFor = "pressure"
            })
            Slider(value = uiState.pressure, onValueChange = { viewModel.onEvent(ScenarioEvent.PressureChanged(it)) }, valueRange = 0f..500f)

            Spacer(modifier = Modifier.weight(1f, fill = false))

            AnimatedVisibility(visible = uiState.showReactionEquation) {
                Text(
                    text = scenario.reactionEquation,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.onEvent(ScenarioEvent.ToggleReactionEquation) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LabX_Button_Transparent_Dark)
            ) {
                Text(
                    text = stringResource(if (uiState.showReactionEquation) R.string.hide_formula else R.string.show_formula),
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    playClickFeedback(context)
                    viewModel.onEvent(ScenarioEvent.StartReaction)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(stringResource(id = R.string.start_reaction), color = LabX_Primary, fontWeight = FontWeight.Bold)
            }
        }

        // --- Dialogs ---

        showInputDialogFor?.let { editingKey ->
            val isDecimal = editingKey !in listOf("temperature", "pressure")
            InputDialog(
                title = when (editingKey) {
                    "temperature" -> stringResource(R.string.enter_temperature)
                    "pressure" -> stringResource(R.string.enter_pressure)
                    else -> stringResource(R.string.enter_amount)
                },
                label = when (editingKey) {
                    "temperature" -> stringResource(R.string.temperature_celsius)
                    "pressure" -> stringResource(R.string.pressure_atm)
                    else -> stringResource(R.string.amount_mol)
                },
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                onDismiss = { showInputDialogFor = null },
                onConfirm = {
                    val floatValue = it.toFloatOrNull()
                    if (floatValue != null) {
                        when (editingKey) {
                            "temperature" -> viewModel.onEvent(ScenarioEvent.TemperatureChanged(floatValue))
                            "pressure" -> viewModel.onEvent(ScenarioEvent.PressureChanged(floatValue))
                            else -> viewModel.onEvent(ScenarioEvent.ChemicalAmountChanged(editingKey, floatValue))
                        }
                    }
                    showInputDialogFor = null
                }
            )
        }

        showResultDialog?.let { result ->
            AlertDialog(
                onDismissRequest = { showResultDialog = null },
                title = { Text(result.title) },
                text = { Text(result.message) },
                confirmButton = {
                    Button(onClick = {
                        if (result is ReactionResult.Success) {
                            onComplete(scenario.id)
                        }
                        showResultDialog = null
                    }) { Text(stringResource(id = R.string.ok)) }
                }
            )
        }
    }
}

@Composable
fun ChemicalButton(
    modifier: Modifier = Modifier,
    chemical: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else LabX_Button_Transparent,
            contentColor = if (isSelected) LabX_Primary else Color.White
        )
    ) { Text(chemical) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(
    title: String,
    label: String,
    keyboardType: KeyboardType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm(text) })
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text(stringResource(id = R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) } }
    )
}
