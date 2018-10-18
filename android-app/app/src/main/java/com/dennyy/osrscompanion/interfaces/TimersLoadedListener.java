package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.Timers.Timer;

import java.util.ArrayList;

public interface TimersLoadedListener {
    void onTimersLoaded(ArrayList<Timer> timers);

    void onTimersLoadFailed();
}
