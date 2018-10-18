package com.dennyy.osrscompanion.interfaces;

public interface TimerEditorListener {
    void onTimerEditorSave(String title, String description, int hours, int minutes, int seconds, boolean repeated);

    void onTimerEditorCancel();
}
