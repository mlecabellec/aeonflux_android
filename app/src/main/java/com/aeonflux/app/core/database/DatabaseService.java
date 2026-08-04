/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.4 - Centralized Data Management Service with CRUD hooks.
 */
package com.aeonflux.app.core.database;

import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.KeywordEntity;
import com.aeonflux.app.core.database.entities.LabelEntity;
import com.aeonflux.app.core.database.entities.PropertyEntity;
import com.aeonflux.app.core.database.entities.SettingEntity;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.database.entities.SourceSecretEntity;
import com.aeonflux.app.core.security.CryptographyManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * [TSK-20260804-003.4] Centralized Service for Database Management, CRUD Operations,
 * Encrypted Secret Storage, Dynamic Properties, and CRUD Lifecycle Observer Hooks.
 */
@Singleton
public class DatabaseService {

    private final AppDatabase appDatabase;
    private final CryptographyManager cryptoManager;
    private final Map<Class<?>, CopyOnWriteArrayList<OnEntityChangeListener<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * [TSK-20260804-003.4] Observer interface for entity CRUD lifecycle events.
     */
    public interface OnEntityChangeListener<T> {
        void onCreated(T entity);
        void onUpdated(T entity);
        void onDeleted(T entity);
    }

    @Inject
    public DatabaseService(AppDatabase appDatabase, CryptographyManager cryptoManager) {
        this.appDatabase = Objects.requireNonNull(appDatabase, "appDatabase must not be null");
        this.cryptoManager = Objects.requireNonNull(cryptoManager, "cryptoManager must not be null");
    }

