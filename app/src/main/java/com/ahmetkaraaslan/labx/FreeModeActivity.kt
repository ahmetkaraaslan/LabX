package com.ahmetkaraaslan.labx

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.view.ViewGroup
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.unity3d.player.UnityPlayerActivity
import androidx.compose.ui.viewinterop.AndroidView

class FreeModeActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var isLaunching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KimyasalTheme {
                ReadyPlayerMeScreen(
                    onWebViewCreated = { view -> webView = view },
                    activity = this@FreeModeActivity
                )
            }
        }
    }

    fun launchUnityWithAvatar(avatarUrl: String) {
        if (isLaunching) return
        isLaunching = true

        android.util.Log.d("FreeMode", "🎯 YAKALANAN URL: $avatarUrl")

        try {
            val intent = Intent(this, UnityPlayerActivity::class.java)
            intent.putExtra("avatarUrl", avatarUrl)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000)
        } catch (e: Exception) {
            isLaunching = false
            android.util.Log.e("FreeMode", "❌ Unity hatası: ${e.message}")
        }
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReadyPlayerMeScreen(
    onWebViewCreated: (WebView) -> Unit,
    activity: FreeModeActivity
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                addJavascriptInterface(WebAppInterface(activity), "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Sayfa her yüklendiğinde ve değişimde scripti tekrar enjekte et
                        view?.injectReadyPlayerMeScript()
                    }
                }

                webChromeClient = WebChromeClient()

                // Demo URL yerine direkt oluşturma URL'sini kullanmak daha stabil olabilir
                loadUrl("https://demo.readyplayer.me/avatar")
                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// KRİTİK DÜZELTME: JavaScript Dinleyicisi
private fun WebView.injectReadyPlayerMeScript() {
    val script = """
        (function() {
            console.log('RPM Dinleyici Baslatildi...');
            
            function handleMessage(event) {
                try {
                    const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                    
                    // 1. Durum: Modern v1 export mesajı
                    if (data.type === 'v1.avatar.exported' && data.data && data.data.url) {
                        Android.onAvatarCreated(data.data.url);
                    }
                    // 2. Durum: Klasik URL mesajı
                    else if (typeof event.data === 'string' && event.data.includes('.glb')) {
                        Android.onAvatarCreated(event.data);
                    }
                } catch (e) {
                    // JSON olmayan verilerde hata almamak için
                    if (typeof event.data === 'string' && event.data.includes('.glb')) {
                        Android.onAvatarCreated(event.data);
                    }
                }
            }

            // Mevcut dinleyicileri temizle ve yenisini ekle
            window.removeEventListener('message', handleMessage);
            window.addEventListener('message', handleMessage);
        })();
    """.trimIndent()
    evaluateJavascript(script, null)
}

class WebAppInterface(private val activity: FreeModeActivity) {
    @JavascriptInterface
    fun onAvatarCreated(avatarUrl: String) {
        Handler(Looper.getMainLooper()).post {
            activity.launchUnityWithAvatar(avatarUrl)
        }
    }
}