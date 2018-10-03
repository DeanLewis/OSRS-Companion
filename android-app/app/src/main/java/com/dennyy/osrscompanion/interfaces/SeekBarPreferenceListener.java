package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.customviews.SeekBarPreference;

public interface SeekBarPreferenceListener {
    void onSeekBarValueSet(SeekBarPreference preference, String key, int value);

    void onSeekBarCancel(SeekBarPreference preference, String key);
}
