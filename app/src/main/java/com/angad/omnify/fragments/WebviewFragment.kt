package com.angad.omnify.fragments

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import com.angad.omnify.R
import com.angad.omnify.helpers.AppUtils
import kotlinx.android.synthetic.main.fragment_webview.view.*
import android.webkit.WebSettings.PluginState
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_webview.*

/**
 * @author Angad Tiwari
 * @msg article webview page
 */
class WebviewFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_webview, container, false)
        return rootView
    }

    override fun onStart() {
        super.onStart()
        loadArticleWebview()
    }

    /**
     * load the article webpage with all configuration for android webview
     */
    fun loadArticleWebview() {
        if(arguments?.containsKey(AppUtils.ARTICLE_URL_OBJECT)!!) {
            // Get the web view settings instance
            val settings = webview_article.settings

            // Enable java script in web view
            settings.javaScriptEnabled = true

            // Enable and setup web view cache
            settings.setAppCacheEnabled(true)
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            // Enable zooming in web view
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = true

            // Zoom web view text
            settings.textZoom = 125

            // Enable disable images in web view
            settings.blockNetworkImage = false
            // Whether the WebView should load image resources
            settings.loadsImagesAutomatically = true

            // More web view settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true  // api 26
            }
            //settings.pluginState = WebSettings.PluginState.ON
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false

            // More optional settings, you can enable it by yourself
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.loadWithOverviewMode = true
            settings.allowContentAccess = true
            settings.setGeolocationEnabled(true)
            settings.allowUniversalAccessFromFileURLs = true
            settings.allowFileAccess = true

            // WebView settings
            webview_article.fitsSystemWindows = true

            /*
                if SDK version is greater of 19 then activate hardware acceleration
                otherwise activate software acceleration
            */
            webview_article.setLayerType(View.LAYER_TYPE_HARDWARE, null)

            // Set web view client
            webview_article.webViewClient = object: WebViewClient(){
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    // Page loading started
                    // Do something
                    Toast.makeText(this@WebviewFragment.context,"Page loading...", Toast.LENGTH_LONG).show()
                }

                override fun onPageFinished(view: WebView, url: String) {
                }
            }

            // Set web view chrome client
            webview_article.webChromeClient = object: WebChromeClient(){
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                }
            }
            webview_article.loadUrl(arguments?.getString(AppUtils.ARTICLE_URL_OBJECT))
        }
    }
}