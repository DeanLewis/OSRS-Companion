package com.dennyy.osrscompanion.enums;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;

public enum SkillType {
    OVERALL(0, R.drawable.stats_icon),
    ATTACK(1, R.drawable.attack_icon),
    DEFENCE(2, R.drawable.defence_icon),
    STRENGTH(3, R.drawable.strength_icon),
    HITPOINTS(4, R.drawable.hitpoints_icon),
    RANGED(5, R.drawable.ranged_icon),
    PRAYER(6, R.drawable.prayer_icon),
    MAGIC(7, R.drawable.magic_icon),
    COOKING(8, R.drawable.cooking_icon),
    WOODCUTTING(9, R.drawable.woodcutting_icon),
    FLETCHING(10, R.drawable.fletching_icon),
    FISHING(11, R.drawable.fishing_icon),
    FIREMAKING(12, R.drawable.firemaking_icon),
    CRAFTING(13, R.drawable.crafting_icon),
    SMITHING(14, R.drawable.smithing_icon),
    MINING(15, R.drawable.mining_icon),
    HERBLORE(16, R.drawable.herblore_icon),
    AGILITY(17, R.drawable.agility_icon),
    THIEVING(18, R.drawable.thieving_icon),
    SLAYER(19, R.drawable.slayer_icon),
    FARMING(20, R.drawable.farming_icon),
    RUNECRAFTING(21, R.drawable.runecrafting_icon),
    HUNTER(22, R.drawable.hunter_icon),
    CONSTRUCTION(23, R.drawable.construction_icon),
    CLUE_EASY(24, R.drawable.clue_scroll_easy),
    CLUE_MED(25, R.drawable.clue_scroll_med),
    CLUE_TOTAL(26, R.drawable.clue_scroll),
    BHR(27, R.drawable.bounty_hunter_rogue),
    BH(28, R.drawable.bounty_hunter),
    CLUE_HARD(29, R.drawable.clue_scroll_hard),
    LMS(30, R.drawable.lms),
    CLUE_ELITE(31, R.drawable.clue_scroll_elite),
    CLUE_MASTER(32, R.drawable.clue_scroll_master);

    private int id;
    private int drawable;

    SkillType(int id, int drawable) {
        this.id = id;
        this.drawable = drawable;
    }

    public int getId() {
        return id;
    }

    public int getDrawable() {
        return drawable;
    }

    public boolean isMinigame() { return getId() >= Constants.REQUIRED_STATS_LENGTH;}

    public static SkillType fromId(int id) {
        for (SkillType skillType : SkillType.values()) {
            if (skillType.getId() == id) {
                return skillType;
            }
        }
        throw new IndexOutOfBoundsException(String.format("Unknown skill id: %d", id));
    }
}
