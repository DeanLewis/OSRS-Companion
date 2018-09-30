package com.dennyy.osrscompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.osrscompanion.enums.ItemIdListUpdateResult;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ItemIdListResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateItemIdListTask extends AsyncTask<Void, Void, ItemIdListUpdateResult> {
    private WeakReference<Context> context;
    private ItemIdListResultListener callback;
    private String downloadedItemIdListJson;

    public UpdateItemIdListTask(final Context context, final String downloadedItemIdListJson, final ItemIdListResultListener callback) {
        this.context = new WeakReference<>(context);
        this.downloadedItemIdListJson = downloadedItemIdListJson;
        this.callback = callback;
    }

    private Date getDateFromItemIdList(String content) throws JSONException, ParseException {
        JSONObject obj = new JSONObject(content);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date fileDate = format.parse(obj.getString("datetime"));
        return fileDate;
    }

    @Override
    protected ItemIdListUpdateResult doInBackground(Void... voids) {
        try {
            String existingItemIdList = Utils.readFromFile(context.get(), Constants.ITEMIDLIST_FILE_NAME);
            if (Utils.isNullOrEmpty(existingItemIdList)) {
                existingItemIdList = downloadedItemIdListJson;
                String assetsItemIdList = Utils.readFromAssets(context.get(), "names.json");
                Date existingFileDate = getDateFromItemIdList(existingItemIdList);
                Date assetsDate = getDateFromItemIdList(assetsItemIdList);
                if (assetsDate.before(existingFileDate)) {
                    Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, downloadedItemIdListJson);
                    return ItemIdListUpdateResult.SUCCESS;
                }
                else {
                    Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, assetsItemIdList);
                    return ItemIdListUpdateResult.UP_TO_DATE;
                }
            }
            Date fileDate = getDateFromItemIdList(existingItemIdList);
            Date resultDate = getDateFromItemIdList(downloadedItemIdListJson);

            if (fileDate.before(resultDate)) {
                Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, downloadedItemIdListJson);
                return ItemIdListUpdateResult.SUCCESS;
            }
        }
        catch (JSONException | ParseException e) {
            Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, "");
            return ItemIdListUpdateResult.ERROR;
        }
        return ItemIdListUpdateResult.UP_TO_DATE;
    }

    @Override
    protected void onPostExecute(ItemIdListUpdateResult result) {
        switch (result) {
            case SUCCESS:
                callback.onItemsUpdated();
                break;
            case UP_TO_DATE:
                callback.onItemsNotUpdated();
                break;
            default:
                callback.onError();
                break;
        }
    }
}
