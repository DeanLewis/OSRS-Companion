package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;

import java.util.HashMap;

public interface OSBuddySummaryLoadedCallback {
    void onContentLoaded(HashMap<String,OSBuddySummaryItem> content, boolean cacheExpired);

    void onContentLoadError();
}
