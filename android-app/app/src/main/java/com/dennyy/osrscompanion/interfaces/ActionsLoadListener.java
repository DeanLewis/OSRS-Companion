package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.SkillCalculator.SkillData;

public interface ActionsLoadListener {
    void onActionsLoaded(SkillData skillData);

    void onActionsLoadFailed();
}