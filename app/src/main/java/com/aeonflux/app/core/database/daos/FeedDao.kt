package com.aeonflux.app.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aeonflux.app.core.database.entities.FeedEntity
import com.aeonflux.app.core.database.entities.FeedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    fun getAllFeedsFlow(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feeds WHERE id = :feedId")
    suspend fun getFeedById(feedId: String): FeedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: FeedEntity)

    @Update
    suspend fun updateFeed(feed: FeedEntity)

    @Delete
    suspend fun deleteFeed(feed: FeedEntity)

    // Feed Items queries
    @Query("SELECT * FROM feed_items WHERE feed_id = :feedId ORDER BY published_at DESC")
    fun getItemsForFeedFlow(feedId: String): Flow<List<FeedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FeedItemEntity>)

    @Query("UPDATE feed_items SET is_read = :isRead WHERE id = :itemId")
    suspend fun updateReadStatus(itemId: String, isRead: Int)

    @Query("UPDATE feed_items SET playback_position_ms = :positionMs WHERE id = :itemId")
    suspend fun updatePlaybackPosition(itemId: String, positionMs: Long)
}
