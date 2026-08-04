/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Label DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aeonflux.app.core.database.entities.ArticleLabelCrossRef;
import com.aeonflux.app.core.database.entities.LabelEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for Labels and Article-Label cross-references.
 */
@Dao
public interface LabelDao {

    @Query("SELECT * FROM labels ORDER BY name ASC")
    List<LabelEntity> getAllLabels();

    @Query("SELECT * FROM labels ORDER BY name ASC")
    LiveData<List<LabelEntity>> getAllLabelsLiveData();

    @Query("SELECT * FROM labels WHERE id = :id")
    LabelEntity getLabelById(String id);

    @Query("SELECT * FROM labels WHERE name = :name")
    LabelEntity getLabelByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLabel(LabelEntity label);

    @Update
    void updateLabel(LabelEntity label);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticleLabelCrossRef(ArticleLabelCrossRef crossRef);

    @Query("SELECT l.* FROM labels l INNER JOIN article_labels al ON l.id = al.label_id WHERE al.article_id = :articleId")
    List<LabelEntity> getLabelsForArticle(String articleId);

    @Delete
    void deleteLabel(LabelEntity label);

    @Query("DELETE FROM article_labels WHERE article_id = :articleId AND label_id = :labelId")
    void deleteArticleLabelCrossRef(String articleId, String labelId);

    @Query("DELETE FROM article_labels WHERE article_id = :articleId")
    void deleteAllLabelsForArticle(String articleId);
}
