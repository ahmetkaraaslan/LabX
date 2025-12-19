package com.ahmetkaraaslan.labx.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ahmetkaraaslan.labx.R
import com.ahmetkaraaslan.labx.model.Quiz
import com.ahmetkaraaslan.labx.model.Scenario
import java.io.InputStreamReader

// --- Settings Read/Write ---
fun saveVibrationSetting(context: Context, isEnabled: Boolean) {
    context.getSharedPreferences("kimyasal_settings", Context.MODE_PRIVATE).edit().putBoolean("vibration_enabled", isEnabled).apply()
}

fun loadVibrationSetting(context: Context): Boolean {
    return context.getSharedPreferences("kimyasal_settings", Context.MODE_PRIVATE).getBoolean("vibration_enabled", true)
}

fun saveSoundSetting(context: Context, isEnabled: Boolean) {
    context.getSharedPreferences("kimyasal_settings", Context.MODE_PRIVATE).edit().putBoolean("sound_enabled", isEnabled).apply()
}

fun loadSoundSetting(context: Context): Boolean {
    return context.getSharedPreferences("kimyasal_settings", Context.MODE_PRIVATE).getBoolean("sound_enabled", true)
}

// --- Data Loading ---
fun loadScenariosFromJson(context: Context): List<Scenario> {
    val inputStream = context.resources.openRawResource(R.raw.scenarios)
    val reader = InputStreamReader(inputStream)
    val scenarioType = object : TypeToken<List<Scenario>>() {}.type
    return Gson().fromJson(reader, scenarioType)
}

fun loadQuizzesFromJson(context: Context): List<Quiz> {
    val inputStream = context.resources.openRawResource(R.raw.quizzes)
    val reader = InputStreamReader(inputStream)
    val quizType = object : TypeToken<List<Quiz>>() {}.type
    return Gson().fromJson(reader, quizType)
}

// --- Progress Read/Write ---
fun saveCompletedScenarios(context: Context, completedIds: Set<Int>) {
    val sharedPref = context.getSharedPreferences("kimyasal_progress", Context.MODE_PRIVATE)
    val completedStrings = completedIds.map { "scenario_$it" }.toSet()
    with(sharedPref.edit()) {
        putStringSet("completed_scenarios", completedStrings)
        apply()
    }
}

fun loadCompletedScenarios(context: Context): Set<Int> {
    val sharedPref = context.getSharedPreferences("kimyasal_progress", Context.MODE_PRIVATE)
    val completedStrings = sharedPref.getStringSet("completed_scenarios", emptySet()) ?: emptySet()
    return completedStrings.mapNotNull { it.removePrefix("scenario_").toIntOrNull() }.toSet()
}

fun saveCompletedQuizzes(context: Context, completedIds: Set<Int>) {
    val sharedPref = context.getSharedPreferences("kimyasal_progress", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        val completedStrings = completedIds.map { "quiz_$it" }.toSet()
        putStringSet("completed_quizzes", completedStrings)
        apply()
    }
}

fun loadCompletedQuizzes(context: Context): Set<Int> {
    val sharedPref = context.getSharedPreferences("kimyasal_progress", Context.MODE_PRIVATE)
    val completedStrings = sharedPref.getStringSet("completed_quizzes", emptySet()) ?: emptySet()
    return completedStrings.mapNotNull { it.removePrefix("quiz_").toIntOrNull() }.toSet()
}

// --- Feedback (Sound/Vibration) ---
fun playSound(context: Context, soundResId: Int) {
    if (loadSoundSetting(context)) {
        MediaPlayer.create(context, soundResId).apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }
}

fun vibrate(context: Context, duration: Long) {
    if (loadVibrationSetting(context)) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}
