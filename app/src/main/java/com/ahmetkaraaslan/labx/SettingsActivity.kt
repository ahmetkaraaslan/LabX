package com.ahmetkaraaslan.labx

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ahmetkaraaslan.labx.utils.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                SettingsScreen {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val verticalGradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFF00586d), Color(0xFF009b97)))
    
    var soundEnabled by remember { mutableStateOf(loadSoundSetting(context)) }
    var vibrationEnabled by remember { mutableStateOf(loadVibrationSetting(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(verticalGradientBrush)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SettingSwitch(title = "Uygulama Sesi", checked = soundEnabled, onCheckedChange = { 
                soundEnabled = it
                saveSoundSetting(context, it)
            })
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitch(title = "Titreşim", checked = vibrationEnabled, onCheckedChange = { 
                vibrationEnabled = it 
                saveVibrationSetting(context, it)
            })
            
            Spacer(modifier = Modifier.weight(1f))

            // Danger Zone
            Text("Veri Yönetimi", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // 1. Clear SharedPreferences
                    deleteAvatarUrl(context)

                    // 2. Clear all WebView data
                    try {
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        WebStorage.getInstance().deleteAllData()
                        val webView = WebView(context)
                        webView.clearCache(true)
                        webView.clearFormData()
                        webView.clearHistory()
                        webView.clearSslPreferences()
                    } catch (e: Exception) {
                        // Handle exceptions if necessary
                    }

                    Toast.makeText(context, "Tüm avatar verileri temizlendi!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
            ) {
                Text("Karakteri Sıfırla", color = Color.White)
            }
        }
    }
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0x33FFFFFF), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00586d),
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}
