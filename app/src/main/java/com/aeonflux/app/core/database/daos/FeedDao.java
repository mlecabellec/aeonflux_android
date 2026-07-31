package com.aeonflux.app.core.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.aeonflux.app.core.database.entities.FeedEntity;
import com.aeonflux.app.core.database.entities.FeedItemEntity;

import java.util.List;

@Dao
public interface FeedDao {
    @Query("SELECT * FROM feeds")
    LiveData<List<FeedEntity>> getAllFeedsLiveData();

    @Query("SELECT * FROM feeds WHERE id = :feedId")
    FeedEntity getFeedById(String feedId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeed(FeedEntity feed);

    @Update
    void updateFeed(FeedEntity feed);

    @Delete
    void deleteFeed(FeedEntity feed);

    // Feed Items queries
    @Query("SELECT * FROM feed_items WHERE feed_id = :feedId ORDER BY published_at DESC")
    LiveData<List<FeedItemEntity>> getItemsForFeedLiveData(String feedId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(List<FeedItemEntity> items);

    @Query("UPDATE feed_items SET is_read = :isRead WHERE id = :itemId")
    void updateReadStatus(String itemId, int isRead);

    @Query("UPDATE feed_items SET playback_position_ms = :positionMs WHERE id = :itemId")
    void updatePlaybackPosition(String itemId, long positionMs);
}
