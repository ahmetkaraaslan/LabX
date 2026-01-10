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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import kotlinx.coroutines.delay
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.unity3d.player.UnityPlayerActivity

class FreeModeActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var avatarUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KimyasalTheme {
                ReadyPlayerMeScreen(
                    onWebViewCreated = { view -> webView = view },
                    onBackPressed = { finish() },
                    onAvatarCreated = { url ->
                        android.util.Log.d("FreeMode", "🎯 Avatar URL received in Compose: $url")
                        avatarUrl = url
                        // Not: Unity launch is handled directly in WebAppInterface now
                    },
                    activity = this@FreeModeActivity
                )
            }
        }
    }

    fun launchUnityWithAvatar(avatarUrl: String) {
        android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Method called with URL: $avatarUrl")
        android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Thread: ${Thread.currentThread().name}")
        android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Activity: $this")

        try {
            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 1: Creating Intent...")
            val intent = Intent(this, UnityPlayerActivity::class.java)

            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 2: Setting action...")
            intent.action = "com.labx.UNITY_LAUNCH" // Manifest'te tanımlı action

            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 3: Adding extra...")
            intent.putExtra("avatarUrl", avatarUrl)

            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 4: Adding flags...")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

            android.util.Log.d("FreeMode", "✅ [launchUnityWithAvatar] Intent created successfully: $intent")
            android.util.Log.d("FreeMode", "✅ [launchUnityWithAvatar] Intent extras: ${intent.extras}")

            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 5: Starting activity...")
            startActivity(intent)

            android.util.Log.d("FreeMode", "✅ [launchUnityWithAvatar] Unity Activity started successfully!")

            android.util.Log.d("FreeMode", "🚀 [launchUnityWithAvatar] Step 6: Finishing current activity...")
            finish() // WebView Activity'sini kapat

            android.util.Log.d("FreeMode", "✅ [launchUnityWithAvatar] Current activity finished!")
        } catch (e: Exception) {
            android.util.Log.e("FreeMode", "❌ [launchUnityWithAvatar] Error launching Unity: ${e.message}", e)
            android.util.Log.e("FreeMode", "❌ [launchUnityWithAvatar] Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                finish()
            }
        } ?: finish()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        webView?.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()
    }

    override fun onDestroy() {
        webView?.let {
            it.stopLoading()
            it.clearHistory()
            it.clearCache(true)
            it.loadUrl("about:blank")
            it.onPause()
            it.removeAllViews()
            (it.parent as? ViewGroup)?.removeView(it)
            it.destroy()
        }
        webView = null
        super.onDestroy()
    }
}

