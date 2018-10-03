package com.dennyy.osrscompanion.helpers;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.dennyy.osrscompanion.AppController;

import io.fabric.sdk.android.Fabric;

public class Logger {

    public static void log(String message, Exception exception) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(Log.ERROR, AppController.TAG, exception.getClass().getSimpleName() + " data: " + message);
            Crashlytics.logException(exception);
        }
    }

    public static void log(Exception exception, String... messages) {
        if (Fabric.isInitialized()) {
            StringBuilder stringBuilder = new StringBuilder();

            for (String message : messages) {
                stringBuilder.append(message).append("~");
            }
            String log = stringBuilder.toString();

            Crashlytics.log(Log.ERROR, AppController.TAG, exception.getClass().getSimpleName() + " data: " + log);
            Crashlytics.logException(exception);
        }
    }

    public static void log(Exception exception) {
        if (Fabric.isInitialized()) {
            Crashlytics.logException(exception);
        }
    }
}
