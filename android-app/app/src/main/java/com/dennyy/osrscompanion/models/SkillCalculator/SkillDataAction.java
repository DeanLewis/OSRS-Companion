package com.dennyy.osrscompanion.models.SkillCalculator;

import com.dennyy.osrscompanion.enums.SkillType;

public class SkillDataAction {
    public final SkillType skillType;
    public final String name;
    public final int level;
    public final double exp;
    public final boolean ignoreBonus;

    public SkillDataAction(SkillType skillType, String name, int level, double exp, boolean ignoreBonus) {
        this.skillType = skillType;
        this.name = name;
        this.level = level;
        this.exp = exp;
        this.ignoreBonus = ignoreBonus;
    }
}
