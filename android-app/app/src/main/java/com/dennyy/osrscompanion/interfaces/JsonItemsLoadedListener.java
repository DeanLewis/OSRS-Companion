package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;

import java.util.ArrayList;

public interface JsonItemsLoadedListener {
    void onJsonItemsLoaded(ArrayList<JsonItem> items);

    void onJsonItemsLoadError();
}