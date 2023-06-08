package com.example.new_app.common.composables.top_app_bars

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumAppBarWithTabs(
    selectedIndex: MutableState<Int>,
    tabTitles: List<String>,
    title: String
) {
    Column {
        EmptyTopBar(
            title = title,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
            ),
        )
        CustomTabRow(
            selectedIndex = selectedIndex,
            tabTitles = tabTitles,
            selectedTasks = mutableListOf()
        )
    }
}