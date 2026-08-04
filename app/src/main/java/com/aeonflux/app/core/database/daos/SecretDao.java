/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Secret DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aeonflux.app.core.database.entities.SourceSecretEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for ciphered secrets.
 */
@Dao
public interface SecretDao {

    @Query("SELECT * FROM source_secrets WHERE source_id = :sourceId")
    List<SourceSecretEntity> getSecretsForSource(String sourceId);

    @Query("SELECT * FROM source_secrets WHERE source_id = :sourceId AND secret_key = :secretKey")
    SourceSecretEntity getSecret(String sourceId, String secretKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSecret(SourceSecretEntity secret);

    @Delete
    void deleteSecret(SourceSecretEntity secret);

    @Query("DELETE FROM source_secrets WHERE source_id = :sourceId AND secret_key = :secretKey")
    void deleteSecret(String sourceId, String secretKey);

    @Query("DELETE FROM source_secrets WHERE source_id = :sourceId")
    void deleteAllSecretsForSource(String sourceId);
}
