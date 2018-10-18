package com.dennyy.osrscompanion.asynctasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.broadcastreceivers.TimerReceiver;
import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.interfaces.TimersLoadedListener;
import com.dennyy.osrscompanion.models.Timers.Timer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
        if (context == null) {
            return null;
        }
        ArrayList<Timer> timers = AppDb.getInstance(weakContext.get()).getTimers();
        for (Timer timer : timers) {
            if (PendingIntent.getBroadcast(context, timer.id, new Intent(context, TimerReceiver.class), PendingIntent.FLAG_NO_CREATE) != null) {
                timer.setActive(true);
            }
        }
        return timers;
    }

    @Override
    protected void onPostExecute(ArrayList<Timer> timers) {
        if (timers == null) {
            callback.onTimersLoadFailed();
        }
        else {
            callback.onTimersLoaded(timers);
        }
    }
}
