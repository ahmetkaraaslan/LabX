package com.ahmetkaraaslan.labx

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.activity.ComponentActivity
import com.ahmetkaraaslan.labx.utils.loadAvatarUrl
import com.ahmetkaraaslan.labx.utils.saveAvatarUrl
import com.unity3d.player.UnityPlayerActivity // Unity kütüphanesinin bağlı olduğundan emin ol
import com.unity3d.player.UnityPlayer

private const val TAG = "FreeModeActivity"
private const val JS_CONSOLE_TAG = "WebViewConsole"
private const val RPM_AVATAR_CREATOR_URL = "https://demo.readyplayer.me/avatar?frameApi&clearCache=true"

class FreeModeActivity : ComponentActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Eğer zaten bir avatar URL'si varsa direkt Unity'ye git
        val existingUrl = loadAvatarUrl(this)
        if (existingUrl != null) {
            Log.d(TAG, "Avatar mevcut, Unity sahnesine geçiliyor.")
            navigateToUnity(existingUrl)
            return
        }

        // 2. WebView kurulumu
        WebView.setWebContentsDebuggingEnabled(true)
        webView = WebView(this)
        setContentView(webView)

        configureWebView()

        // 3. RPM Creator'ı yükle
        webView.loadUrl(RPM_AVATAR_CREATOR_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.apply {
            clearCache(true)
            clearHistory()
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // Güncel UserAgent 3D render için önemli
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Sayfa yüklendi: $url")
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        Log.e(TAG, "WebView Hatası: ${error?.description}")
                    }
                }
            }

            // JavaScript arayüzünü bağla
            addJavascriptInterface(WebAppInterface(), "Android")
        }
    }

    // Avatar oluşturulduğunda bu fonksiyon çalışacak
    private fun navigateToUnity(url: String) {
        // URL'yi yerel hafızaya kaydet
        saveAvatarUrl(this, url)

        // Unity'ye URL mesajını gönder (Unity arka planda açıksa yakalar)
        try {
            com.unity3d.player.UnityPlayer.UnitySendMessage("AvatarManager", "LoadNewAvatar", url)
        } catch (e: Exception) {
            Log.e(TAG, "Unity henüz hazır değil, mesaj gönderilemedi.")
        }

        // Unity Activity'sini başlat
        val intent = Intent(this, UnityPlayerActivity::class.java)
        startActivity(intent)
        finish()
    }

    // JavaScript'ten gelen mesajları dinleyen sınıf
    private inner class WebAppInterface {
        @JavascriptInterface
        fun onAvatarExported(url: String) {
            Log.d(TAG, "Avatar URL Alındı: $url")
            // UI thread üzerinde geçişi başlat
            runOnUiThread {
                navigateToUnity(url)
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}