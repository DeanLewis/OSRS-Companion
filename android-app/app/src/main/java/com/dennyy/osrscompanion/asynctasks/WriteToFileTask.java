package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.Utils;

import java.lang.ref.WeakReference;

public class WriteToFileTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> context;
    private String fileName;
    private String content;

    public WriteToFileTask(final Context context, String fileName, String content) {
        this.context = new WeakReference<>(context);
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Utils.writeToFile(context.get(), fileName, content);
        return null;
    }
}