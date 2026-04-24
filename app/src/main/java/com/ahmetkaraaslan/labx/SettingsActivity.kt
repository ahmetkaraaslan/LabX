package com.ahmetkaraaslan.labx

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.ui.theme.*
import com.ahmetkaraaslan.labx.utils.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimyasalTheme {
                SettingsScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(loadSoundSetting(context)) }
    var vibrationEnabled by remember { mutableStateOf(loadVibrationSetting(context)) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                playClickFeedback(context)
                // 1. Clear SharedPreferences for avatar URL
                deleteAvatarUrl(context)

                // 2. Clear all WebView data globally
                // No need to create a WebView instance. These methods work globally.
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()

                Toast.makeText(context, context.getString(R.string.avatar_data_cleared), Toast.LENGTH_SHORT).show()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings), color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = {
                        playClickFeedback(context)
                        onBackPressed()
                    }) { 
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.back), tint = Color.White)
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(LabX_Background_Gradient)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SettingSwitch(title = stringResource(id = R.string.app_sound), checked = soundEnabled, onCheckedChange = { 
                soundEnabled = it
                saveSoundSetting(context, it)
            })
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitch(title = stringResource(id = R.string.vibration), checked = vibrationEnabled, onCheckedChange = { 
                vibrationEnabled = it 
                saveVibrationSetting(context, it)
            })
            
            Spacer(modifier = Modifier.weight(1f))

            // Danger Zone
            Text(stringResource(id = R.string.data_management), color = LabX_White_70, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { 
                    playClickFeedback(context)
                    showResetDialog = true 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LabX_Button_Red)
            ) {
                Text(stringResource(id = R.string.reset_character), color = Color.White)
            }
        }
    }
}

@Composable
private fun ResetConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.reset_character_confirmation_title)) },
        text = { Text(stringResource(id = R.string.reset_character_confirmation_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LabX_Button_Transparent, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LabX_Primary,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}
