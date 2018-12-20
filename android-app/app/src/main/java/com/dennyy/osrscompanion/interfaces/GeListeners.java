package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.GrandExchange.GeHistory;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeUpdateData;
import com.dennyy.osrscompanion.models.GrandExchange.ItemData;

public class GeListeners {
    private GeListeners() {
    }

    public interface GeHistoryLoadedListener {
        void onGeHistoryLoaded(GeHistory geHistory);

        void onGeHistoryLoadFailed();
    }

    public interface ItemDataLoadedListener {
        void onItemDataLoaded(ItemData itemData);

        void onItemDataLoadFailed();
    }

    public interface GeUpdateLoadedListener {
        void onGeUpdateLoaded(boolean cacheExpired, GrandExchangeUpdateData grandExchangeUpdateData);

        void onGeUpdateLoadFailed();
    }
}
