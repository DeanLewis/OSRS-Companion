package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;

public abstract class BaseViewHandler {
    private Toast toast;
    private final String defaultRsn;
    protected Context context;
    protected View view;
    protected Resources resources;
    protected boolean wasRequesting;

    BaseViewHandler(Context context, View view) {
        this.context = context;
        this.view = view;
        this.resources = context.getResources();
        this.defaultRsn = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_RSN, "");
    }

    protected void showToast(String message, int duration) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    protected String getString(int resourceId) {
        return context.getResources().getString(resourceId);
    }

    protected String getString(int resourceId, Object... formatArgs) {
        return context.getResources().getString(resourceId, formatArgs);
    }

    protected void hideKeyboard() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.hideKeyboard(context, view);
            }
        }, 500);
    }

    /**
     * Try to get text from edittext as rsn else it uses the defaultRsn that the user set in the settings
     * Check the result of this method also for null or empty and handle accordingly
     *
     * @param editText The edittext to get the text from
     * @return String with the rsn
     */
    protected String getRsn(EditText editText) {
        String result = editText.getText().toString();
        if (Utils.isNullOrEmpty(result)) {
            result = defaultRsn;
        }
        if (!Utils.isNullOrEmpty(result)) {
            editText.setText(result);
        }
        return result;
    }

    public abstract boolean wasRequesting();

    public abstract void cancelVolleyRequests();
}