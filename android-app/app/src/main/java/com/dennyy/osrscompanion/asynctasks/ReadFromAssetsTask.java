package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ContentLoadedListener;

import java.lang.ref.WeakReference;

public class ReadFromAssetsTask extends AsyncTask<Void, Void, String> {
    private WeakReference<Context> weakContext;
    private ContentLoadedListener callback;
    private String fileName;

    public ReadFromAssetsTask(final Context context, String fileName, final ContentLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
        this.fileName = fileName;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String content = "";
        Context context = weakContext.get();
        if (context != null) {
            content = Utils.readFromAssets(weakContext.get(), fileName);
        }
        return content;
    }

    @Override
    protected void onPostExecute(String content) {
        callback.onContentLoaded(content);
    }
}