package com.ahmetkaraaslan.labx

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ahmetkaraaslan.labx.ui.theme.KimyasalTheme
import com.ahmetkaraaslan.labx.ui.theme.LabX_Background_Gradient
import com.ahmetkaraaslan.labx.utils.playClickFeedback
import com.unity3d.player.UnityPlayerActivity

class FreeModeActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var avatarUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            KimyasalTheme {
                ReadyPlayerMeScreen(
                    onWebViewCreated = { view -> webView = view },
                    onBackPressed = { finish() },
                    onAvatarCreated = { url ->
                        avatarUrl = url
                        launchUnityWithAvatar(url)
                    }
                )
            }
        }
    }

    private fun launchUnityWithAvatar(avatarUrl: String) {
        val intent = Intent(this, UnityPlayerActivity::class.java)
        intent.putExtra("avatarUrl", avatarUrl)
        startActivity(intent)
        finish() // WebView Activity'sini kapat
    }

    override fun onBackPressed() {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyPlayerMeScreen(
    onWebViewCreated: (WebView) -> Unit,
    onBackPressed: () -> Unit,
    onAvatarCreated: (String) -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.free_mode), 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        playClickFeedback(context)
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = Modifier.background(LabX_Background_Gradient)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = false
                        settings.displayZoomControls = false
                        
                        // Ready Player Me Frame API için JavaScript interface
                        addJavascriptInterface(WebAppInterface { url ->
                            onAvatarCreated(url)
                        }, "Android")
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Ready Player Me Frame API event listener'ını inject et
                                injectFrameAPIScript()
                            }
                        }
                        
                        // WebView referansını sakla
                        onWebViewCreated(this)
                        
                        // Ready Player Me Avatar Creator URL'sini yükle
                        loadUrl("https://readyplayer.me/avatar-creator?frameApi")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// WebView'a JavaScript inject etmek için extension function
private fun WebView.injectFrameAPIScript() {
    val script = """
        (function() {
            window.addEventListener('message', function(event) {
                if (event.data && event.data.type === 'v1.avatar.exported') {
                    var avatarUrl = event.data.data.url;
                    if (avatarUrl && window.Android) {
                        window.Android.onAvatarCreated(avatarUrl);
                    }
                }
            });
        })();
    """.trimIndent()
    
    evaluateJavascript(script, null)
}

// JavaScript'ten çağrılacak interface
class WebAppInterface(private val onAvatarCreated: (String) -> Unit) {
    @JavascriptInterface
    fun onAvatarCreated(avatarUrl: String) {
        onAvatarCreated(avatarUrl)
    }
}
