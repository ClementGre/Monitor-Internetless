package fr.themsou.monitorinternetless.ui.numbers;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Number.class}, version = 1)
public abstract class NumberDatabase extends RoomDatabase {
    public abstract NumberDao daoAccess();
}

