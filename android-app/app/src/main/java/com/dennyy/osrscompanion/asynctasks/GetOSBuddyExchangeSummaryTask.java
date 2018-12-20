package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummary;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;
import com.dennyy.osrscompanion.viewhandlers.GrandExchangeViewHandler;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class GetOSBuddyExchangeSummaryTask extends AsyncTask<Void, Void, HashMap<String, OSBuddySummaryItem>> {
    private WeakReference<Context> weakContext;
    private OSBuddySummaryLoadedListener callback;
    private long dateModified;

    public GetOSBuddyExchangeSummaryTask(final Context context, final OSBuddySummaryLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected HashMap<String, OSBuddySummaryItem> doInBackground(Void... voids) {
        HashMap<String, OSBuddySummaryItem> content = new HashMap<>();
        Context context = weakContext.get();
        if (context == null){
            return content;
        }
        OSBuddySummary summary = AppDb.getInstance(context).getOSBuddyExchangeSummary();
        if (summary != null) {
            dateModified = summary.dateModified;
            try {
                content = GrandExchangeViewHandler.parseOSBuddySummary(summary.data);
            }
            catch (JSONException ex) {
                Logger.log(summary.data, ex);
                return null;
            }
        }
        return content;
    }

    @Override
    protected void onPostExecute(HashMap<String, OSBuddySummaryItem> content) {
        if (content == null) {
            callback.onContentLoadError();
        }
        else {
            boolean cacheExpired = Math.abs(System.currentTimeMillis() - dateModified) > Constants.OSBUDDY_SUMMARY_CACHE_DURATION;
            callback.onContentLoaded(content, dateModified, cacheExpired);
        }
    }
}
