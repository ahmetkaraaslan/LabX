package com.ahmetkaraaslan.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme

class FreeModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                FreeModePlaceholderScreen {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeModePlaceholderScreen(onBackPressed: () -> Unit) {
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))

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
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradientBrush)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Serbest Mod yakında sizlerle!",
                color = Color.White,
                fontSize = 24.sp
            )
        }
    }
}
