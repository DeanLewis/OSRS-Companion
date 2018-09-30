package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.AchievementDiary.DiariesMap;

public interface DiariesLoadedListener {
    void onDiariesLoaded(DiariesMap diariesMap);

    void onDiariesLoadError();
}
