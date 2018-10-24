package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.database.ActionsDb;
import com.dennyy.osrscompanion.interfaces.ActionsLoadListener;
import com.dennyy.osrscompanion.models.General.Action;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetActionsTask extends AsyncTask<Void, Void, ArrayList<Action>> {
    private WeakReference<Context> weakContext;
    private ActionsLoadListener callback;
    private int selectedSkillId;

    public GetActionsTask(final Context context, int selectedSkillId, final ActionsLoadListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.selectedSkillId = selectedSkillId;
        this.callback = callback;
    }

    @Override
    protected ArrayList<Action> doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        ArrayList<Action> actions = ActionsDb.getInstance(context).getActions(selectedSkillId);
        return actions;
    }

    @Override
    protected void onPostExecute(ArrayList<Action> actions) {
        if (actions == null) {
            callback.onActionsLoadFailed();
        }
        else {
            callback.onActionsLoaded(actions);
        }
    }
}