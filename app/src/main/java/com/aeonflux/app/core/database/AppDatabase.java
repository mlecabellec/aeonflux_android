package com.aeonflux.app.core.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.aeonflux.app.core.database.daos.FeedDao;
import com.aeonflux.app.core.database.entities.FeedEntity;
import com.aeonflux.app.core.database.entities.FeedItemEntity;

@Database(
    entities = {FeedEntity.class, FeedItemEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FeedDao feedDao();
}
