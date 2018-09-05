package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrails;

public interface TreasureTrailsLoadedCallback {
    void onTreasureTrailsLoaded(TreasureTrails treasureTrails);

    void onTreasureTrailsLoadError();
}
