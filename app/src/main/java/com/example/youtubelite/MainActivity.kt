package com.example.youtubelite

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var fullScreenContainer: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        fullScreenContainer = findViewById(R.id.fullScreenContainer)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        
        webSettings.userAgentString = webSettings.userAgentString.replace("; wv", "")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return handleUrl(url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleUrl(url)
            }

            private fun handleUrl(url: String?): Boolean {
                if (url != null) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        return false 
                    }
                    return true
                }
                return false
            }

            // --- SKRIP PEMBLOKIR IKLAN & CUSTOM LOGO RUMAH GADGET (SUPPORT ANDROID LAWAS & BARU) ---
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                
                val combinedScript = """
                    javascript:(function() {
                        setInterval(function() {
                            // 1. BLOKIR IKLAN & AUTO SKIP
                            var ads = document.querySelectorAll('.ad-showing, .ad-container, .ytp-ad-overlay-container, .ytp-ad-image-overlay');
                            ads.forEach(function(ad) { ad.style.display = 'none'; });
                            
                            var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button');
                            if (skipBtn) { skipBtn.click(); }
                            
                            var vid = document.querySelector('video');
                            if (vid && document.querySelector('.ad-showing')) {
                                vid.currentTime = vid.duration;
                            }

                            // 2. GANTI LOGO YANG KUAT UNTUK SEMUA VERSI ANDROID
                            var ytLogo = document.querySelector('ytm-home-logo') || document.querySelector('.mobile-topbar-header-content');
                            if (ytLogo) {
                                var targetLogo = ytLogo.querySelector('a') || ytLogo;
                                if (!targetLogo.dataset.rgModified) {
                                    targetLogo.innerHTML = '<div style="display: flex; align-items: center; gap: 4px; padding: 2px 0;">' +
                                        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="26" height="26" fill="#FF0000"><path d="M21.58 7.19c-.23-.86-.91-1.54-1.77-1.77C18.25 5 12 5 12 5s-6.25 0-7.81.42c-.86.23-1.54.91-1.77 1.77C2 8.75 2 12 2 12s0 3.25.42 4.81c.23.86.91 1.54 1.77 1.77C5.75 19 12 19 12 19s6.25 0 7.81-.42c.86-.23 1.54-.91 1.77-1.77C22 15.25 22 12 22 12s0-3.25-.42-4.81z"></path><path d="M10 15l5-3-5-3v6z" fill="#FFFFFF"></path></svg>' +
                                        '<span style="font-family: Roboto, Arial, sans-serif; font-size: 18px; font-weight: 600; letter-spacing: -0.5px; color: var(--ytm-spec-text-primary, #0f0f0f);">Rumah Gadget</span>' +
                                    '</div>';
                                    targetLogo.dataset.rgModified = 'true';
                                }
                            }
                        }, 300);
                    })();
                """.trimIndent()
                
                view?.evaluateJavascript(combinedScript, null)
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                
                webView.visibility = View.GONE
                fullScreenContainer.visibility = View.VISIBLE
                fullScreenContainer.addView(view)
                
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                
                fullScreenContainer.removeView(customView)
                fullScreenContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                
                customViewCallback?.onCustomViewHidden()
                customView = null
                
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        webView.loadUrl("https://m.youtube.com")
    }

    override fun onBackPressed() {
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
