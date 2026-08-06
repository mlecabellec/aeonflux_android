/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - AppDatabase class with seed initialization.
 */
package com.aeonflux.app.core.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.aeonflux.app.core.database.daos.ArticleDao;
import com.aeonflux.app.core.database.daos.KeywordDao;
import com.aeonflux.app.core.database.daos.LabelDao;
import com.aeonflux.app.core.database.daos.PropertyDao;
import com.aeonflux.app.core.database.daos.SecretDao;
import com.aeonflux.app.core.database.daos.SettingDao;
import com.aeonflux.app.core.database.daos.SourceDao;
import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.ArticleKeywordCrossRef;
import com.aeonflux.app.core.database.entities.ArticleLabelCrossRef;
import com.aeonflux.app.core.database.entities.KeywordEntity;
import com.aeonflux.app.core.database.entities.LabelEntity;
import com.aeonflux.app.core.database.entities.PropertyEntity;
import com.aeonflux.app.core.database.entities.SettingEntity;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.database.entities.SourceSecretEntity;

import java.util.concurrent.Executors;

/**
 * [TSK-20260804-003.3] Central Room database class for AeonFlux.
 */
@Database(
    entities = {
        SourceEntity.class,
        ArticleEntity.class,
        KeywordEntity.class,
        LabelEntity.class,
        ArticleKeywordCrossRef.class,
        ArticleLabelCrossRef.class,
        PropertyEntity.class,
        SourceSecretEntity.class,
        SettingEntity.class
    },
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SourceDao sourceDao();
    public abstract ArticleDao articleDao();
    public abstract PropertyDao propertyDao();
    public abstract SecretDao secretDao();
    public abstract SettingDao settingDao();
    public abstract KeywordDao keywordDao();
    public abstract LabelDao labelDao();

    /**
     * [TSK-20260804-003.3] Database Seeding Callback executed at first launch / installation.
     */
    public static final RoomDatabase.Callback SEED_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Execute initial database seeding asynchronously
            Executors.newSingleThreadExecutor().execute(() -> {
                // Default settings seed statements
                db.execSQL("INSERT OR REPLACE INTO settings (`key`, `value`) VALUES ('app_version', '1.0.0')");
                db.execSQL("INSERT OR REPLACE INTO settings (`key`, `value`) VALUES ('theme', 'slate')");
                db.execSQL("INSERT OR REPLACE INTO settings (`key`, `value`) VALUES ('auto_refresh_minutes', '60')");

                // Default labels seed statements
                db.execSQL("INSERT OR REPLACE INTO labels (`id`, `name`, `color`) VALUES ('lbl_read_later', 'Read Later', '#3B82F6')");
                db.execSQL("INSERT OR REPLACE INTO labels (`id`, `name`, `color`) VALUES ('lbl_ai_summary', 'AI Summary', '#10B981')");
                db.execSQL("INSERT OR REPLACE INTO labels (`id`, `name`, `color`) VALUES ('lbl_favorite', 'Favorite', '#EF4444')");
            });
        }
    };
}
