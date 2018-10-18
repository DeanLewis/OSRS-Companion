package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.osrscompanion.interfaces.TimersLoadedListener;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryDTO;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;
import com.dennyy.osrscompanion.models.Timers.Timer;
import com.dennyy.osrscompanion.viewhandlers.GrandExchangeViewHandler;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class GetTimersTask extends AsyncTask<Void, Void, ArrayList<Timer>> {
    private WeakReference<Context> weakContext;
    private TimersLoadedListener callback;

    public GetTimersTask(final Context context, final TimersLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected ArrayList<Timer> doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null){
            return null;
        }
        ArrayList<Timer> timers = AppDb.getInstance(weakContext.get()).getTimers();
        return timers;
    }

    @Override
    protected void onPostExecute( ArrayList<Timer> timers) {
        if (timers == null) {
            callback.onTimersLoadFailed();
        }
        else {
            callback.onTimersLoaded(timers);
        }
    }
}
