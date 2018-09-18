package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ContentLoadedCallback;

import java.lang.ref.WeakReference;

public class ReadFromFileTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> context;
    private ContentLoadedCallback callback;
    private String content;
    private String fileName;

    public ReadFromFileTask(final Context context, String fileName, final ContentLoadedCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        content = Utils.readFromFile(context.get(), fileName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
    //    if (callback.get() != null) {
            callback.onContentLoaded(content);
      //  }
    }
}