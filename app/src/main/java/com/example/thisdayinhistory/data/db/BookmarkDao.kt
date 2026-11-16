package com.example.thisdayinhistory.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE text = :text AND year = :year")
    suspend fun deleteBookmarkByTextAndYear(text: String, year: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE text = :text AND year = :year)")
    fun isBookmarked(text: String, year: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE text = :text AND year = :year)")
    suspend fun isBookmarkedSync(text: String, year: Int): Boolean
}
