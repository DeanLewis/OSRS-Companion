package com.dennyy.osrscompanion.interfaces;

public interface ItemIdListResultListener {
    void onItemsUpdated();

    void onItemsNotUpdated();

    void onError();
}