package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.interfaces.UserStatsLoadedListener;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import java.lang.ref.WeakReference;

public class GetUserStatsTask extends AsyncTask<Void, Void, UserStats> {
    private WeakReference<Context> weakContext;
    private UserStatsLoadedListener listener;
    private String rsn;
    private HiscoreType hiscoreType;

    public GetUserStatsTask(final Context context, String rsn, HiscoreType hiscoreType, final UserStatsLoadedListener listener) {
        this.weakContext = new WeakReference<>(context);
        this.rsn = rsn;
        this.hiscoreType = hiscoreType;
        this.listener = listener;
    }

    @Override
    protected UserStats doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        UserStats userStats = null;
        try {
            userStats = AppDb.getInstance(context).getUserStats(rsn, hiscoreType);
        }
        catch (Exception ex) {
            Logger.log(ex);
        }
        return userStats;
    }

    @Override
    protected void onPostExecute(UserStats userStats) {
        if (userStats == null) {
            listener.onUserStatsLoadFailed();
        }
        else {
            listener.onUserStatsLoaded(userStats);
        }
    }
}