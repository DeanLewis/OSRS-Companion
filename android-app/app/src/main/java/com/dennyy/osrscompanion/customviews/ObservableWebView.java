package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.util.AttributeSet;

import com.dennyy.osrscompanion.interfaces.WebViewScrollListener;

import im.delight.android.webview.AdvancedWebView;

public class ObservableWebView extends AdvancedWebView {

    private WebViewScrollListener webViewScrollListener;
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

    public void setWebViewScrollListener(WebViewScrollListener onScrollChangeListener) {
        this.webViewScrollListener = onScrollChangeListener;
    }

    public WebViewScrollListener getWebViewScrollListener() {
        return webViewScrollListener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        int dy = scrollY - oldScrollY;
        if (webViewScrollListener != null) {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                webViewScrollListener.onWebViewScrollDown(this, scrollY, oldScrollY);
                controlsVisible = false;
                scrolledDistance = 0;
            }
            else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                webViewScrollListener.onWebViewScrollUp(this, scrollY, oldScrollY);
                controlsVisible = true;
                scrolledDistance = 0;
            }
            if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                scrolledDistance += dy;
            }
        }
    }
}
