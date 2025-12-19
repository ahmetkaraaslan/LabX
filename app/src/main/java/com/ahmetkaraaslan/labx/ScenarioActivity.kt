package com.ahmetkaraaslan.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.model.Scenario
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.*

class ScenarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                ScenarioNavigator()
            }
        }
    }
}

@Composable
fun ScenarioNavigator() {
    val context = LocalContext.current
    val allScenarios = remember { loadScenariosFromJson(context) }
    var completedScenarioIds by remember { mutableStateOf(loadCompletedScenarios(context)) }
    var currentScenario by remember { mutableStateOf<Scenario?>(null) }

    val onScenarioComplete: (Int) -> Unit = { scenarioId ->
        val newCompletedIds = completedScenarioIds + scenarioId
        saveCompletedScenarios(context, newCompletedIds)
        completedScenarioIds = newCompletedIds
        currentScenario = null
    }

    if (currentScenario == null) {
        ScenarioSelectionScreen(
            scenarios = allScenarios,
            completedIds = completedScenarioIds,
            onScenarioSelected = { scenario ->
                vibrate(context, 50)
                playSound(context, R.raw.click_sound)
                currentScenario = scenario
            },
            onBackPressed = { (context as? ComponentActivity)?.finish() }
        )
    } else {
        ScenarioExperimentScreen(
            scenario = currentScenario!!,
            onComplete = onScenarioComplete,
            onBackPressed = {
                vibrate(context, 50)
                playSound(context, R.raw.click_sound)
                currentScenario = null
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
    val context = LocalContext.current
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Senaryo Seçimi", color = Color.White) },
                navigationIcon = { IconButton(onClick = {
                    vibrate(context, 50)
                    playSound(context, R.raw.click_sound)
                    onBackPressed()
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(verticalGradientBrush).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scenarios, key = { it.id }) { scenario ->
                val isUnlocked = scenario.id == 1 || (scenario.id - 1) in completedIds
                val alpha = if (isUnlocked) 1f else 0.6f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
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
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Tamamlandı", tint = Color(0xFF4CAF50))
                        }
                        !isUnlocked -> {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Kilitli", tint = Color.White, modifier = Modifier.size(20.dp))
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
    onComplete: (Int) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))
    var temperature by remember { mutableStateOf(25f) }
    var pressure by remember { mutableStateOf(1f) }
    var selectedChemicals by remember { mutableStateOf(setOf<String>()) }
    var chemicalAmounts by remember { mutableStateOf(mapOf<String, Float>()) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var showInputDialogFor by remember { mutableStateOf<String?>(null) }

    val shuffledChemicals = remember(scenario.id) { scenario.allChemicals.shuffled() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scenario.title, color = Color.White, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(verticalGradientBrush).padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = scenario.description, color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
            
            Text("Gerekli Kimyasallar:", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                shuffledChemicals.chunked(3).forEach { rowChemicals ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                       rowChemicals.forEach { chemical ->
                            ChemicalButton(
                                modifier = Modifier.weight(1f),
                                chemical = chemical,
                                selectedChemicals = selectedChemicals,
                                chemicalAmounts = chemicalAmounts,
                                onSelect = { s, a -> selectedChemicals = s; chemicalAmounts = a }
                            )
                        }
                        repeat(3 - rowChemicals.size) {
                            Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                        }
                    }
                }
            }

            if (selectedChemicals.isNotEmpty()) {
                Text("Madde Miktarları (mol):", color = Color.White, fontSize = 18.sp)
                selectedChemicals.forEach { chemical ->
                    val amount = chemicalAmounts[chemical] ?: 0f
                    Text(text = "$chemical Miktarı: ${String.format("%.2f", amount)} mol", color = Color.White, modifier = Modifier.clickable {
                        vibrate(context, 50)
                        playSound(context, R.raw.click_sound)
                        showInputDialogFor = chemical
                    })
                    Slider(value = amount, onValueChange = { newAmount -> chemicalAmounts = chemicalAmounts + (chemical to newAmount) }, valueRange = 0f..5f)
                }
            }

            Text(text = "Sıcaklık: ${temperature.toInt()} °C", color = Color.White, modifier = Modifier.clickable {
                vibrate(context, 50)
                playSound(context, R.raw.click_sound)
                showInputDialogFor = "temperature"
            })
            Slider(value = temperature, onValueChange = { temperature = it }, valueRange = 0f..1000f)
            Text(text = "Basınç: ${pressure.toInt()} atm", color = Color.White, modifier = Modifier.clickable {
                vibrate(context, 50)
                playSound(context, R.raw.click_sound)
                showInputDialogFor = "pressure"
            })
            Slider(value = pressure, onValueChange = { pressure = it }, valueRange = 0f..500f)
            Spacer(modifier = Modifier.weight(1f, fill = false))

            Button(
                onClick = {
                    vibrate(context, 50)
                    playSound(context, R.raw.click_sound)

                    val areChemicalsCorrect = selectedChemicals == scenario.correctChemicals

                    var isRatioCorrect = selectedChemicals.size == 1 && scenario.correctRatio.size == 1
                    if (scenario.correctRatio.size > 1) {
                        val mainCorrectChemical = scenario.correctRatio.keys.first()
                        val n2Amount = chemicalAmounts[mainCorrectChemical] ?: 0f
                        isRatioCorrect = scenario.correctRatio.all { (key, ratio) ->
                            val amount = chemicalAmounts[key] ?: 0f
                            if (n2Amount == 0f) false
                            else {
                                val actualRatio = amount / n2Amount
                                val expectedRatio = ratio / scenario.correctRatio.values.first()
                                actualRatio in (expectedRatio * 0.9)..(expectedRatio * 1.1)
                            }
                        }
                    }

                    val isTempCorrect = temperature in scenario.tempRange.start..scenario.tempRange.endInclusive
                    val isPressureCorrect = pressure in scenario.pressureRange.start..scenario.pressureRange.endInclusive

                    if (areChemicalsCorrect && isRatioCorrect && isTempCorrect && isPressureCorrect) {
                        dialogTitle = "Tepkime Başarılı!"
                        dialogMessage = scenario.successMessage
                        vibrate(context, 1000) 
                        playSound(context, R.raw.success_sound) 
                    } else {
                        dialogTitle = "Tepkime Başarısız"
                        vibrate(context, 2000)
                        playSound(context, R.raw.error_sound)
                        dialogMessage = when {
                            !areChemicalsCorrect -> scenario.failureMessages["chemicals"]!!
                            !isRatioCorrect -> scenario.failureMessages["ratio"]!!
                            !isTempCorrect -> scenario.failureMessages["temperature"]!!
                            !isPressureCorrect -> scenario.failureMessages["pressure"]!!
                            else -> "Bir şeyler ters gitti."
                        }
                    }
                    showResultDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xAAFFFFFF)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Tepkimeyi Başlat", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (showInputDialogFor != null) {
            val editingKey = showInputDialogFor!!
            val isDecimal = editingKey !in listOf("temperature", "pressure")
            InputDialog(
                title = when (editingKey) {
                    "temperature" -> "Sıcaklık Girin"
                    "pressure" -> "Basınç Girin"
                    else -> "$editingKey Miktarı Girin"
                },
                label = when (editingKey) {
                    "temperature" -> "Sıcaklık (°C)"
                    "pressure" -> "Basınç (atm)"
                    else -> "Miktar (mol)"
                },
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                onDismiss = { showInputDialogFor = null },
                onConfirm = {
                    val floatValue = it.toFloatOrNull()
                    if (floatValue != null) {
                        when (editingKey) {
                            "temperature" -> temperature = floatValue
                            "pressure" -> pressure = floatValue
                            else -> chemicalAmounts = chemicalAmounts + (editingKey to floatValue)
                        }
                    }
                    showInputDialogFor = null
                }
            )
        }

        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                title = { Text(dialogTitle) },
                text = { Text(dialogMessage) },
                confirmButton = {
                    Button(onClick = {
                        if (dialogTitle == "Tepkime Başarılı!") {
                            onComplete(scenario.id)
                        }
                        showResultDialog = false
                    }) { Text("Tamam") }
                }
            )
        }
    }
}

@Composable
fun ChemicalButton(
    modifier: Modifier = Modifier,
    chemical: String,
    selectedChemicals: Set<String>,
    chemicalAmounts: Map<String, Float>,
    onSelect: (Set<String>, Map<String, Float>) -> Unit
) {
    val context = LocalContext.current
    val isSelected = chemical in selectedChemicals
    Button(
        modifier = modifier.padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = {
            vibrate(context, 50)
            playSound(context, R.raw.click_sound)
            val newSelected = if (isSelected) selectedChemicals - chemical else selectedChemicals + chemical
            val newAmounts = if (isSelected) chemicalAmounts - chemical else chemicalAmounts + (chemical to 0f)
            onSelect(newSelected, newAmounts)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color(0x33FFFFFF),
            contentColor = if (isSelected) Color(0xFF00586d) else Color.White
        )
    ) { Text(chemical) }
}

@Composable
fun InputDialog(title: String, label: String, keyboardType: KeyboardType, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onConfirm(text) })) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Tamam") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
