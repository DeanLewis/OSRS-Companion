package com.dennyy.osrscompanion.broadcastreceivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dennyy.osrscompanion.enums.ReloadTimerSource;
import com.dennyy.osrscompanion.helpers.TimerIntentHelper;
import com.dennyy.osrscompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.osrscompanion.models.Timers.Timer;

import org.greenrobot.eventbus.EventBus;

public class RestartTimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timer timer = TimerIntentHelper.GetTimerFromExtras(intent.getExtras());
        if (timer == null) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            PendingIntent pendingIntent = TimerIntentHelper.CreateIntent(context, timer, TimerReceiver.class, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timer.getDelayMs(), pendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(timer.id);
            }
            EventBus.getDefault().post(new ReloadTimersEvent(ReloadTimerSource.ANY));
        }
    }
}