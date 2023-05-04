package com.example.new_app.common.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.new_app.screens.screen_switcher.BottomNavigationItem
import com.example.new_app.theme.MediumGrey

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
    listState: LazyListState,
) {
    val visible = listState.isScrollingUp()

    // EnterTransition, ExitTransition, fadeIn, expandIn, fadeOut, shrinkOut, AnimatedVisibilityScope
    //See Also:
    //fadeIn, scaleIn, slideIn, slideInHorizontally, slideInVertically, expandIn, expandHorizontally, expandVertically, AnimatedVisibility
    //See Also:
    //fadeOut, scaleOut, slideOut, slideOutHorizontally, slideOutVertically, shrinkOut, shrinkHorizontally, shrinkVertically, AnimatedVisibility

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = 200,
            )
        ),
        exit =  shrinkVertically(
            animationSpec = tween(
                durationMillis = 200,
            )
        ),
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.height(48.dp),
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
                        indicatorColor = MediumGrey,
                        unselectedIconColor = MediumGrey,
                    )
                )
            }
        }
    }
}