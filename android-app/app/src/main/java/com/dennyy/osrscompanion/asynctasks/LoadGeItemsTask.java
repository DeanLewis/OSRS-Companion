package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.dennyy.osrscompanion.enums.GeItemsSource;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.JsonItemsLoadedListener;
import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LoadGeItemsTask extends AsyncTask<String, Void, ArrayList<JsonItem>> {
    private WeakReference<Context> weakContext;
    private JsonItemsLoadedListener jsonItemsLoadedListener;
    private ArrayList<JsonItem> allItems = new ArrayList<>();
    private HashMap<String, Integer> itemLimits = new HashMap<>();

    public LoadGeItemsTask(Context context, JsonItemsLoadedListener jsonItemsLoadedListener) {
        this.weakContext = new WeakReference<>(context);
        this.jsonItemsLoadedListener = jsonItemsLoadedListener;
    }

    @Override
    protected ArrayList<JsonItem> doInBackground(String... params) {
        Context context = weakContext.get();
        if (context == null) {
            return allItems;
        }
        GeItemsSource geItemsSource = GeItemsSource.fromName(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_GE_SOURCE, GeItemsSource.BOTH.getName()));
        String json = Utils.readFromFile(context, Constants.ITEMIDLIST_FILE_NAME);
        try {
            String itemLimitsJson = Utils.readFromAssets(context, "ge_limits.json");
            JSONObject jsonObject = new JSONObject(itemLimitsJson);
            Iterator itemLimitsIterator = jsonObject.keys();
            while (itemLimitsIterator.hasNext()) {
                String itemId = (String) itemLimitsIterator.next();
                int limit = jsonObject.getInt(itemId);
                itemLimits.put(itemId, limit);
            }

            String namesJson = Utils.readFromAssets(context, "names.json");
            if (Utils.isNullOrEmpty(json)) {
                json = namesJson;
            }
            else if (RsUtils.getDateFromItemIdList(json).before(RsUtils.getDateFromItemIdList(namesJson))) {
                Utils.writeToFile(context, Constants.ITEMIDLIST_FILE_NAME, "");
                json = namesJson;
            }
            JSONObject obj = new JSONObject(json);
            String itemsString = obj.getString("items");
            JSONObject items = new JSONObject(itemsString);
            Iterator iterator = items.keys();
            while (iterator.hasNext()) {
                String id = (String) iterator.next();
                JSONObject result = items.getJSONObject(id);
                boolean isMembers = result.getBoolean("p2p");
                if (geItemsSource == GeItemsSource.BOTH
                        || geItemsSource == GeItemsSource.F2P && !isMembers
                        || geItemsSource == GeItemsSource.P2P && isMembers) {
                    allItems.add(getJsonItemFromJson(id, isMembers, result));
                }
            }
        }
        catch (JSONException | ParseException ex) {
            Logger.log(ex);
        }
        return allItems;
    }

    private JsonItem getJsonItemFromJson(String id, boolean isMembers, JSONObject result) throws JSONException {
        JsonItem geResult = new JsonItem();
        geResult.id = id;
        geResult.name = result.getString("name");
        geResult.store = result.getString("store");
        geResult.isMembers = isMembers;
        geResult.limit = itemLimits.containsKey(id) ? itemLimits.get(id) : -1;
        return geResult;
    }

    @Override
    protected void onPostExecute(ArrayList<JsonItem> items) {
        if (items.size() > 0) {
            jsonItemsLoadedListener.onJsonItemsLoaded(items);
        }
        else {
            jsonItemsLoadedListener.onJsonItemsLoadError();
        }
    }
}