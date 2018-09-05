package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;

public interface FairyRingsLoadedCallback {
    void onFairyRingsLoaded(ArrayList<FairyRing> fairyRings);

    void onFairyRingsLoadError();
}
