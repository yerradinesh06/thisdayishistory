package com.example.thisdayinhistory.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val year: Int,
    val type: String,
    val dateString: String,
    val thumbnailUrl: String?,
    val wikiUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)
