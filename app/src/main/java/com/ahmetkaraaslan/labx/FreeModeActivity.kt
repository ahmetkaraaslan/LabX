package com.ahmetkaraaslan.labx

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.ahmetkaraaslan.labx.utils.loadAvatarUrl
import com.ahmetkaraaslan.labx.utils.saveAvatarUrl

class FreeModeActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val avatarUrl = loadAvatarUrl(this)

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // 🔑 JavaScript → Android köprüsü
        webView.addJavascriptInterface(object {

            @JavascriptInterface
            fun onAvatarCreated(url: String) {
                runOnUiThread {
                    saveAvatarUrl(this@FreeModeActivity, url)

                    // Avatar oluşturuldu → Lab ortamına geç
                    webView.loadUrl("https://labx.readyplayer.me/avatar?lang=tr&id=6955654794a2c8f39a076cf3")
                }
            }

        }, "Android")

        if (avatarUrl == null) {
            // 🧍‍♂️ Avatar yok → Creator aç
            webView.loadUrl("https://demo.readyplayer.me/avatar?frameApi")
        } else {
            // 🧪 Avatar var → Lab aç
            webView.loadUrl("https://your-lab-environment-url-here")
        }
    }
}
