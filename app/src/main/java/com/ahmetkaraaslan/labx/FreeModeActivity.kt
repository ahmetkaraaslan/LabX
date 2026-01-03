package com.ahmetkaraaslan.labx

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.activity.ComponentActivity
import com.ahmetkaraaslan.labx.utils.loadAvatarUrl
import com.ahmetkaraaslan.labx.utils.saveAvatarUrl

private const val TAG = "FreeModeActivity"
private const val JS_CONSOLE_TAG = "WebViewConsole"
private const val RPM_AVATAR_CREATOR_URL = "https://demo.readyplayer.me/avatar?frameApi&clearCache=true"

class FreeModeActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If an avatar URL already exists, there is no need to create a new one.
        // The user should be directed to the Unity scene activity instead.
        // For now, we just finish this activity.
        if (loadAvatarUrl(this) != null) {
            Log.d(TAG, "Avatar already exists. Finishing FreeModeActivity.")
            // Here you would typically start your UnityPlayerActivity
            finish()
            return
        }

        // Proceed with creating a WebView to show the avatar creator
        WebView.setWebContentsDebuggingEnabled(true)
        webView = WebView(this)
        setContentView(webView)

        // Apply all the necessary settings we've discovered
        configureWebView()

        // Load the URL from the constant, which is the most reliable method
        webView.loadUrl(RPM_AVATAR_CREATOR_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.apply {
            // Nuke all data to ensure a clean session, preventing RPM from redirecting.
            clearCache(true)
            clearHistory()
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            // Enable Hardware Acceleration for better performance
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                 override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page finished loading: $url")
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        Log.e(TAG, "WebView Error: Code: ${error?.errorCode} Description: ${error?.description} URL: ${request.url}")
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    Log.d(TAG, "onPermissionRequest for: ${request.origin} - Resources: ${request.resources.joinToString()}")
                    request.grant(request.resources)
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    val message = "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}"
                    when (consoleMessage.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> Log.e(JS_CONSOLE_TAG, message)
                        ConsoleMessage.MessageLevel.WARNING -> Log.w(JS_CONSOLE_TAG, message)
                        else -> Log.i(JS_CONSOLE_TAG, message)
                    }
                    return true
                }
            }

            addJavascriptInterface(WebAppInterface(), "Android")
        }
    }

    // The interface that will receive the URL from JavaScript
    private inner class WebAppInterface {
        @JavascriptInterface
        fun onAvatarExported(url: String) {
            Log.d(TAG, "Avatar exported from JS: $url")
            // This is called from a background thread. Switch to the main thread to finish the activity.
            runOnUiThread {
                saveAvatarUrl(this@FreeModeActivity, url)
                // Now that we have the URL, we can close the creator.
                // The main navigation logic will then direct the user to the Unity scene.
                finish()
            }
        }
    }

    // Make sure the WebView can go back
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
