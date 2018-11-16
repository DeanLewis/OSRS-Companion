package com.dennyy.osrscompanion.models.General;

import com.dennyy.osrscompanion.enums.SkillType;
import com.dennyy.osrscompanion.helpers.RsUtils;

public class Skill {
    private SkillType skillType;
    private int rank;
    private int level;
    private long exp;
    private int score;

    Skill(SkillType skillType, int rank, int level, long exp) {
        this.skillType = skillType;
        this.rank = rank;
        this.level = level;
        this.exp = exp;
    }

    Skill(SkillType skillType, int rank, int score) {
        this.skillType = skillType;
        this.rank = rank;
        this.score = score;
    }

    public int getId() {
        return skillType.id;
    }

    public SkillType getType() {
        return skillType;
    }

    public int getRank() {
        return rank;
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public int getScore() {
        return score;
    }

    public boolean isOverall() {
        return skillType == SkillType.OVERALL;
    }

    public boolean isMinigame() {
        return skillType.isMinigame();
    }

    public static Skill getDefault(SkillType skillType) {
        if (skillType.isMinigame()) {
            return new Skill(skillType, -1, -1);
        }
        else if (skillType == SkillType.HITPOINTS) {
            return new Skill(skillType, -1, 10, RsUtils.exp(10));
        }
        return new Skill(skillType, -1, 1, 0);
    }
}