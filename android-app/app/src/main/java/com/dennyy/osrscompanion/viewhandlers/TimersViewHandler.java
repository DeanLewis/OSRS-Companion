package com.dennyy.osrscompanion.viewhandlers;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.dennyy.osrscompanion.BuildConfig;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TimersAdapter;
import com.dennyy.osrscompanion.asynctasks.GetTimersTask;
import com.dennyy.osrscompanion.customviews.TimerEditor;
import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.AdapterTimerClickListener;
import com.dennyy.osrscompanion.interfaces.TimerEditorListener;
import com.dennyy.osrscompanion.interfaces.TimersLoadedListener;
import com.dennyy.osrscompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.osrscompanion.models.Timers.Timer;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class TimersViewHandler extends BaseViewHandler implements TimerEditorListener, TimersLoadedListener, AdapterTimerClickListener {

    private ListView timersListView;
    private TimersAdapter timersAdapter;
    private TimerEditor timerEditor;

    public TimersViewHandler(Context context, View view) {
        super(context, view);

        timersListView = view.findViewById(R.id.timers_listview);
        initEditor();
        reloadTimers();
    }

    private void initEditor() {
        timerEditor = view.findViewById(R.id.timer_editor);
        timerEditor.setListener(this);
    }

    public void openAddTimerView() {
        timersListView.setVisibility(View.GONE);
        view.findViewById(R.id.timer_editor).setVisibility(View.VISIBLE);
    }

    public void reloadTimers() {
        new GetTimersTask(context, this).execute();
    }

    @Override
    public void onTimersLoaded(ArrayList<Timer> timers) {
        if (timersAdapter == null) {
            timersAdapter = new TimersAdapter(context, timers, this);
            timersListView.setAdapter(timersAdapter);
        }
        else {
            timersAdapter.updateList(timers);
        }
    }

    @Override
    public void onTimersLoadFailed() {
        showToast(getString(R.string.error_please_try_again), Toast.LENGTH_LONG);
    }

    @Override
    public void onTimerEditorSave(String title, String description, int hours, int minutes, int seconds, boolean repeated) {
        int total = hours + minutes + seconds;
        if (total < 1) {
            showToast(getString(R.string.timer_invalid_interval), Toast.LENGTH_SHORT);
            return;
        }
        else if (Utils.isNullOrEmpty(title)) {
            showToast(getString(R.string.timer_empty_title), Toast.LENGTH_SHORT);
            return;
        }
        Timer timer = new Timer();
        timer.id = timerEditor.getTag() == null ? 0 : (int) timerEditor.getTag();
        timer.title = title;
        timer.description = description;
        timer.interval = hours * 3600 + minutes * 60 + seconds;
        timer.repeat = repeated;
        timerEditor.clear();
        AppDb.getInstance(context).insertOrUpdateTimer(timer);
        EventBus.getDefault().post(new ReloadTimersEvent());
        onTimerEditorCancel();
    }

    @Override
    public void onTimerEditorCancel() {
        view.findViewById(R.id.timer_editor).setVisibility(View.GONE);
        timersListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void cancelRunningTasks() {

    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void onStartClick(int id) {
        if (timersAdapter == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return;
        }
        Timer timer = timersAdapter.getById(id);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                .setSmallIcon(R.drawable.baseline_timer_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.osrscompanion4))
                .setContentTitle(timer.title)
                .setContentText(timer.description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify((int)System.currentTimeMillis(), mBuilder.build());
    }

    @Override
    public void onEditClick(int id) {
        if (timersAdapter == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return;
        }
        Timer timer = timersAdapter.getById(id);
        timerEditor.setContent(timer);
        openAddTimerView();
    }

    @Override
    public void onDeleteClick(int id) {
        AppDb.getInstance(context).deleteTimer(id);
        EventBus.getDefault().post(new ReloadTimersEvent());
    }
}