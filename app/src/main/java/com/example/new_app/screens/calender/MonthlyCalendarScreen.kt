package com.example.new_app.screens.calender

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.new_app.R
import com.example.new_app.common.composables.MonthHeader
import com.example.new_app.common.composables.SimpleCalendarTitle
import com.example.new_app.common.composables.rememberFirstCompletelyVisibleMonth
import com.example.new_app.common.ext.calendar
import com.example.new_app.common.ext.customCalendarBox
import com.example.new_app.common.ext.customCalendarBox2
import com.example.new_app.common.ext.displayText
import com.example.new_app.common.ext.divider
import com.example.new_app.common.ext.padding8
import com.example.new_app.common.ext.paddingMaxsize
import com.example.new_app.common.ext.standardImage
import com.example.new_app.common.ext.staticMap
import com.example.new_app.common.ext.toDate
import com.example.new_app.common.ext.toLocalDate
import com.example.new_app.model.Task
import com.example.new_app.screens.task.tasklist.ShowDialogWithTaskDetailsAndDelete
import com.example.new_app.screens.task.tasklist.StaticMap
import com.example.new_app.screens.task.tasklist.TaskListViewModel
import com.example.new_app.screens.task.tasklist.generateStaticMapUrl
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import com.example.new_app.R.string as CalendarString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MonthlyCalendarScreen(
    viewModel: TaskListViewModel,
    openScreen: (String) -> Unit,

    paddingValues: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
) {
    // todo test test test was - val tasks by viewModel.taskListUiState.collectAsState()
    val tasks by viewModel.taskListUiState.collectAsStateWithLifecycle()
    val taskList = tasks.tasks
    val context = LocalContext.current

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    var selection by rememberSaveable { mutableStateOf<CalendarDay?>(CalendarDay(currentMonth.atDay(LocalDate.now().dayOfMonth), DayPosition.MonthDate)) }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid,
    )

    val tasksInSelectedDate = remember(taskList, selection, state.firstVisibleMonth.yearMonth) {
        derivedStateOf {
            val date = selection?.date
            if (date == null || state.firstVisibleMonth.yearMonth.month != date.month) emptyList() else taskList.filter { task ->
                task.dueDate.toDate().toLocalDate() == date
            }.sortedBy { it.dueDate.toDate() }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = rememberFirstCompletelyVisibleMonth(state)

    LazyColumn(
        modifier = Modifier
            .calendar(paddingValues, scrollBehavior),
        state = listState,
    ) {
        item {
            SimpleCalendarTitle(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                currentMonth = visibleMonth.yearMonth,
                goToPrevious = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                    }
                },
                goToNext = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                    }
                },
            )

            HorizontalCalendar(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding8(),
                state = state,
                dayContent = { day ->
                    val tasksOnDate = remember(day.date, taskList) {
                        if (day.position == DayPosition.MonthDate) {
                            taskList.filter { task ->
                                task.dueDate.toDate().toLocalDate() == day.date
                            }
                        } else {
                            emptyList()
                        }
                    }
                    val colors = remember(day.date, tasksOnDate) {
                        tasksOnDate.map { task ->
                            task.color?.let { Color(it) } ?: Color(0xFF4E4E4E)
                        }
                    }

                    Day(
                        day = day,
                        isSelected = selection == day,
                        colors = colors,
                    ) { clicked ->
                        selection = clicked
                    }
                },
                monthHeader = {
                    MonthHeader(
                        modifier = Modifier.padding(vertical = 8.dp),
                        daysOfWeek = daysOfWeek,
                    )
                },
            )
        }

        if (tasksInSelectedDate.value.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .divider(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.padding8(),
                            text = stringResource(CalendarString.no_tasks),
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                        Image(
                            modifier = Modifier.standardImage(),
                            painter = painterResource(id = R.drawable.removebg_preview),
                            contentDescription = "No tasks",
                            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        } else {
            itemsIndexed(tasksInSelectedDate.value, key = { _, task -> task.id }) { _, task ->
                Column(
                    modifier = Modifier.animateItemPlacement()
                ) {
                    TaskInformation(
                        task,
                        viewModel,
                        context
                    )
                }
            }
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean = false,
    colors: List<Color> = emptyList(),
    onClick: (CalendarDay) -> Unit = {},
) {

    val displayedColors = remember(colors) { colors.take(3) }
    val textColor = remember(day.position) {
        when (day.position) {
            DayPosition.MonthDate -> Color.White
            DayPosition.InDate, DayPosition.OutDate -> Color.LightGray
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(5.dp),
            )
            .padding(1.dp)
            .background(color = Color(0xFF444444), shape = RoundedCornerShape(5.dp))
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) },
            ),
    ) {

        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(5f)
                .padding(top = 3.dp, end = 4.dp),
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 12.sp,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (color in displayedColors) {
                Box(
                    modifier = Modifier
                        .customCalendarBox(color),
                )
            }
        }
        if (colors.size > 3) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .customCalendarBox2(MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "+",
                    color = Color.White,
                    fontSize = 10.sp,
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskInformation(
    task: Task,
    viewModel: TaskListViewModel,
    context: Context
) {
    val selectedTask = remember { mutableStateOf<Task?>(null) }
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (selectedTask.value != null) {
        ShowDialogWithTaskDetailsAndDelete(
            context = context,
            task = selectedTask.value!!,
            viewModel = viewModel,
            onDismiss = { selectedTask.value = null }
        )
    }

    val taskImage = generateStaticMapUrl(task)
    val dueDateMillis = task.dueDateToMillis()
    val dueDateText = if (dueDateMillis != null) {
        DateUtils.getRelativeTimeSpanString(
            dueDateMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
    } else {
        stringResource(CalendarString.no_due_date)
    }
    Card(
        modifier = Modifier
            .paddingMaxsize()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    selectedTask.value = task
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            25,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF444444)
        ),
        border = BorderStroke(2.dp, task.color?.let { Color(it) } ?: Color(0xFF4E4E4E)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = task.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(CalendarString.calendar_status, task.status),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                        Text(
                            text = stringResource(CalendarString.monthly_due_date, dueDateText),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " - ${task.dueDate} ${task.dueTime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }

                StaticMap(
                    staticMapUrl = taskImage,
                    modifier = Modifier.staticMap()
                )
            }
        }
    }
}