package com.ahmetkaraaslan.labx.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Custom Colors
val LabX_Primary = Color(0xFF00586d)
val LabX_Secondary = Color(0xFF009b97)
val LabX_Button_Transparent = Color(0x33FFFFFF)
val LabX_Button_Transparent_Dark = Color(0x66FFFFFF)
val LabX_Button_Red = Color(0xFFB00020)
val LabX_Success = Color(0xFF4CAF50)
val LabX_Success_Bg = Color(0x334CAF50)
val LabX_Error = Color.Red
val LabX_Error_Bg = Color(0x33FF0000)
val LabX_White_80 = Color.White.copy(alpha = 0.8f)
val LabX_White_70 = Color.White.copy(alpha = 0.7f)
val LabX_White_50 = Color.White.copy(alpha = 0.5f)

val LabX_Background_Gradient = Brush.verticalGradient(
    colors = listOf(LabX_Primary, LabX_Secondary)
)
