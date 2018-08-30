package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.AchievementDiary.DiariesMap;

public interface DiariesLoadedCallback {
    void onDiariesLoaded(DiariesMap diariesMap);

    void onDiariesLoadError();
}
