package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.customviews.ObservableWebView;

public interface WebViewScrollListener {
    void onWebViewScrollDown(ObservableWebView observableWebView, int y, int oldY);

    void onWebViewScrollUp(ObservableWebView observableWebView, int y, int oldY);
}
