package com.example.thisdayinhistory.data.model

data class HistoryItem(
    val text: String,
    val year: Int,
    val type: String,
    val thumbnailUrl: String? = null,
    val wikiUrl: String? = null,
    val isBookmarked: Boolean = false
)
