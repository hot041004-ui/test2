package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          ScienceQuizWebViewScreen(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun ScienceQuizWebViewScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    webViewInstance?.reload()
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Handle hardware back button in WebView
  BackHandler(enabled = webViewInstance?.canGoBack() == true) {
    webViewInstance?.goBack()
  }

  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { ctx ->
      val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
        .build()

      WebView(ctx).apply {
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          mediaPlaybackRequiresUserGesture = false
          allowFileAccess = true
          allowContentAccess = true
          databaseEnabled = true
        }

        webViewClient = object : WebViewClient() {
          override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
          ): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
          }
        }

        webChromeClient = object : WebChromeClient() {
          override fun onPermissionRequest(request: PermissionRequest) {
            // Automatically grant WebRTC camera and audio permissions to the secure asset app
            request.grant(request.resources)
          }
        }

        // Load the local HTML5/React app via secure HTTPS origin
        loadUrl("https://appassets.androidplatform.net/assets/index.html")
        webViewInstance = this
      }
    },
    update = { webView ->
      webViewInstance = webView
    }
  )
}
