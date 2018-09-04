package com.dennyy.osrscompanion.models.General;

import com.dennyy.osrscompanion.enums.SkillType;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;

import java.util.LinkedHashMap;

public class PlayerStats extends LinkedHashMap<SkillType, Skill> {
    private int totalLevel;
    private long totalExp;

    /**
     * Generates skills object from official osrs hiscores api
     *
     * @param stats complete string of the official osrs hiscores api
     */
    public PlayerStats(String stats) {
        super(33);
        if (Utils.isNullOrEmpty(stats) || stats.split("\n").length < Constants.REQUIRED_STATS_LENGTH) {
            return;
        }
        String[] statsArray = stats.split("\n");
        for (int i = 0; i < statsArray.length; i++) {
            String[] line = statsArray[i].split(",");
            SkillType skillType = SkillType.fromId(i);
            if (line.length == 3) {
                int rank = Integer.parseInt(line[0]);
                int level = Integer.parseInt(line[1]);
                long exp = Long.parseLong(line[2]);
                if (skillType != SkillType.OVERALL) {
                    totalLevel += level;
                    totalExp += Math.max(0, exp);
                }
                put(skillType, new Skill(SkillType.fromId(i), rank, level, exp));
            }
            // minigames
            else if (line.length == 2) {
                int rank = Integer.parseInt(line[0]);
                int score = Integer.parseInt(line[1]);
                put(skillType, new Skill(SkillType.fromId(i), rank, score));
            }
        }
    }

    public Skill getSkill(SkillType skillType) {
        Skill skill = get(skillType);
        if (skill != null) {
            return skill;
        }
        else if (skillType.isMinigame()) {
            return new Skill(skillType, -1, -1);
        }
        else if (skillType == SkillType.HITPOINTS) {
            return new Skill(skillType, -1, 10, RsUtils.exp(10));
        }
        return new Skill(skillType, -1, 1, 0);
    }

    public int getLevel(SkillType skillType) {
        return getSkill(skillType).getLevel();
    }

    public long getExp(SkillType skillType) {
        return getSkill(skillType).getExp();
    }

    /**
     * Calculates combat level on the fly based on the stats in the map
     *
     * @return Default combat of 3 if stats are not found, else the combat
     */
    public Combat getCombat() {
        if (isUnranked()) {
            return Combat.getDefault();
        }
        Combat combat = new Combat(
                getLevel(SkillType.ATTACK),
                getLevel(SkillType.DEFENCE),
                getLevel(SkillType.STRENGTH),
                getLevel(SkillType.HITPOINTS),
                getLevel(SkillType.RANGED),
                getLevel(SkillType.PRAYER),
                getLevel(SkillType.MAGIC));
        return combat;
    }

    public long getCombatExp() {
        long exp = getExp(SkillType.ATTACK) +
                getExp(SkillType.DEFENCE) +
                getExp(SkillType.STRENGTH) +
                getExp(SkillType.HITPOINTS) +
                getExp(SkillType.RANGED) +
                getExp(SkillType.PRAYER) +
                getExp(SkillType.MAGIC);
        return exp;
    }

    public int getTotalLevel() {
        return Math.max(totalLevel, Constants.MIN_TOTAL_LEVEL);
    }

    public long getTotalExp() {
        return totalExp;
    }

    public boolean isUnranked() {
        return isEmpty();
    }
}
