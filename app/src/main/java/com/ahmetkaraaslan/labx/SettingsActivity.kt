package com.ahmetkaraaslan.labx

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextDecoration
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.loadSoundSetting
import com.ahmetkaraaslan.labx.utils.loadVibrationSetting
import com.ahmetkaraaslan.labx.utils.saveSoundSetting
import com.ahmetkaraaslan.labx.utils.saveVibrationSetting

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
    
    var selectedLanguage by remember { mutableStateOf("Türkçe") }
    var soundEnabled by remember { mutableStateOf(loadSoundSetting(context)) }
    var vibrationEnabled by remember { mutableStateOf(loadVibrationSetting(context)) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(verticalGradientBrush).padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0x33FFFFFF), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Uygulama Dili", color = Color.White, fontSize = 16.sp)
                Text(text = selectedLanguage, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            SettingSwitch(title = "Uygulama Sesi", checked = soundEnabled, onCheckedChange = { 
                soundEnabled = it
                saveSoundSetting(context, it)
            })
            
            SettingSwitch(title = "Titreşim", checked = vibrationEnabled, onCheckedChange = { 
                vibrationEnabled = it 
                saveVibrationSetting(context, it)
            })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { showPrivacyPolicy = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gizlilik Politikası",
                    color = Color.White,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
                Text(text = "→", color = Color.White, fontSize = 16.sp)
            }
        }
    }
    
    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Gizlilik Politikası",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00586d)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Veri Toplama ve Kullanım",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "LabX uygulaması, kullanıcı ilerlemesini (tamamlanan senaryolar ve testler) ve uygulama ayarlarını (ses ve titreşim tercihleri) sadece cihazınızda yerel olarak saklar. Bu veriler cihazınızın dışına gönderilmez ve hiçbir sunucuya aktarılmaz.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )
                
                Text(
                    text = "Toplanan Veriler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "• Tamamlanan senaryo ID'leri\n• Tamamlanan test ID'leri\n• Ses açık/kapalı ayarı\n• Titreşim açık/kapalı ayarı\n\nBu veriler sadece uygulama içi deneyiminizi kişiselleştirmek için kullanılır.",
                    fontSize = 14.sp
                )
                
                Text(
                    text = "İnternet Bağlantısı",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Uygulama herhangi bir internet bağlantısı gerektirmez ve kullanmaz. Tüm veriler cihazınızda güvenle saklanır.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )
                
                Text(
                    text = "Üçüncü Taraf Hizmetler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Uygulama herhangi bir üçüncü taraf analitik, reklam veya veri toplama servisi kullanmaz.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )
                
                Text(
                    text = "İletişim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Gizlilik politikamız hakkında sorularınız için Play Store üzerinden bize ulaşabilirsiniz.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam", color = Color(0xFF00586d))
            }
        },
        containerColor = Color.White
    )
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
