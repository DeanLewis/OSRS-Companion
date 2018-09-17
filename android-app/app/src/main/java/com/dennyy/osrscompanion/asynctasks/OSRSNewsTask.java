package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.interfaces.OSRSNewsLoadedCallback;
import com.dennyy.osrscompanion.models.OSRSNews.OSRSNewsDTO;

import java.lang.ref.WeakReference;

public class OSRSNewsTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> context;
    private WeakReference<OSRSNewsLoadedCallback> callback;
    private OSRSNewsDTO osrsNewsDTO;

    public OSRSNewsTask(final Context context, final OSRSNewsLoadedCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        osrsNewsDTO = AppDb.getInstance(context.get()).getOSRSNews();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (callback.get() != null) {
            callback.get().onOSRSNewsLoaded(osrsNewsDTO);
        }
    }
}
