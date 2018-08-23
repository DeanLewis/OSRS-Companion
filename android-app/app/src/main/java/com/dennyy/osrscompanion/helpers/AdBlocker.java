package com.dennyy.osrscompanion.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dennyy.osrscompanion.AppController;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdBlocker {
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init() {
        if (AD_HOSTS.size() > 0) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                loadFromAssets(AppController.getInstance().getApplicationContext());
                return null;
            }
        }.execute();
    }

    private static void loadFromAssets(Context context) {
        try {
            InputStream stream = context.getAssets().open(Constants.HOSTS_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                AD_HOSTS.add(line);
            }
            reader.close();
            stream.close();
        }
        catch (IOException ignored) {
        }
    }

    public static boolean isAd(String url) {
        try {
            return isAdHost(getDomainName(url));
        }
        catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean isAdHost(String host) {
        if (Utils.isNullOrEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) || index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    private static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    private static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain != null && domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static WebViewClient getWebViewClient() {
        return new WebViewClient() {
            private Map<String, Boolean> loadedUrls = new HashMap<>();
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                boolean ad;
                String url = request.getUrl().toString();
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                }
                else {
                    ad = loadedUrls.get(url);
                }
                return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, request);
            }
        };
    }
}
