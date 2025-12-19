package com.ahmetkaraaslan.labx

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.playSound
import com.ahmetkaraaslan.labx.utils.vibrate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val verticalGradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF00586d), Color(0xFF009b97))
    )
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { 
                        vibrate(context, 50)
                        playSound(context, R.raw.click_sound)
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradientBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_illustration),
                    contentDescription = "Kimya İllüstrasyonu",
                    modifier = Modifier.height(300.dp),
                    contentScale = ContentScale.Fit
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_scenario),
                        title = "Senaryo Modu",
                        subtitle = "Kimyasal tepkimeleri öğren"
                    ) {
                        vibrate(context, 50)
                        playSound(context, R.raw.click_sound)
                        context.startActivity(Intent(context, ScenarioActivity::class.java))
                    }
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_sandbox),
                        title = "Serbest Mod",
                        subtitle = "Kimyasallarla özgürce deney yap"
                    ) {
                         vibrate(context, 50)
                         playSound(context, R.raw.click_sound)
                         context.startActivity(Intent(context, FreeModeActivity::class.java))
                    }
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_quiz),
                        title = "Test Modu",
                        subtitle = "Bilgini test et"
                    ) {
                         vibrate(context, 50)
                         playSound(context, R.raw.click_sound)
                         context.startActivity(Intent(context, TestActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(icon: Painter, title: String, subtitle: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text(text = subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    KimyasalTheme {
        MainScreen()
    }
}
