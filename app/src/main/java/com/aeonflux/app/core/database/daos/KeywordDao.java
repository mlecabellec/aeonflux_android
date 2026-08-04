/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Keyword DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aeonflux.app.core.database.entities.ArticleKeywordCrossRef;
import com.aeonflux.app.core.database.entities.KeywordEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for Keywords and Article-Keyword cross-references.
 */
@Dao
public interface KeywordDao {

    @Query("SELECT * FROM keywords ORDER BY keyword ASC")
    List<KeywordEntity> getAllKeywords();

    @Query("SELECT * FROM keywords WHERE id = :id")
    KeywordEntity getKeywordById(String id);

    @Query("SELECT * FROM keywords WHERE keyword = :keyword")
    KeywordEntity getKeywordByValue(String keyword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKeyword(KeywordEntity keyword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticleKeywordCrossRef(ArticleKeywordCrossRef crossRef);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticleKeywordCrossRefs(List<ArticleKeywordCrossRef> crossRefs);

    @Query("SELECT k.* FROM keywords k INNER JOIN article_keywords ak ON k.id = ak.keyword_id WHERE ak.article_id = :articleId")
    List<KeywordEntity> getKeywordsForArticle(String articleId);

    @Delete
    void deleteKeyword(KeywordEntity keyword);

    @Query("DELETE FROM article_keywords WHERE article_id = :articleId AND keyword_id = :keywordId")
    void deleteArticleKeywordCrossRef(String articleId, String keywordId);

    @Query("DELETE FROM article_keywords WHERE article_id = :articleId")
    void deleteAllKeywordsForArticle(String articleId);
}
