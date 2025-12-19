package com.ahmetkaraaslan.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.GeminiService
import com.ahmetkaraaslan.labx.utils.playSound
import com.ahmetkaraaslan.labx.utils.vibrate
import kotlinx.coroutines.launch

class FreeModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                FreeModeScreen {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeModeScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))
    
    var userQuery by remember { mutableStateOf("") }
    var assistantResponse by remember { mutableStateOf("Merhaba! Ben senin kimya asistanınım. Yapmak veya öğrenmek istediğin her şeyi bana sorabilirsin.") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serbest Mod", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(verticalGradientBrush).padding(padding).padding(16.dp),
        ) {
            // Assistant Response Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { Text(text = assistantResponse, color = Color.White, fontSize = 18.sp) }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // User Input Area
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Örn: Hidrojen ve oksijen birleşirse ne olur?", color = Color.White.copy(alpha = 0.7f)) },
                trailingIcon = {
                    IconButton(onClick = {
                        if(userQuery.isNotBlank() && !isLoading) {
                            vibrate(context, 50)
                            playSound(context, R.raw.click_sound)
                            coroutineScope.launch {
                                isLoading = true
                                val prompt = "Sen bir kimya laboratuvarı asistanısın. Cevapların her zaman kısa, eğitici ve bir öğrenciye uygun olsun. Soru: $userQuery"
                                assistantResponse = GeminiService.getResponse(prompt)
                                isLoading = false
                            }
                            userQuery = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
        }
    }
}
