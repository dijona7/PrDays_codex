package com.algoritmika.prapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.algoritmika.prapp.ui.theme.PrAppTheme
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        setLocale(this, lang)

        val insertedRange = updateDateRangesWithPrediction(this)

        setContent {
            PrAppTheme {
                var ranges by remember { mutableStateOf(insertedRange) }
                MainScreen(ranges) { updated ->
                    saveDateRanges(this, updated)
                }
            }
        }
    }
}

fun setLocale(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
}

@Composable
fun MainScreen(
    ranges: List<ClosedRange<LocalDate>>,
    onSave: (List<ClosedRange<LocalDate>>) -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("calendar") }
    var currentRanges by remember { mutableStateOf(ranges) }
    val groupedCurrent by remember { derivedStateOf { groupRangesByMonthWithGlobalGaps(currentRanges) } }

    ThreeZonesScreen(
        topContent = { CycleInfo(currentRanges) },
        middleContent = {
            when (currentTab) {
                "calendar" -> DateListScreen(
                    rangesByMonth = groupedCurrent,
                    onUpdateRange = { oldStart, oldEnd, newStart, newEnd ->
                        currentRanges = currentRanges.map {
                            if (it.start == oldStart && it.endInclusive == oldEnd) {
                                newStart..newEnd
                            } else it
                        }
                        onSave(currentRanges)
                    }
                )
                "file" -> FileEditorScreen(context) { newRanges ->
                    currentRanges = newRanges
                    onSave(currentRanges)
                }
                "settings" -> SettingsScreen { updated ->
                    currentRanges = updated
                    onSave(currentRanges)
                }
            }
        },
        selectedTab = currentTab,
        onTabSelected = { currentTab = it }
    )
}

@Composable
fun ThreeZonesScreen(
    topContent: @Composable () -> Unit,
    middleContent: @Composable () -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                topContent()
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                NavigationBarItem(
                    selected = selectedTab == "calendar",
                    onClick = { onTabSelected("calendar") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.calendar)) },
                    label = { Text(stringResource(R.string.calendar)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "file",
                    onClick = { onTabSelected("file") },
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.file)) },
                    label = { Text(stringResource(R.string.file)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { onTabSelected("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) },
                    label = { Text(stringResource(R.string.settings)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            middleContent()
        }
    }
}
