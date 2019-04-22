package com.ric.midflutter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.midtrans.sdk.uikit.activities.BaseActivity;

abstract class WebviewBaseActivity extends BaseActivity {

    public static final String EXTRA_URL = "url";

    protected WebView webView;
    protected String url;


    @SuppressLint("SetJavaScriptEnabled")
    protected void populateWebview(FrameLayout webViewContainer) {

        url = getIntent().getStringExtra(EXTRA_URL);
        if (webView == null) {
            webView = new WebView(this);
            webView.clearCache(true);
            webView.getSettings().setJavaScriptEnabled(true);
            setupWebViewClient();
            webView.loadUrl(url);
        }

        webViewContainer.addView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            webView.destroy();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    protected abstract void setupWebViewClient();

}

public class WebviewVerifyActivity extends WebviewBaseActivity {

    public static final String SUCCESS_URL = "/token/callback/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        FrameLayout webViewContainer = (FrameLayout) findViewById(R.id.webview_container);

        populateWebview(webViewContainer);
    }


    @Override
    protected void setupWebViewClient() {
        webView.setWebViewClient(new MyWebClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void completePayment() {
        setResult(RESULT_OK);
        finish();
    }

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //Timber.i("Load url : %s", url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Timber.i("Overloading url : %s", url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("Load finished, url : ", url);
            if (url.contains(SUCCESS_URL)) {
                completePayment();
            }
            super.onPageFinished(view, url);
        }
    }

}