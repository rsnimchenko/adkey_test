package com.kotrev.dek.test

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class WebActivity : AppCompatActivity() {
    private lateinit var wv: WebView

    private var filePath: ValueCallback<Array<Uri>?>? = null

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode != RESULT_OK || filePath == null) {
            filePath?.onReceiveValue(null)
            filePath = null
            return@registerForActivityResult
        }
        var results: Array<Uri>? = null
        val data = it.data

        if (data != null) {
            val dataString = data.dataString
            if (dataString != null) results = arrayOf(Uri.parse(dataString))
        }

        filePath!!.onReceiveValue(results)
        filePath = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        FullScreen.fullScreen(this@WebActivity)
        FullScreen.enableFullScreen(this@WebActivity)

        val link = intent.getStringExtra("url") ?: ""

        wv = findViewById(R.id.webView)
        setWVSettings()

        wv.webViewClient = createWVClient()
        wv.webChromeClient = createWCClient()

        wv.loadUrl(link)

        onBackPressedDispatcher.addCallback {
            if (wv.canGoBack()) wv.goBack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWVSettings() {
        wv.apply {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            isSaveEnabled = true
            isFocusableInTouchMode = true
            isFocusable = true
            isHorizontalScrollBarEnabled = true
            isVerticalScrollBarEnabled = true
        }

        wv.settings.apply {
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            allowFileAccess = true
            setSupportZoom(true)
            pluginState = WebSettings.PluginState.ON
            userAgentString = userAgentString.replace("; wv", "")
            domStorageEnabled = true
            javaScriptEnabled = true
            mixedContentMode = 0
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            saveFormData = true
            savePassword = true
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            allowContentAccess = true
            setEnableSmoothTransition(true)
        }
    }

    private fun createWCClient() = object : WebChromeClient() {
        override fun onShowFileChooser(
            view: WebView?,
            filePath: ValueCallback<Array<Uri>?>,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (this@WebActivity.filePath != null)
                this@WebActivity.filePath?.onReceiveValue(null)

            this@WebActivity.filePath = filePath

            resultLauncher.launch(intentContent())
            return true
        }
    }

    private fun createWVClient() = object : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleUri(url, view)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            return handleUri(request.url.toString(), view)
        }
    }

    private fun handleUri(url: String, view: WebView): Boolean {
        if (url.startsWith("mailto")) {
            startActivity(intentSend(url))
        } else if (url.startsWith("tel:")) {
            startActivity(intentDial(url))
        } else if (url.startsWith("https://t.me/account") ||
            url.startsWith("tg://data") ||
            url.startsWith("viber://") ||
            url.startsWith("whatsapp://")
        ) {
            startActivity(intentView(url))
        } else if (url.startsWith("http") || url.startsWith("https")) return false

        return try {
            view.context.startActivity(intentView(url))
            true
        } catch (e: Exception) {
            true
        }
    }

    private fun intentContent(): Intent {
        return Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, intentGetContent())
            putExtra(Intent.EXTRA_TITLE, "Select Option:")
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOfNulls<Intent?>(0))
        }
    }

    private fun intentSend(url: String): Intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "plain/text"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(url.replace("mailto:", "")))
        },
        "Mail to Support"
    )

    private fun intentDial(url: String) = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse(url) }

    private fun intentView(url: String) = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }

    private fun intentGetContent() = Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wv.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wv.saveState(outState)
    }
}