/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.5 - Hilt Dependency Injection Module for Core Database.
 */
package com.aeonflux.app.core.database;

import android.content.Context;

import androidx.room.Room;

import com.aeonflux.app.core.database.daos.ArticleDao;
import com.aeonflux.app.core.database.daos.KeywordDao;
import com.aeonflux.app.core.database.daos.LabelDao;
import com.aeonflux.app.core.database.daos.PropertyDao;
import com.aeonflux.app.core.database.daos.SecretDao;
import com.aeonflux.app.core.database.daos.SettingDao;
import com.aeonflux.app.core.database.daos.SourceDao;
import com.aeonflux.app.core.security.CryptographyManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * [TSK-20260804-003.5] Hilt Module providing singleton instances for Room Database, DAOs, and Services.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "aeonflux.db")
                   .addCallback(AppDatabase.SEED_CALLBACK)
                   .addMigrations(AppDatabase.MIGRATION_3_4)
                   .fallbackToDestructiveMigration()
                   .build();
    }

    @Provides
    @Singleton
    public SourceDao provideSourceDao(AppDatabase appDatabase) {
        return appDatabase.sourceDao();
    }

    @Provides
    @Singleton
    public ArticleDao provideArticleDao(AppDatabase appDatabase) {
        return appDatabase.articleDao();
    }

    @Provides
    @Singleton
    public PropertyDao providePropertyDao(AppDatabase appDatabase) {
        return appDatabase.propertyDao();
    }

    @Provides
    @Singleton
    public SecretDao provideSecretDao(AppDatabase appDatabase) {
        return appDatabase.secretDao();
    }

    @Provides
    @Singleton
    public SettingDao provideSettingDao(AppDatabase appDatabase) {
        return appDatabase.settingDao();
    }

    @Provides
    @Singleton
    public KeywordDao provideKeywordDao(AppDatabase appDatabase) {
        return appDatabase.keywordDao();
    }

    @Provides
    @Singleton
    public LabelDao provideLabelDao(AppDatabase appDatabase) {
        return appDatabase.labelDao();
    }

    @Provides
    @Singleton
    public CryptographyManager provideCryptographyManager() {
        return new CryptographyManager();
    }

    @Provides
    @Singleton
    public DatabaseService provideDatabaseService(AppDatabase appDatabase, CryptographyManager cryptoManager) {
        return new DatabaseService(appDatabase, cryptoManager);
    }
}
