package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.Timers.Timer;

public interface AdapterTimerClickListener {
    void onStartClick(Timer timer);
    void onEditClick(Timer timer);
    void onConfirmDeleteClick(Timer timer);
}
