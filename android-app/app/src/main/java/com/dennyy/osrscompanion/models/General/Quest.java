package com.dennyy.osrscompanion.models.General;

import com.dennyy.osrscompanion.enums.QuestDifficulty;
import com.dennyy.osrscompanion.enums.QuestLength;
import com.dennyy.osrscompanion.helpers.Utils;

public class Quest {
    public final String name;
    public final String url;
    public final String runeHqUrl;
    public final QuestDifficulty questDifficulty;
    public final QuestLength questLength;
    public final boolean isMembers;
    public final int questPoints;

    public Quest(String name, String url, String runeHqUrl, int questDifficulty, int questLength, boolean isMembers, int questPoints) {
        this.name = name;
        this.url = url;
        this.runeHqUrl = runeHqUrl;
        this.questDifficulty = QuestDifficulty.fromValue(questDifficulty);
        this.questLength = QuestLength.fromValue(questLength);
        this.isMembers = isMembers;
        this.questPoints = questPoints;
    }

    public boolean hasRuneHqUrl() {
        return !Utils.isNullOrEmpty(runeHqUrl);
    }
}
