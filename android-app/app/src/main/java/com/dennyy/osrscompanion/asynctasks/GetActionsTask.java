package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.enums.SkillType;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ActionsLoadListener;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillData;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataAction;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataBonus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetActionsTask extends AsyncTask<Void, Void, SkillData> {
    private WeakReference<Context> weakContext;
    private ActionsLoadListener callback;
    private String dataFileName;
    private SkillType skillType;

    public GetActionsTask(final Context context, SkillType skillType, String dataFileName, final ActionsLoadListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.dataFileName = dataFileName;
        this.callback = callback;
        this.skillType = skillType;
    }

    @Override
    protected SkillData doInBackground(Void... voids) {
        ArrayList<SkillDataBonus> bonuses = new ArrayList<>();
        ArrayList<SkillDataAction> actions = new ArrayList<>();
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        try {
            String actionsString = Utils.readFromAssets(context, dataFileName);
            JSONObject jsonObject = new JSONObject(actionsString);
            if (jsonObject.has("bonuses")) {
                JSONArray bonusesArray = jsonObject.getJSONArray("bonuses");
                for (int i = 0; i < bonusesArray.length(); i++) {
                    JSONObject bonusObject = bonusesArray.getJSONObject(i);
                    String name = bonusObject.getString("name");
                    float value = (float) bonusObject.getDouble("value");
                    bonuses.add(new SkillDataBonus(name, value));
                }
            }

            JSONArray actionsArray = jsonObject.getJSONArray("actions");
            for (int i = 0; i < actionsArray.length(); i++) {
                JSONObject actionsObject = actionsArray.getJSONObject(i);
                String name = actionsObject.getString("name");
                int level = actionsObject.getInt("level");
                double exp = actionsObject.getDouble("xp");
                boolean ignoreBonus = actionsObject.has("ignoreBonus") && actionsObject.getBoolean("ignoreBonus");
                actions.add(new SkillDataAction(skillType, name, level, exp, ignoreBonus));

            }
        }
        catch (Exception ex) {
            Logger.log(ex);
            return null;
        }
        SkillData skillData = new SkillData(bonuses, actions);
        return skillData;
    }

    @Override
    protected void onPostExecute(SkillData skillData) {
        if (skillData == null) {
            callback.onActionsLoadFailed();
        }
        else {
            callback.onActionsLoaded(skillData);
        }
    }
}