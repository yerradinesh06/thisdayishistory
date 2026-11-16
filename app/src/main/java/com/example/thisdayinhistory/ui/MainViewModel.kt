package com.example.thisdayinhistory.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thisdayinhistory.auth.AuthManager
import com.example.thisdayinhistory.auth.UserProfile
import com.example.thisdayinhistory.data.db.AppDatabase
import com.example.thisdayinhistory.data.model.HistoryItem
import com.example.thisdayinhistory.data.repository.HistoryRepository
import com.example.thisdayinhistory.worker.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class HistoryTab(val title: String, val apiType: String) {
    EVENTS("Events", "events"),
    BIRTHS("Births", "births"),
    DEATHS("Deaths", "deaths"),
    BOOKMARKS("Bookmarks", "bookmarks")
}

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val items: List<HistoryItem>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = HistoryRepository(bookmarkDao = db.bookmarkDao())
    val authManager = AuthManager(application)

    val userState: StateFlow<UserProfile?> = authManager.userState

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(String.format(Locale.US, "%02d", calendar.get(Calendar.MONTH) + 1))
    private val _selectedDay = MutableStateFlow(String.format(Locale.US, "%02d", calendar.get(Calendar.DAY_OF_MONTH)))

    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _selectedTab = MutableStateFlow(HistoryTab.EVENTS)
    val selectedTab: StateFlow<HistoryTab> = _selectedTab.asStateFlow()

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _dailyReminderEnabled = MutableStateFlow(false)
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled.asStateFlow()

    val bookmarksFlow: StateFlow<List<HistoryItem>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadData()
    }

    fun selectTab(tab: HistoryTab) {
        _selectedTab.value = tab
        loadData()
    }

    fun setDate(month: String, day: String) {
        _selectedMonth.value = month
        _selectedDay.value = day
        loadData()
    }

    fun setDateFromTimestamp(timestampMillis: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestampMillis
        val m = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
        val d = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        setDate(m, d)
    }

    fun loadData() {
        val tab = _selectedTab.value
        val month = _selectedMonth.value
        val day = _selectedDay.value

        viewModelScope.launch {
            if (tab == HistoryTab.BOOKMARKS) {
                repository.getAllBookmarks().collect { bookmarks ->
                    _uiState.value = HistoryUiState.Success(bookmarks)
                }
            } else {
                _uiState.value = HistoryUiState.Loading
                try {
                    val items = repository.getHistoryItems(tab.apiType, month, day)
                    val updatedItems = items.map { item ->
                        val isBm = repository.isBookmarked(item.text, item.year)
                        item.copy(isBookmarked = isBm)
                    }
                    _uiState.value = HistoryUiState.Success(updatedItems)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.value = HistoryUiState.Error(
                        e.localizedMessage ?: "Failed to load historical data. Please check connection."
                    )
                }
            }
        }
    }

    fun toggleBookmark(item: HistoryItem) {
        val dateStr = "${_selectedMonth.value}-${_selectedDay.value}"
        viewModelScope.launch {
            repository.toggleBookmark(item, dateStr)
            val currentState = _uiState.value
            if (currentState is HistoryUiState.Success) {
                val updatedList = currentState.items.map {
                    if (it.text == item.text && it.year == item.year) {
                        it.copy(isBookmarked = !it.isBookmarked)
                    } else {
                        it
                    }
                }
                _uiState.value = HistoryUiState.Success(updatedList)
            }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        _dailyReminderEnabled.value = enabled
        if (enabled) {
            ReminderManager.scheduleDailyReminder(getApplication())
        } else {
            ReminderManager.cancelDailyReminder(getApplication())
        }
    }

    fun getFormattedDate(): String {
        return try {
            val monthInt = _selectedMonth.value.toInt() - 1
            val dayInt = _selectedDay.value.toInt()
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, monthInt)
                set(Calendar.DAY_OF_MONTH, dayInt)
            }
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(cal.time)
        } catch (e: Exception) {
            "${_selectedMonth.value}-${_selectedDay.value}"
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
