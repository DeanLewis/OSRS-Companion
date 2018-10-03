package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.util.AttributeSet;

import com.dennyy.osrscompanion.interfaces.WebViewScrollCallback;

import im.delight.android.webview.AdvancedWebView;

public class ObservableWebView extends AdvancedWebView {

    private WebViewScrollCallback webViewScrollCallback;
    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    public ObservableWebView(Context context) {
        super(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWebViewScrollCallback(WebViewScrollCallback onScrollChangeListener) {
        this.webViewScrollCallback = onScrollChangeListener;
    }

    public WebViewScrollCallback getWebViewScrollCallback() {
        return webViewScrollCallback;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        int dy = scrollY - oldScrollY;
        if (webViewScrollCallback != null) {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                webViewScrollCallback.onWebViewScrollDown(this, scrollY, oldScrollY);
                controlsVisible = false;
                scrolledDistance = 0;
            }
            else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                webViewScrollCallback.onWebViewScrollUp(this, scrollY, oldScrollY);
                controlsVisible = true;
                scrolledDistance = 0;
            }
            if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                scrolledDistance += dy;
            }
        }
    }
}
