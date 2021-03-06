package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;

import java.util.HashMap;

public interface OSBuddySummaryLoadedListener {
    void onContentLoaded(HashMap<String,OSBuddySummaryItem> content, long dateModified, boolean cacheExpired);

    void onContentLoadError();
}
