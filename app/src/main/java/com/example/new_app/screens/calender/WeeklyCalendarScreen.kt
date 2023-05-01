package com.example.new_app.screens.calender

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.new_app.common.helpers.getWeekPageTitle
import com.example.new_app.common.helpers.rememberFirstVisibleWeekAfterScroll
import com.example.new_app.model.Task
import com.example.new_app.screens.task.tasklist.ShowDialogWithTaskDetailsAndDelete
import com.example.new_app.screens.task.tasklist.TaskListViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyCalendarViewScreen(
    viewModel: TaskListViewModel,
    openScreen: (String) -> Unit,
) {
    val tasks by viewModel.taskListUiState.collectAsState()
    val taskList = tasks.tasks
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    var selection by remember { mutableStateOf(currentDate) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
        )
        val visibleWeek = rememberFirstVisibleWeekAfterScroll(state)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = getWeekPageTitle(visibleWeek),
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        WeekCalendar(
            modifier = Modifier.background(color = Color(0xFF444444)),
            state = state,
            dayContent = { day ->
                val hasActiveTasks = taskList.any {
                    val taskDate = LocalDate.parse(it.dueDate, dueDateFormat)
                    taskDate == day.date
                }
                Day(day.date, isSelected = selection == day.date, hasActiveTasks = hasActiveTasks) { clicked ->
                    if (selection != clicked) {
                        selection = clicked
                    }
                }
            },
        )

        HourlyTaskView(
            selectedDate = selection,
            taskList = taskList,
            viewModel = viewModel,
            context = context
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HourlyTaskView(
    selectedDate: LocalDate,
    taskList: List<Task>,
    viewModel: TaskListViewModel,
    context: Context
) {
    val selectedTask = remember { mutableStateOf<Task?>(null) }
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val tasksForSelectedDate = taskList.filter {
        val taskDate = LocalDate.parse(it.dueDate, dueDateFormat)
        taskDate == selectedDate
    }
    val padding = 8.dp

    if (selectedTask.value != null) {
        ShowDialogWithTaskDetailsAndDelete(
            context = context,
            task = selectedTask.value!!,
            viewModel = viewModel,
            onDismiss = { selectedTask.value = null }
        )
    }

    if (tasksForSelectedDate.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .padding(padding),
                text = "You have no tasks yet for this day",
                fontSize = 24.sp,
                color = Color.White
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val taskHours = tasksForSelectedDate.mapNotNull { it.dueTime.split(":")[0].toIntOrNull() }.toSet().sorted()
            var lastDisplayedHour = -1

            taskHours.forEach { taskHour ->
                val startTime = if (lastDisplayedHour == -1) 0 else lastDisplayedHour + 1

                if (startTime != taskHour) {
                    Text(
                        text = String.format("%02d:00 - %02d:00", startTime, taskHour - 1),
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(top = padding / 2, bottom = padding / 2, start = padding, end = padding)
                    )
                }

                Text(
                    text = String.format("%02d:00", taskHour),
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(top = padding / 2, bottom = padding / 2, start = padding, end = padding)
                )

                val tasksForThisHour = tasksForSelectedDate.filter {
                    val currentTaskHour = it.dueTime.split(":")[0].toIntOrNull() ?: 0
                    currentTaskHour == taskHour
                }

                tasksForThisHour.forEach { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            )
                            .padding(top = padding / 2, bottom = padding / 2, start = padding, end = padding),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 5.dp,
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(task.color ?: -478827),
                            contentColor = Color.Black,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                        ) {
                            Text(
                                text = task.title,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Due: ${task.dueDate} ${task.dueTime}",
                                fontSize = 12.sp,
                                color = Color.Black,
                            )

                            task.description.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Description: $it",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                )
                            }

                            task.locationName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Location: $it",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                }

                lastDisplayedHour = taskHour
            }

            if (lastDisplayedHour < 23) {
                Text(
                    text = String.format("%02d:00 - 23:00", lastDisplayedHour + 1),
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(top = padding / 2, bottom = padding / 2, start = padding, end = padding)
                )
            }
        }
    }
}


private val dateFormatter = DateTimeFormatter.ofPattern("dd")
private val dueDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

@Composable
private fun Day(date: LocalDate, isSelected: Boolean, hasActiveTasks: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.displayText(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomCenter)
            )
        }
        if (hasActiveTasks) {
            Box(
                modifier = Modifier.padding(6.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .align(Alignment.TopEnd)
                    .background(Color.Red)
            )
        }
    }
}

