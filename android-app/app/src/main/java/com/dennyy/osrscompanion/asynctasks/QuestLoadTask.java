package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.QuestsLoadedCallback;
import com.dennyy.osrscompanion.models.General.Quest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class QuestLoadTask extends AsyncTask<String, Void, ArrayList<Quest>> {
    private WeakReference<Context> context;
    private QuestsLoadedCallback questsLoadedCallback;
    private ArrayList<Quest> quests = new ArrayList<>();

    public QuestLoadTask(Context context, QuestsLoadedCallback questsLoadedCallback) {
        this.context = new WeakReference<>(context);
        this.questsLoadedCallback = questsLoadedCallback;
    }

    @Override
    protected ArrayList<Quest> doInBackground(String... params) {
        try {
            JSONArray array = new JSONArray(Utils.readFromAssets(context.get(), "quests.json"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Quest quest = new Quest(obj.getString("name"), obj.getString("url"), obj.getString("runehqurl"));
                quests.add(quest);
            }
            Collections.sort(quests, new Comparator<Quest>() {
                @Override
                public int compare(Quest quest1, Quest quest2) {
                    return quest1.name.compareTo(quest2.name);
                }
            });
        }
        catch (JSONException ignored) {

        }
        return quests;
    }

    @Override
    protected void onPostExecute(ArrayList<Quest> quests) {
        if (quests.size() > 0) {
            questsLoadedCallback.onQuestsLoaded(quests);
        }
        else {
            questsLoadedCallback.onQuestsLoadError();
        }
    }
}