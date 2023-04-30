package com.jentsch.voicegpt.db;

import android.content.Context;

import androidx.room.Room;

public class DbSingleton {

    private static DbSingleton instance = null;
    private static final String DB_NAME = "database";

    private AppDatabase db;

    private DbSingleton (Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class,
                        DB_NAME)
                .allowMainThreadQueries()
                .build();
    }

    public AppDatabase getAppDatabase()
    {
        return db;
    }

    public static synchronized DbSingleton instance(Context context) {
        if (instance == null) {
            instance = new DbSingleton(context);
        }
        return instance;
    }
}