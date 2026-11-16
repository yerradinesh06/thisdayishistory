package com.example.thisdayinhistory.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.thisdayinhistory.ui.components.AuthDialog
import com.example.thisdayinhistory.ui.components.CodeScannerHelper
import com.example.thisdayinhistory.ui.components.DateHeader
import com.example.thisdayinhistory.ui.components.HistoryItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val userProfile by viewModel.userState.collectAsState()
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleDailyReminder(true)
            Toast.makeText(context, "Daily history reminders enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission required for daily reminders.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timestamp ->
                        viewModel.setDateFromTimestamp(timestamp)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            userProfile = userProfile,
            onSignInRequested = {
                viewModel.authManager.signInSimulatedUser()
                Toast.makeText(context, "Signed in with Google", Toast.LENGTH_SHORT).show()
            },
            onSignOutRequested = {
                viewModel.authManager.signOut()
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAuthDialog = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "This Day in History",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val current = dailyReminderEnabled
                        if (!current) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.toggleDailyReminder(true)
                                Toast.makeText(context, "Daily history reminders enabled!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.toggleDailyReminder(false)
                            Toast.makeText(context, "Daily reminders disabled", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = if (dailyReminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Daily Reminder",
                            tint = if (dailyReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showAuthDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Sign In",
                            tint = if (userProfile != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                HistoryTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        HistoryTab.EVENTS -> Icons.Default.Event
                        HistoryTab.BIRTHS -> Icons.Default.Cake
                        HistoryTab.DEATHS -> Icons.Default.MoodBad
                        HistoryTab.BOOKMARKS -> Icons.Default.Bookmark
                    }

                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.title) },
                        icon = { Icon(imageVector = icon, contentDescription = tab.title) }
                    )
                }
            }

            if (selectedTab != HistoryTab.BOOKMARKS) {
                DateHeader(
                    dateFormatted = viewModel.getFormattedDate(),
                    onOpenDatePicker = { showDatePicker = true },
                    onOpenQrScanner = {
                        CodeScannerHelper.scanQrCode(
                            context = context,
                            onDateScanned = { month, day ->
                                viewModel.setDate(month, day)
                                Toast.makeText(context, "Scanned date: $month-$day", Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            } else {
                Text(
                    text = "Saved Bookmarks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is HistoryUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is HistoryUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadData() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is HistoryUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTab == HistoryTab.BOOKMARKS) "No bookmarks saved yet!" else "No events found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = state.items,
                                    key = { item -> "${item.year}_${item.text.hashCode()}" }
                                ) { item ->
                                    HistoryItemCard(
                                        item = item,
                                        onBookmarkToggle = { viewModel.toggleBookmark(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
