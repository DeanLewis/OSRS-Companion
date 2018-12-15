package com.dennyy.osrscompanion.viewhandlers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.ObservableWebView;
import com.dennyy.osrscompanion.enums.ScrollState;
import com.dennyy.osrscompanion.helpers.AdBlocker;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ObservableScrollViewCallbacks;
import im.delight.android.webview.AdvancedWebView;

public class RSWikiViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, View.OnClickListener, ObservableScrollViewCallbacks {

    public ObservableWebView webView;

    private ProgressBar progressBar;
    private boolean clearHistory;
    private TextView navBarTitle;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private LinearLayout navBar;
    private LinearLayout webviewContainer;

    public RSWikiViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);

        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        navBarTitle = view.findViewById(R.id.webview_navbar_title);
        navBar = view.findViewById(R.id.webview_navbar);
        webviewContainer = view.findViewById(R.id.webview_container);
        ImageButton navBarLeft = view.findViewById(R.id.webview_navbar_left);
        ImageButton navBarRight = view.findViewById(R.id.webview_navbar_right);
        if (isFloatingView) {
            webView.addScrollViewCallbacks(this);
            navBar.findViewById(R.id.webview_navbar_title).setOnClickListener(this);
            navBar.findViewById(R.id.webview_navbar_to_top).setOnClickListener(this);
            navBarLeft.setOnClickListener(this);
            navBarRight.setOnClickListener(this);
            navBar.setVisibility(View.VISIBLE);
        }
        initWebView();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initWebView() {
        webView.setThirdPartyCookiesEnabled(false);
        webView.setMixedContentAllowed(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl("https://oldschool.runescape.wiki");

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
            case R.id.webview_navbar_title:
            case R.id.webview_navbar_to_top:
                scrollToTop();
                break;
        }
    }

    public void scrollToTop() {
        if (webView == null) return;
        webView.scrollVerticallyTo(0);
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
        navBar.setVisibility(View.GONE);
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
        Utils.clearWebView(webView);
    }

    public void cleanup() {
        clearHistory = true;
    }


    private void pushWebViewDown() {
        int height = navBar.getHeight();
        webviewContainer.animate().translationY(height).setInterpolator(new AccelerateInterpolator(2));
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP && navBar.isShown()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) navBar.getLayoutParams();
            int height = navBar.getHeight() + params.bottomMargin + params.topMargin;
            navBar.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
            webviewContainer.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2));
            navBar.setVisibility(View.GONE);
        }

        else if (scrollState == ScrollState.DOWN && !navBar.isShown()) {
            navBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            navBar.setVisibility(View.VISIBLE);
            pushWebViewDown();
        }
    }
}
