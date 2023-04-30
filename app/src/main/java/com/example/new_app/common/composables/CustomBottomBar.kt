package com.example.new_app.common.composables

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.new_app.screens.screen_switcher.BottomNavigationItem


val bottomNavigationItems = listOf(
    BottomNavigationItem(
        label = "Tasks",
        selectedIcon = Icons.Filled.Task,
        unselectedIcon = Icons.Outlined.Task,
    ),
    BottomNavigationItem(
        label = "Calendar",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
    ),
)

@Composable
fun BottomNavigationBar(
    selectedIndex: MutableState<Int>,
    items: List<BottomNavigationItem> = bottomNavigationItems,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.height(48.dp)
    ) {

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    val icon = if (selectedIndex.value == index) {
                        item.selectedIcon
                    } else {
                        item.unselectedIcon
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                    )
                },
                selected = selectedIndex.value == index,
                onClick = { selectedIndex.value = index },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    indicatorColor = Color(0xFFBDBDBD),
                    unselectedIconColor = Color(0xFFBDBDBD),
                )
            )
        }
    }
}