    /* TSK-20260804-003.4 - Register a CRUD observer listener */
    @SuppressWarnings("unchecked")
    public <T> void registerListener(Class<T> clazz, OnEntityChangeListener<T> listener) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        listeners.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>()).add((OnEntityChangeListener<?>) listener);
    }

    /* TSK-20260804-003.4 - Unregister a CRUD observer listener */
    public <T> void unregisterListener(Class<T> clazz, OnEntityChangeListener<T> listener) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        CopyOnWriteArrayList<OnEntityChangeListener<?>> list = listeners.get(clazz);
        if (list != null) {
            list.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notifyCreated(T entity) {
        if (entity == null) return;
        CopyOnWriteArrayList<OnEntityChangeListener<?>> list = listeners.get(entity.getClass());
        if (list != null) {
            for (OnEntityChangeListener<?> l : list) {
                ((OnEntityChangeListener<T>) l).onCreated(entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notifyUpdated(T entity) {
        if (entity == null) return;
        CopyOnWriteArrayList<OnEntityChangeListener<?>> list = listeners.get(entity.getClass());
        if (list != null) {
            for (OnEntityChangeListener<?> l : list) {
                ((OnEntityChangeListener<T>) l).onUpdated(entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notifyDeleted(T entity) {
        if (entity == null) return;
        CopyOnWriteArrayList<OnEntityChangeListener<?>> list = listeners.get(entity.getClass());
        if (list != null) {
            for (OnEntityChangeListener<?> l : list) {
                ((OnEntityChangeListener<T>) l).onDeleted(entity);
            }
        }
    }

    // --- TRANSACTION MANAGEMENT ---

    public void runInTransaction(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        appDatabase.runInTransaction(runnable);
    }

    // --- SOURCES CRUD ---

    public void insertSource(SourceEntity source) {
        Objects.requireNonNull(source, "source must not be null");
        appDatabase.sourceDao().insertSource(source);
        notifyCreated(source);
    }

    public void updateSource(SourceEntity source) {
        Objects.requireNonNull(source, "source must not be null");
        appDatabase.sourceDao().updateSource(source);
        notifyUpdated(source);
    }

    public void deleteSource(SourceEntity source) {
        Objects.requireNonNull(source, "source must not be null");
        appDatabase.sourceDao().deleteSource(source);
        notifyDeleted(source);
    }

    public SourceEntity getSourceById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return appDatabase.sourceDao().getSourceById(id);
    }

    public List<SourceEntity> getAllSources() {
        return appDatabase.sourceDao().getAllSources();
    }

    // --- ARTICLES CRUD ---

    public void insertArticle(ArticleEntity article) {
        Objects.requireNonNull(article, "article must not be null");
        appDatabase.articleDao().insertArticle(article);
        notifyCreated(article);
    }

    public void updateArticle(ArticleEntity article) {
        Objects.requireNonNull(article, "article must not be null");
        appDatabase.articleDao().updateArticle(article);
        notifyUpdated(article);
    }

    public void deleteArticle(ArticleEntity article) {
        Objects.requireNonNull(article, "article must not be null");
        appDatabase.articleDao().deleteArticle(article);
        notifyDeleted(article);
    }

    public ArticleEntity getArticleById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return appDatabase.articleDao().getArticleById(id);
    }

    public List<ArticleEntity> getArticlesForSource(String sourceId) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        return appDatabase.articleDao().getArticlesForSource(sourceId);
    }

    // --- KEYWORDS & LABELS CRUD ---

    public void insertKeyword(KeywordEntity keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        appDatabase.keywordDao().insertKeyword(keyword);
        notifyCreated(keyword);
    }

    public void insertLabel(LabelEntity label) {
        Objects.requireNonNull(label, "label must not be null");
        appDatabase.labelDao().insertLabel(label);
        notifyCreated(label);
    }

    public List<LabelEntity> getAllLabels() {
        return appDatabase.labelDao().getAllLabels();
    }

    // --- DYNAMIC PROPERTIES (EAV) ---

    public void setProperty(String entityType, String entityId, String key, Object value, String dataType) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(dataType, "dataType must not be null");

        String strValue = (value != null) ? String.valueOf(value) : null;
        PropertyEntity prop = new PropertyEntity(entityType, entityId, key, strValue, dataType);
        appDatabase.propertyDao().insertProperty(prop);
        notifyCreated(prop);
    }

    public String getPropertyString(String entityType, String entityId, String key) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        Objects.requireNonNull(key, "key must not be null");

        PropertyEntity prop = appDatabase.propertyDao().getProperty(entityType, entityId, key);
        return (prop != null) ? prop.propertyValue : null;
    }

    public int getPropertyInt(String entityType, String entityId, String key, int defaultValue) {
        String val = getPropertyString(entityType, entityId, key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getPropertyBoolean(String entityType, String entityId, String key, boolean defaultValue) {
        String val = getPropertyString(entityType, entityId, key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public double getPropertyDouble(String entityType, String entityId, String key, double defaultValue) {
        String val = getPropertyString(entityType, entityId, key);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // --- CIPHERED SECRETS MANAGEMENT ---

    public void setSecret(String sourceId, String secretKey, String rawSecret) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        Objects.requireNonNull(rawSecret, "rawSecret must not be null");

        CryptographyManager.EncryptedData encryptedData = cryptoManager.encrypt(rawSecret);
        SourceSecretEntity secretEntity = new SourceSecretEntity(
            sourceId,
            secretKey,
            encryptedData.getBase64Ciphertext(),
            encryptedData.getBase64Iv()
        );
        appDatabase.secretDao().insertSecret(secretEntity);
        notifyCreated(secretEntity);
    }

    public String getSecret(String sourceId, String secretKey) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");

        SourceSecretEntity secretEntity = appDatabase.secretDao().getSecret(sourceId, secretKey);
        if (secretEntity == null) {
            return null;
        }
        return cryptoManager.decrypt(secretEntity.encryptedValue, secretEntity.iv);
    }

    public void deleteSecret(String sourceId, String secretKey) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        appDatabase.secretDao().deleteSecret(sourceId, secretKey);
    }

    // --- SETTINGS MANAGEMENT ---

    public void setSetting(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        SettingEntity setting = new SettingEntity(key, value);
        appDatabase.settingDao().insertSetting(setting);
        notifyCreated(setting);
    }

    public String getSetting(String key) {
        Objects.requireNonNull(key, "key must not be null");
        SettingEntity setting = appDatabase.settingDao().getSetting(key);
        return (setting != null) ? setting.value : null;
    }
}
