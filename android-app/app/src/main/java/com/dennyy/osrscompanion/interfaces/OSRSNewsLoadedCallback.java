package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.OSRSNews.OSRSNews;

import java.util.ArrayList;

public interface OSRSNewsLoadedCallback {
    void onOSRSNewsLoaded(ArrayList<OSRSNews> news);

    void onOSRSNewsLoadError();
}
