package com.ahmetkaraaslan.labx

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.utils.loadAvatarUrl
import com.ahmetkaraaslan.labx.utils.saveAvatarUrl
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class FreeModeActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var isUnityLaunched = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) Log.d("Avaturn", "Kamera izni verildi.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val savedAvatarUrl = loadAvatarUrl(this)

        // Eğer daha önce kaydedilmiş bir avatar varsa direkt Unity'yi başlat
        if (savedAvatarUrl != null && !isUnityLaunched) {
            isUnityLaunched = true
            lifecycleScope.launch {
                try {
                    val file = downloadGlbToCache(savedAvatarUrl)
                    launchUnity(file, savedAvatarUrl)
                } catch (e: Exception) {
                    Log.e("AvaturnErr", "Kayıtlı avatar yüklenemedi, webview açılıyor", e)
                    showAvaturnScreen()
                }
            }
        } else {
            showAvaturnScreen()
        }
    }

    private fun showAvaturnScreen() {
        checkCameraPermission()
        setContent {
            KimyasalTheme {
                AvaturnScreen(
                    onWebViewCreated = { view -> webView = view },
                    activity = this@FreeModeActivity
                )
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private suspend fun downloadGlbToCache(url: String): File =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().build()
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { res ->
                val bytes = res.body?.bytes() ?: error("Download failed")
                val out = File(cacheDir, "avatar.glb")
                out.writeBytes(bytes)
                out
            }
        }

    private suspend fun saveBase64ToCache(base64Data: String): File =
        withContext(Dispatchers.IO) {
            val cleanData = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
            val bytes = Base64.decode(cleanData, Base64.DEFAULT)
            val out = File(cacheDir, "avatar.glb")
            out.writeBytes(bytes)
            out
        }

    private fun launchUnity(localFile: File, originalUrl: String) {
        saveAvatarUrl(this, originalUrl)

        val intent = Intent(this, UnityPlayerActivity::class.java).apply {
            putExtra("avatarLocalPath", localFile.absolutePath)
            // Activity'yi öne getir, yeni süreç başlatma (çakışmayı önler)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)

        // Unity'nin sahneyi (Laboratory) yüklemesi için 3 saniye bekleme süresi
        Handler(Looper.getMainLooper()).postDelayed({
            val filePath = "file://" + localFile.absolutePath
            Log.d("UnityBridge", "Unity'ye gönderilen yol: $filePath")

            // Unity tarafındaki C# kodundaki fonksiyon adının "LoadAvatar" olduğundan emin ol
            UnityPlayer.UnitySendMessage("AvatarManager", "LoadAvatar", filePath)
        }, 3000)

        // KRİTİK DÜZELTME: finish() satırı kaldırıldı.
        // Unity açıkken bu Activity arka planda yaşamalı, aksi halde Unity süreci sonlanır.
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun AvaturnScreen(onWebViewCreated: (WebView) -> Unit, activity: FreeModeActivity) {
        val avaturnUrl = activity.getString(R.string.avaturn_url)

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                    }

                    val allowedOrigins = setOf(
                        "https://labx.avaturn.dev",
                        "https://hub.avaturn.me",
                        "https://demo.avaturn.dev",
                        "https://avaturn.me"
                    )

                    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                        WebViewCompat.addWebMessageListener(this, "native_app", allowedOrigins) { _, message, _, _, _ ->
                            try {
                                val dataStr = message.data
                                val obj = JSONObject(dataStr!!)
                                val eventName = obj.optString("eventName")

                                if (eventName.contains("avatar.exported")) {
                                    val dataObj = obj.getJSONObject("data")
                                    val avatarData = dataObj.optString("url", dataObj.optString("blobURI"))

                                    if (avatarData.isNotEmpty()) {
                                        lifecycleScope.launch {
                                            val file = if (avatarData.startsWith("http")) {
                                                downloadGlbToCache(avatarData)
                                            } else {
                                                saveBase64ToCache(avatarData)
                                            }
                                            launchUnity(file, avatarData)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AvaturnErr", "Mesaj işleme hatası", e)
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            view.evaluateJavascript("window.avaturnFirebaseUseSignInWithRedirect = true;", null)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest) {
                            activity.runOnUiThread { request.grant(request.resources) }
                        }
                    }

                    loadUrl(avaturnUrl)
                    onWebViewCreated(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}