@Composable
fun ReadyPlayerMeScreen(
    onWebViewCreated: (WebView) -> Unit,
    onBackPressed: () -> Unit,
    onAvatarCreated: (String) -> Unit,
    activity: ComponentActivity
) {
    val context = LocalContext.current
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    var isWebViewReady by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                settings.databaseEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.blockNetworkLoads = false
                settings.blockNetworkImage = false
                settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

                // Ready Player Me için JavaScript interface
                // Activity referansını doğrudan kullan - callback chain'i kaldır
                addJavascriptInterface(WebAppInterface(activity as FreeModeActivity) { url ->
                    android.util.Log.d("FreeMode", "✅ Avatar URL received from JS interface callback: $url")
                    // Compose callback'i çağır (logging için)
                    Handler(Looper.getMainLooper()).post {
                        try {
                            onAvatarCreated(url)
                            android.util.Log.d("FreeMode", "✅ Compose callback executed successfully")
                        } catch (e: Exception) {
                            android.util.Log.e("FreeMode", "❌ Error in Compose callback: ${e.message}", e)
                            e.printStackTrace()
                        }
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        android.util.Log.d("FreeMode", "🌐 Page started: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        android.util.Log.d("FreeMode", "✅ Page finished: $url")
                        view?.let { webView ->
                            // Ready Player Me demo URL'i için event listener'ı inject et
                            webView.injectReadyPlayerMeScript()
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            android.util.Log.e("FreeMode", "❌ Main frame error: ${error?.description}")
                            android.util.Log.e("FreeMode", "❌ Error code: ${error?.errorCode}")
                            android.util.Log.e("FreeMode", "❌ Failed URL: ${request?.url}")
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        errorResponse: android.webkit.WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        if (request?.isForMainFrame == true) {
                            android.util.Log.e("FreeMode", "❌ HTTP error: ${errorResponse?.statusCode} - ${request.url}")
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        android.util.Log.d("FreeMode", "🔗 URL loading: $url")

                        // GLB URL'yi yakala
                        if (url.endsWith(".glb", ignoreCase = true)) {
                            android.util.Log.d("FreeMode", "✅ GLB URL detected in shouldOverrideUrlLoading: $url")
                            // Main thread'de callback'i çağır (Activity context'inde)
                            Handler(Looper.getMainLooper()).post {
                                try {
                                    onAvatarCreated(url)
                                    // Unity launch is handled in WebAppInterface, but also handle it here for URL override
                                    if (activity is FreeModeActivity) {
                                        activity.launchUnityWithAvatar(url)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("FreeMode", "❌ Error in URL callback: ${e.message}", e)
                                    e.printStackTrace()
                                }
                            }
                            return true
                        }

                        // HTTPS/HTTP URL'lere izin ver
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            return false // WebView'in normal yüklemesine izin ver
                        }

                        return false
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: android.webkit.SslErrorHandler?,
                        error: android.net.http.SslError?
                    ) {
                        android.util.Log.e("FreeMode", "⚠️ SSL Error: ${error?.toString()}")
                        handler?.proceed() // SSL hatalarını yoksay (sadece test için)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        val level = when(consoleMessage?.messageLevel()) {
                            android.webkit.ConsoleMessage.MessageLevel.ERROR -> "❌"
                            android.webkit.ConsoleMessage.MessageLevel.WARNING -> "⚠️"
                            else -> "ℹ️"
                        }
                        android.util.Log.d("FreeMode", "$level Console: ${consoleMessage?.message()}")
                        return true
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        android.util.Log.d("FreeMode", "📊 Progress: $newProgress%")
                    }
                }

                // WebView referansını sakla
                webViewState.value = this
                onWebViewCreated(this)
                isWebViewReady = true
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // WebView güncellendiğinde
            if (webViewState.value == null) {
                webViewState.value = view
                isWebViewReady = true
            }
        }
    )

    // WebView hazır olduğunda URL'i yükle
    LaunchedEffect(isWebViewReady) {
        if (isWebViewReady && webViewState.value != null) {
            delay(500) // 500ms gecikme ile WebView'in tamamen hazır olmasını bekle
            webViewState.value?.let { webView ->
                android.util.Log.d("FreeMode", "🚀 Loading URL: https://demo.readyplayer.me/avatar")
                webView.loadUrl("https://demo.readyplayer.me/avatar")
            }
        }
    }

    // Lifecycle yönetimi
    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.let { webView ->
                android.util.Log.d("FreeMode", "🧹 Disposing WebView")
                webView.stopLoading()
                webView.clearHistory()
                webView.clearCache(true)
                webView.loadUrl("about:blank")
                webView.onPause()
                webView.removeAllViews()
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.destroy()
            }
        }
    }
}

