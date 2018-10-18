package com.dennyy.osrscompanion.models.Timers;

import com.dennyy.osrscompanion.enums.ReloadTimerSource;

public class ReloadTimersEvent {
    public final ReloadTimerSource source;

    public ReloadTimersEvent(ReloadTimerSource source) {
        this.source = source;
    }
}