package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.General.Quest;

import java.util.ArrayList;

public interface QuestsLoadedListener {
    void onQuestsLoaded(ArrayList<Quest> loadedQuests);

    void onQuestsLoadError();
}