// Ready Player Me demo URL'i için JavaScript injection
private fun WebView.injectReadyPlayerMeScript() {
    val script = """
        (function() {
            console.log('Ready Player Me script injected');
            
            // Birden fazla yöntemle avatar URL'sini yakalamaya çalış
            function captureAvatarUrl(url) {
                if (url && url.indexOf('.glb') > -1) {
                    console.log('Avatar URL found: ' + url);
                    if (window.Android && window.Android.onAvatarCreated) {
                        window.Android.onAvatarCreated(url);
                        return true;
                    }
                }
                return false;
            }
            
            // 1. PostMessage event'lerini dinle
            window.addEventListener('message', function(event) {
                console.log('Message received:', event.data);
                if (event.data) {
                    if (event.data.type === 'v1.avatar.exported' && event.data.data && event.data.data.url) {
                        captureAvatarUrl(event.data.data.url);
                    } else if (event.data.url && event.data.url.indexOf('.glb') > -1) {
                        captureAvatarUrl(event.data.url);
                    } else if (typeof event.data === 'string' && event.data.indexOf('.glb') > -1) {
                        captureAvatarUrl(event.data);
                    }
                }
            });
            
            // 2. Navigation event'lerini dinle
            window.addEventListener('beforeunload', function() {
                var currentUrl = window.location.href;
                if (currentUrl.indexOf('.glb') > -1) {
                    captureAvatarUrl(currentUrl);
                }
            });
            
            // 3. Ready Player Me iframe içinde çalışıyorsa kontrol et
            setTimeout(function() {
                var iframes = document.querySelectorAll('iframe');
                iframes.forEach(function(iframe) {
                    try {
                        iframe.contentWindow.addEventListener('message', function(event) {
                            if (event.data && (event.data.url || event.data.data)) {
                                var url = event.data.url || (event.data.data && event.data.data.url);
                                if (url) captureAvatarUrl(url);
                            }
                        });
                    } catch(e) {
                        console.log('Cannot access iframe:', e);
                    }
                });
            }, 2000);
            
            // 4. DOM'da GLB linklerini ara
            setTimeout(function() {
                var links = document.querySelectorAll('a[href*=".glb"]');
                links.forEach(function(link) {
                    link.addEventListener('click', function(e) {
                        var url = link.href || link.getAttribute('href');
                        if (captureAvatarUrl(url)) {
                            e.preventDefault();
                        }
                    });
                });
            }, 3000);
        })();
    """.trimIndent()

    evaluateJavascript(script, null)
}

// JavaScript'ten çağrılacak interface
class WebAppInterface(
    private val activity: FreeModeActivity,
    private val onAvatarCreated: (String) -> Unit
) {
    @JavascriptInterface
    fun onAvatarCreated(avatarUrl: String) {
        android.util.Log.d("FreeMode", "📱 [WebAppInterface] onAvatarCreated called with: $avatarUrl")
        android.util.Log.d("FreeMode", "📱 [WebAppInterface] Thread: ${Thread.currentThread().name}")
        android.util.Log.d("FreeMode", "📱 [WebAppInterface] Activity: $activity")

        // JavaScript interface zaten main thread'de çalışır, ama yine de emin olmak için Handler kullan
        Handler(Looper.getMainLooper()).post {
            android.util.Log.d("FreeMode", "📱 [WebAppInterface] Handler post executed - Thread: ${Thread.currentThread().name}")
            try {
                // Önce Compose callback'ini çağır (logging için)
                android.util.Log.d("FreeMode", "🎯 [WebAppInterface] Step 1: Calling Compose callback...")
                onAvatarCreated(avatarUrl)
                android.util.Log.d("FreeMode", "✅ [WebAppInterface] Step 1 completed: Compose callback executed")

                // Sonra Unity'yi başlat - Activity metodunu doğrudan çağır
                android.util.Log.d("FreeMode", "🎯 [WebAppInterface] Step 2: Calling activity.launchUnityWithAvatar() directly...")
                activity.launchUnityWithAvatar(avatarUrl)
                android.util.Log.d("FreeMode", "✅ [WebAppInterface] Step 2 completed: launchUnityWithAvatar called")
            } catch (e: Exception) {
                android.util.Log.e("FreeMode", "❌ [WebAppInterface] Error in callback: ${e.message}", e)
                android.util.Log.e("FreeMode", "❌ [WebAppInterface] Stack trace: ${e.stackTraceToString()}")
                e.printStackTrace()
            }
        }
    }
}
