package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.Fragment

class RulesFragment : Fragment(R.layout.fragment_rules) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val webView =
            view.findViewById<WebView>(R.id.webRules)

        webView.settings.javaScriptEnabled = false

        val html = resources
            .openRawResource(R.raw.rules)
            .bufferedReader(Charsets.UTF_8)
            .use {
                it.readText()
            }

        webView.loadDataWithBaseURL(
            null,
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
}