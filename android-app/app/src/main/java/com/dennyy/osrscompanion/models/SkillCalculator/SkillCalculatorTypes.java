package com.dennyy.osrscompanion.models.SkillCalculator;

import com.dennyy.osrscompanion.enums.SkillType;

import java.util.LinkedHashMap;

public class SkillCalculatorTypes extends LinkedHashMap<SkillType, String> {

    public SkillCalculatorTypes() {
        super(18);
        put(SkillType.PRAYER, "skill_prayer.json");
        put(SkillType.MAGIC, "skill_magic.json");
        put(SkillType.COOKING, "skill_cooking.json");
        put(SkillType.WOODCUTTING, "skill_woodcutting.json");
        put(SkillType.FLETCHING, "skill_fletching.json");
        put(SkillType.FISHING, "skill_fishing.json");
        put(SkillType.FIREMAKING, "skill_firemaking.json");
        put(SkillType.CRAFTING, "skill_crafting.json");
        put(SkillType.SMITHING, "skill_smithing.json");
        put(SkillType.MINING, "skill_mining.json");
        put(SkillType.HERBLORE, "skill_herblore.json");
        put(SkillType.AGILITY, "skill_agility.json");
        put(SkillType.THIEVING, "skill_thieving.json");
        put(SkillType.SLAYER, "skill_slayer.json");
        put(SkillType.FARMING, "skill_farming.json");
        put(SkillType.RUNECRAFTING, "skill_runecraft.json");
        put(SkillType.HUNTER, "skill_hunter.json");
        put(SkillType.CONSTRUCTION, "skill_construction.json");
    }
}