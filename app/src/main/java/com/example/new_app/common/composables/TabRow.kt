package com.example.new_app.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.new_app.model.Task


@Composable
fun CustomTabRow(
    selectedIndex: MutableState<Int>,
    tabTitles: List<String>,
    selectedTasks: MutableList<Task>,
    rowColor: Color = MaterialTheme.colorScheme.secondary,
){
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        HomeCategoryTabIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedIndex.value]),
            color = rowColor
        )
    }

    TabRow(
        selectedTabIndex = selectedIndex.value,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = Color.White,
        indicator = indicator,
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = index == selectedIndex.value,
                onClick = {
                    if (index != selectedIndex.value) {
                        selectedIndex.value = index
                        if (index == 1) {
                            selectedTasks.clear()
                        }
                    }
                },
                selectedContentColor = rowColor,
                unselectedContentColor = Color.White,
            )
        }
    }
}

@Composable
fun HomeCategoryTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
) {
    Spacer(
        modifier
            .padding(horizontal = 24.dp)
            .height(4.dp)
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}
