package com.ahmetkaraaslan.labx

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmetkaraaslan.labx.ui.theme.*
import com.ahmetkaraaslan.labx.utils.playClickFeedback

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

private fun <T : ComponentActivity> navigateTo(context: Context, destination: Class<T>) {
    playClickFeedback(context)
    context.startActivity(Intent(context, destination))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { navigateTo(context, SettingsActivity::class.java) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
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
                .background(LabX_Background_Gradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_illustration),
                    contentDescription = null,
                    modifier = Modifier.height(300.dp),
                    contentScale = ContentScale.Fit
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_scenario),
                        title = stringResource(id = R.string.scenario_mode),
                        subtitle = stringResource(id = R.string.scenario_mode_subtitle),
                        onClick = { navigateTo(context, ScenarioActivity::class.java) }
                    )
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_sandbox),
                        title = stringResource(id = R.string.free_mode),
                        subtitle = stringResource(id = R.string.free_mode_subtitle),
                        onClick = { navigateTo(context, FreeModeActivity::class.java) }
                    )
                    ModeButton(
                        icon = painterResource(id = R.drawable.ic_quiz),
                        title = stringResource(id = R.string.test_mode),
                        subtitle = stringResource(id = R.string.test_mode_subtitle),
                        onClick = { navigateTo(context, TestActivity::class.java) }
                    )
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
        colors = ButtonDefaults.buttonColors(containerColor = LabX_Button_Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text(text = subtitle, fontSize = 14.sp, color = LabX_White_80)
            }
        }
    }
}
