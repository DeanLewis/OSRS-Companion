package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.interfaces.OSBuddySummaryLoadedCallback;
import com.dennyy.osrscompanion.layouthandlers.GrandExchangeViewHandler;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryDTO;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class GetOSBuddyExchangeSummaryTask extends AsyncTask<Void, Void, HashMap<String, OSBuddySummaryItem>> {
    private WeakReference<Context> context;
    private OSBuddySummaryLoadedCallback callback;
    private long dateModified;

    public GetOSBuddyExchangeSummaryTask(final Context context, final OSBuddySummaryLoadedCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected HashMap<String, OSBuddySummaryItem> doInBackground(Void... voids) {
        HashMap<String, OSBuddySummaryItem> content = new HashMap<>();
        OSBuddySummaryDTO summaryDTO = AppDb.getInstance(context.get()).getOSBuddyExchangeSummary();
        if (summaryDTO != null) {
            dateModified = summaryDTO.dateModified;
            try {
                content = GrandExchangeViewHandler.parseOSBuddySummary(summaryDTO.data);
            }
            catch (JSONException ignored) {
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
            callback.onContentLoaded(content, cacheExpired);
        }
    }
}
