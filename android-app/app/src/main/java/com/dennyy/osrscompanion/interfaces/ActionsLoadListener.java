package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.General.Action;

import java.util.ArrayList;

public interface ActionsLoadListener {
    void onActionsLoaded(ArrayList<Action> actions);

    void onActionsLoadFailed();
}