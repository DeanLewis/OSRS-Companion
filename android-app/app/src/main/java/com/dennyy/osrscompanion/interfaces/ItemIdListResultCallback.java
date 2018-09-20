package com.dennyy.osrscompanion.interfaces;

public interface ItemIdListResultCallback {
    void onItemsUpdated();

    void onItemsNotUpdated();

    void onError();
}