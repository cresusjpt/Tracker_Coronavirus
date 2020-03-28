package com.saltechdigital.coronavirus.views.webview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.saltechdigital.coronavirus.R;

public class WebViewActivity extends AppCompatActivity {

    public static String LOAD_URL ="urlToLoad";

    private Intent intent;
    private Toolbar toolbar;
    private WebView webView;
    private WebSettings webSettings;
    private ProgressBar progressBar;
    private ActionBar actionBar;

    private String urltoload = "https://www.google.tg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        intent = getIntent();
        urltoload = intent.getStringExtra(LOAD_URL);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.baseline_close_24);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        webView.loadUrl(urltoload);
        webView.setWebViewClient(new WebViewClientInApp());
        webView.setWebChromeClient(new WebChromeClientInApp());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public class WebChromeClientInApp extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if (progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    public class WebViewClientInApp extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
            //return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            //progressDialog = ProgressDialog.show(WebviewInAppActivity.this,"",getString(R.string.loading),true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            toolbar.setTitle(webView.getTitle());
            toolbar.setSubtitle(webView.getUrl());
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Toast.makeText(WebViewActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, request, error);
        }
    }
}
