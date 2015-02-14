package com.twobits.polishhorseshoes.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[]{Game.class, TeamStats.class, Throw.class};

    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}
