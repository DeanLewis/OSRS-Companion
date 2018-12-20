package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;

import java.util.HashMap;

public interface JsonItemsLoadedListener {
    void onJsonItemsLoaded(HashMap<String,JsonItem> items);

    void onJsonItemsLoadError();
}