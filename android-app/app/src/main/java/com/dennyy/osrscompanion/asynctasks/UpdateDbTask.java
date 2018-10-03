package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.database.ActionsDb;
import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.helpers.Logger;

import java.lang.ref.WeakReference;

// ensure database is updated when using in the app, https://stackoverflow.com/questions/3163845/is-the-onupgrade-method-ever-called
public class UpdateDbTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> weakContext;

    public UpdateDbTask(final Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context != null) {
            try {
                AppDb.getInstance(context).getWritableDatabase();
                ActionsDb.getInstance(context).getWritableDatabase();
            }
            catch (SQLiteDatabaseLockedException | NullPointerException ex) {
                Logger.log(ex);
            }
        }
        return null;
    }
}