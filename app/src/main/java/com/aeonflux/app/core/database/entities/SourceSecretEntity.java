/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Source Secret entity for storing encrypted credentials.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class storing ciphered secrets for sources.
 */
@Entity(
    tableName = "source_secrets",
    primaryKeys = {"source_id", "secret_key"},
    foreignKeys = {
        @ForeignKey(
            entity = SourceEntity.class,
            parentColumns = "id",
            childColumns = "source_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"source_id"})
    }
)
public class SourceSecretEntity {

    @ColumnInfo(name = "source_id")
    @NonNull
    public String sourceId;

    @ColumnInfo(name = "secret_key")
    @NonNull
    public String secretKey;

    @ColumnInfo(name = "encrypted_value")
    @NonNull
    public String encryptedValue;

    @NonNull
    public String iv;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public SourceSecretEntity() {
        this.sourceId = "";
        this.secretKey = "";
        this.encryptedValue = "";
        this.iv = "";
    }

    @androidx.room.Ignore
    public SourceSecretEntity(@NonNull String sourceId,
                              @NonNull String secretKey,
                              @NonNull String encryptedValue,
                              @NonNull String iv) {
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.secretKey = Objects.requireNonNull(secretKey, "secretKey must not be null");
        this.encryptedValue = Objects.requireNonNull(encryptedValue, "encryptedValue must not be null");
        this.iv = Objects.requireNonNull(iv, "iv must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceSecretEntity that = (SourceSecretEntity) o;
        return Objects.equals(sourceId, that.sourceId) &&
               Objects.equals(secretKey, that.secretKey) &&
               Objects.equals(encryptedValue, that.encryptedValue) &&
               Objects.equals(iv, that.iv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, secretKey, encryptedValue, iv);
    }
}
