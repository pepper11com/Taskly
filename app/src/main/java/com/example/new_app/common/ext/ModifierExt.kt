package com.example.new_app.common.ext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

fun Modifier.divider(): Modifier {
    return this.padding(top = 16.dp)
}

fun Modifier.lazyColumn(innerPadding: PaddingValues): Modifier {
    return this
      .fillMaxSize()
      .padding(innerPadding)
      .padding(16.dp)
}

fun Modifier.padding16(): Modifier {
    return this.padding(16.dp)
}

fun Modifier.padding8(): Modifier {
    return this.padding(8.dp)
}
fun Modifier.paddingMaxsize(): Modifier {
    return this.fillMaxSize().padding(8.dp)
}

fun Modifier.standardImage(): Modifier {
    return this.size(150.dp).padding(8.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.calendar(
    innerPadding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior
): Modifier {
    return this
      .fillMaxSize()
      .padding(innerPadding)
      .nestedScroll(scrollBehavior.nestedScrollConnection)
}

fun Modifier.customCalendarBox(color: Color): Modifier {
    return this
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
        .height(5.dp)
        .clip(RoundedCornerShape(25.dp))
        .background(color)
}

fun Modifier.customCalendarBox2(color: Color): Modifier {
    return this
        .padding(end = 2.dp, bottom = 2.dp)
        .size(16.dp)
        .clip(CircleShape)
        .background(color)
}

fun Modifier.staticMap(): Modifier {
    return this.size(100.dp).padding(8.dp).clip(RoundedCornerShape(4.dp))
}

fun Modifier.padding4(): Modifier {
    return this.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
}

fun Modifier.isSelected(color: Color): Modifier {
    return this.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(25.dp)).background(color)
}

fun Modifier.hasActiveTasks(color: Color): Modifier {
    return this.padding(6.dp).size(5.dp).clip(CircleShape).background(color)
}