package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.ReloadTimerSource;
import com.dennyy.osrscompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.osrscompanion.viewhandlers.TimersViewHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TimersFragment extends BaseFragment {
    private TimersViewHandler timersViewHandler;

    public TimersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.timers_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.timers));
        timersViewHandler = new TimersViewHandler(getActivity(), view, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timers, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_timer) {
            timersViewHandler.openAddTimerView();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timersViewHandler.cancelRunningTasks();
    }

    @Subscribe
    public void reloadTimers(ReloadTimersEvent event) {
        if (timersViewHandler != null && event.source != ReloadTimerSource.FRAGMENT) {
            timersViewHandler.reloadTimers();
        }
    }

    @Override
    public boolean onBackClick() {
        if (timersViewHandler != null && timersViewHandler.isTimerEditorOpen()) {
            timersViewHandler.onTimerEditorCancel();
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}