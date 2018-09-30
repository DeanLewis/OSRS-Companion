package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.OSRSNews.OSRSNews;
import com.dennyy.osrscompanion.models.OSRSNews.OSRSNewsDTO;

import java.util.ArrayList;

public interface OSRSNewsLoadedListener {
    void onOSRSNewsLoaded(OSRSNewsDTO osrsNewsDTO);
}
