package com.example.thisdayinhistory.data.api

import com.google.gson.annotations.SerializedName

data class OnThisDayResponse(
    val events: List<HistoryApiItem>? = null,
    val births: List<HistoryApiItem>? = null,
    val deaths: List<HistoryApiItem>? = null,
    val selected: List<HistoryApiItem>? = null
)

data class HistoryApiItem(
    val text: String = "",
    val year: Int = 0,
    val pages: List<PageApiItem>? = null
) {
    val thumbnailUrl: String?
        get() = pages?.firstOrNull { !it.thumbnail?.source.isNullOrBlank() }?.thumbnail?.source

    val wikiUrl: String?
        get() {
            val firstPage = pages?.firstOrNull { it.contentUrls?.desktop?.page != null || it.contentUrls?.mobile?.page != null }
            return firstPage?.contentUrls?.desktop?.page
                ?: firstPage?.contentUrls?.mobile?.page
                ?: pages?.firstOrNull()?.contentUrls?.desktop?.page
        }
}

data class PageApiItem(
    val title: String? = null,
    val displaytitle: String? = null,
    val extract: String? = null,
    val thumbnail: ThumbnailItem? = null,
    @SerializedName("content_urls")
    val contentUrls: ContentUrlsItem? = null
)

data class ThumbnailItem(
    val source: String? = null,
    val width: Int = 0,
    val height: Int = 0
)

data class ContentUrlsItem(
    val desktop: PageUrlItem? = null,
    val mobile: PageUrlItem? = null
)

data class PageUrlItem(
    val page: String? = null
)
