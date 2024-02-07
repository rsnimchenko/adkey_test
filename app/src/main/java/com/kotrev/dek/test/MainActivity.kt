package com.kotrev.dek.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FullScreen.fullScreen(this@MainActivity)
        FullScreen.enableFullScreen(this@MainActivity)
        OneSignal.initWithContext(this, "860e166b-4270-4f33-a792-e820ac23ca7b")

        lifecycleScope.launch {
            val adId = getAdId()
            adId?.let { OneSignal.login(it) }
            openWeb()
        }
    }

    private suspend fun getAdId() = withContext(Dispatchers.IO) {
        try {
            AdvertisingIdClient.getAdvertisingIdInfo(this@MainActivity).id
        } catch (_: Exception) {
            null
        }
    }

    private fun openWeb() {
        startActivity(Intent(this, WebActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("url", "https://first.ua/")
        })
        finish()
    }
}