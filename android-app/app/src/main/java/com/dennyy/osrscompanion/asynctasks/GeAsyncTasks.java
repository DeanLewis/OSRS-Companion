package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.interfaces.GeListeners;
import com.dennyy.osrscompanion.models.GrandExchange.*;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummary;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;
import com.dennyy.osrscompanion.viewhandlers.GrandExchangeViewHandler;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public final class GeAsyncTasks {
    private GeAsyncTasks() {

    }

    public static class InsertOrUpdateGeData extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String itemId;
        private String itemData;

        public InsertOrUpdateGeData(final Context context, String itemId, String itemData) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.itemData = itemData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateGrandExchangeData(itemId, itemData);
            }
            return null;
        }
    }

    public static class InsertOrUpdateGeHistory extends AsyncTask<Void, Void, GeHistory> {
        private WeakReference<Context> weakContext;
        private GeListeners.GeHistoryLoadedListener callback;
        private String itemId;
        private String itemName;
        private boolean isFavorite;

        public InsertOrUpdateGeHistory(final Context context, String itemId, String itemName, boolean isFavorite, GeListeners.GeHistoryLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.itemName = itemName;
            this.isFavorite = isFavorite;
            this.callback = callback;
        }

        @Override
        protected GeHistory doInBackground(Void... voids) {
            Context context = weakContext.get();
            GeHistory geHistory = null;
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateGeHistory(itemId, itemName, isFavorite);
                geHistory = AppDb.getInstance(context).getGeHistory();
            }
            return geHistory;
        }

        @Override
        protected void onPostExecute(GeHistory geHistory) {
            if (callback == null) return;
            if (geHistory == null) {
                callback.onGeHistoryLoadFailed();
            }
            else {
                callback.onGeHistoryLoaded(geHistory);
            }
        }
    }

    public static class GetHistory extends AsyncTask<Void, Void, GeHistory> {
        private WeakReference<Context> weakContext;
        private GeListeners.GeHistoryLoadedListener callback;
        private boolean clearHistory;

        public GetHistory(final Context context, boolean clearHistory, GeListeners.GeHistoryLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.clearHistory = clearHistory;
            this.callback = callback;
        }

        @Override
        protected GeHistory doInBackground(Void... voids) {
            Context context = weakContext.get();
            GeHistory geHistory = null;
            if (context != null) {
                geHistory = AppDb.getInstance(context).getGeHistory(clearHistory);
            }
            return geHistory;
        }

        @Override
        protected void onPostExecute(GeHistory geHistory) {
            if (geHistory == null) {
                callback.onGeHistoryLoadFailed();
            }
            else {
                callback.onGeHistoryLoaded(geHistory);
            }
        }
    }

    public static class GetItemData extends AsyncTask<Void, Void, ItemData> {
        private WeakReference<Context> weakContext;
        private GeListeners.ItemDataLoadedListener callback;
        private int itemId;

        public GetItemData(final Context context, int itemId, GeListeners.ItemDataLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.callback = callback;
        }

        @Override
        protected ItemData doInBackground(Void... voids) {
            Context context = weakContext.get();
            ItemData itemData = null;
            if (context != null) {
                GrandExchangeData geData = AppDb.getInstance(context).getGrandExchangeData(itemId);
                GrandExchangeGraphData graphData = AppDb.getInstance(context).getGrandExchangeGraphData(itemId);
                GrandExchangeUpdateData geUpdateData = AppDb.getInstance(context).getGrandExchangeUpdateData();
                HashMap<String, OSBuddySummaryItem> summaryMap = new HashMap<>();
                OSBuddySummary summary = AppDb.getInstance(context).getOSBuddyExchangeSummary();
                if (summary != null) {
                    try {
                        summaryMap = GrandExchangeViewHandler.parseOSBuddySummary(summary.data);
                    }
                    catch (JSONException ex) {
                        Logger.log(ex, "failed to restore osbuddysummary from savedinstancestate", summary.data);
                    }
                }
                itemData = new ItemData(geData, graphData, geUpdateData, summaryMap);
            }
            return itemData;
        }

        @Override
        protected void onPostExecute(ItemData data) {
            if (data == null) {
                callback.onItemDataLoadFailed();
            }
            else {
                callback.onItemDataLoaded(data);
            }
        }
    }

    public static class GetGeUpdate extends AsyncTask<Void, Void, GrandExchangeUpdateData> {
        private WeakReference<Context> weakContext;
        private GeListeners.GeUpdateLoadedListener callback;

        public GetGeUpdate(final Context context, GeListeners.GeUpdateLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.callback = callback;
        }

        @Override
        protected GrandExchangeUpdateData doInBackground(Void... voids) {
            Context context = weakContext.get();
            GrandExchangeUpdateData geUpdateData = null;
            if (context != null) {
                geUpdateData = AppDb.getInstance(context).getGrandExchangeUpdateData();
            }
            return geUpdateData;
        }

        @Override
        protected void onPostExecute(GrandExchangeUpdateData data) {
            if (data == null) {
                callback.onGeUpdateLoadFailed();
            }
            else {
                boolean cacheExpired = Math.abs(System.currentTimeMillis() - data.dateModified) > Constants.GE_UPDATE_CACHE_DURATION;
                callback.onGeUpdateLoaded(cacheExpired, data);
            }
        }
    }

    public static class InsertGeUpdate extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String geUpdateData;

        public InsertGeUpdate(final Context context, String geUpdateData) {
            this.weakContext = new WeakReference<>(context);
            this.geUpdateData = geUpdateData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).updateGrandExchangeUpdateData(geUpdateData);
            }
            return null;
        }
    }
}