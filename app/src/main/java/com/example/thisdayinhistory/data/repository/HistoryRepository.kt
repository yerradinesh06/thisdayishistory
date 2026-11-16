package com.example.thisdayinhistory.data.repository

import com.example.thisdayinhistory.data.api.WikipediaApiService
import com.example.thisdayinhistory.data.db.BookmarkDao
import com.example.thisdayinhistory.data.db.BookmarkEntity
import com.example.thisdayinhistory.data.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(
    private val apiService: WikipediaApiService = WikipediaApiService.instance,
    private val bookmarkDao: BookmarkDao
) {
    suspend fun getHistoryItems(type: String, month: String, day: String): List<HistoryItem> {
        val response = apiService.getOnThisDay(type, month, day)
        val rawList = when (type) {
            "events" -> response.events ?: response.selected ?: emptyList()
            "births" -> response.births ?: emptyList()
            "deaths" -> response.deaths ?: emptyList()
            else -> response.events ?: emptyList()
        }

        return rawList.map { apiItem ->
            HistoryItem(
                text = apiItem.text,
                year = apiItem.year,
                type = type,
                thumbnailUrl = apiItem.thumbnailUrl,
                wikiUrl = apiItem.wikiUrl
            )
        }
    }

    fun getAllBookmarks(): Flow<List<HistoryItem>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { entity ->
                HistoryItem(
                    text = entity.text,
                    year = entity.year,
                    type = entity.type,
                    thumbnailUrl = entity.thumbnailUrl,
                    wikiUrl = entity.wikiUrl,
                    isBookmarked = true
                )
            }
        }
    }

    suspend fun toggleBookmark(item: HistoryItem, dateString: String) {
        val isBookmarked = bookmarkDao.isBookmarkedSync(item.text, item.year)
        if (isBookmarked) {
            bookmarkDao.deleteBookmarkByTextAndYear(item.text, item.year)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    text = item.text,
                    year = item.year,
                    type = item.type,
                    dateString = dateString,
                    thumbnailUrl = item.thumbnailUrl,
                    wikiUrl = item.wikiUrl
                )
            )
        }
    }

    suspend fun isBookmarked(text: String, year: Int): Boolean {
        return bookmarkDao.isBookmarkedSync(text, year)
    }
}
