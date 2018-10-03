package com.dennyy.osrscompanion.models.General;

import com.dennyy.osrscompanion.helpers.Utils;

public class Quest {
    public final String name;
    public final String url;
    public final String runeHqUrl;

    public Quest(String name, String url, String runeHqUrl) {
        this.name = name;
        this.url = url;
        this.runeHqUrl = runeHqUrl;
    }

    public boolean hasRuneHqUrl() {
        return !Utils.isNullOrEmpty(runeHqUrl);
    }
}
