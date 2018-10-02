package com.dennyy.osrscompanion.viewhandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.AdBlocker;
import com.dennyy.osrscompanion.helpers.Utils;

import im.delight.android.webview.AdvancedWebView;

public class RSWikiViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, View.OnClickListener {

    public AdvancedWebView webView;

    private ProgressBar progressBar;
    private boolean clearHistory;
    private String currentUrl;
    private TextView navBarTitle;
    private final Handler handler = new Handler();
    private Runnable runnable;

    public RSWikiViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view);

        currentUrl = "https://oldschool.runescape.wiki";
        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        navBarTitle = view.findViewById(R.id.webview_navbar_title);
        ImageButton navBarLeft = view.findViewById(R.id.webview_navbar_left);
        ImageButton navBarRight = view.findViewById(R.id.webview_navbar_right);
        if (isFloatingView) {
            navBarLeft.setOnClickListener(this);
            navBarRight.setOnClickListener(this);
            view.findViewById(R.id.webview_navbar).setVisibility(View.VISIBLE);
        }
        initWebView();
    }


    private void initWebView() {
        webView.addPermittedHostname("oldschool.runescape.wiki");
        webView.setThirdPartyCookiesEnabled(false);
        webView.setMixedContentAllowed(false);
        webView.loadUrl(currentUrl);

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        webView.setListener(activity, this);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
        webView.setWebViewClient(AdBlocker.getWebViewClient());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.webview_navbar_left:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                break;
            case R.id.webview_navbar_right:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        navBarTitle.setText(Utils.isNullOrEmpty(webView.getTitle()) ? getString(R.string.osrs_wiki) : webView.getTitle());
        handler.removeCallbacks(runnable);

        runnable = new Runnable() {
            @Override
            public void run() {
                handlePageTimerFinished();
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    private void handlePageTimerFinished() {
        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);
        if (clearHistory) {
            clearHistory = false;
            webView.clearHistory();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 250);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        showToast(getString(R.string.unexpected_error, description), Toast.LENGTH_SHORT);
    }


    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {
        showToast(getString(R.string.external_navigation_prohibited, "the wiki"), Toast.LENGTH_SHORT);
    }

    public void restoreWebView(Bundle webViewState) {
        webView.restoreState(webViewState);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelRunningTasks() {
        handler.removeCallbacks(runnable);
        if (webView != null) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.destroy();
            webView = null;
        }
    }

    public void cleanup() {
        clearHistory = true;
    }
}
