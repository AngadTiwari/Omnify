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

            // WebView settings
            webview_article.fitsSystemWindows = true
            // Set web view chrome client
            webview_article.webChromeClient = object: WebChromeClient(){
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                }
            }
            webview_article.loadUrl(arguments?.getString(AppUtils.ARTICLE_URL_OBJECT))
        }
    }
}