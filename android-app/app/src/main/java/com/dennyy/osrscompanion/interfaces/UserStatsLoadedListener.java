package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.Hiscores.UserStats;

public interface UserStatsLoadedListener {
    void onUserStatsLoaded(UserStats userStats);

    void onUserStatsLoadFailed();
}