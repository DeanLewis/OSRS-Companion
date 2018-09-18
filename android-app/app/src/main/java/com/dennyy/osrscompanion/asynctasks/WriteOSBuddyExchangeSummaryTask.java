package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.AppDb;

import java.lang.ref.WeakReference;

public class WriteOSBuddyExchangeSummaryTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> context;
    private String content;

    public WriteOSBuddyExchangeSummaryTask(final Context context, String content) {
        this.context = new WeakReference<>(context);
        this.content = content;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AppDb.getInstance(context.get()).insertOrUpdateOSBuddyExchangeSummaryData(content);
        return null;
    }
}
