package com.example.new_app.screens.task.tasklist

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.common.composables.CustomCopyTrueTextField
import com.example.new_app.common.sort.getDueDateAndTime
import com.example.new_app.model.Task
import com.google.android.gms.maps.MapView

@Composable
fun ShowDialogWithTaskDetailsAndDelete(
    context: Context,
    task: Task,
    viewModel: TaskListViewModel,
    onDismiss: () -> Unit
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(min = 400.dp)
                    .heightIn(min = 100.dp, max = 900.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = getDueDateAndTime(task),
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }


                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                        ) {
                            if (task.location != null) {
                                DialogMap(
                                    task = task,
                                    modifier = Modifier
                                        .size(200.dp, 100.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp, 100.dp)
                                        .background(Color(0xFF666666), shape = RoundedCornerShape(8.dp))
                                )
                            }

                            CustomCopyTrueTextField(
                                value = task.locationName ?: "No location",
                                fontSize = 12.sp,
                            )

                        }


                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.onTaskDelete(task)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                onClick = onDismiss,
                            ) {
                                Text("Cancel")
                            }
                        }
                    }


                    Column{
                        CustomCopyTrueTextField(
                            value = task.description,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            maxLines = 50
                        )
                    }
                }
            }

        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(min = 400.dp)
                    .heightIn(min = 50.dp, max = 900.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = task.color?.let { Color(it) } ?: Color(0xFF666666),
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    if (task.location != null) {
                        DialogMap(
                            task = task,
                            modifier = Modifier
                                .height(165.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .background(Color(0xFF666666), shape = RoundedCornerShape(8.dp))
                        )
                    }

                    CustomCopyTrueTextField(
                        value = task.locationName ?: "No location",
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomCopyTrueTextField(
                        value = task.description,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 50
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getDueDateAndTime(task),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = task.color?.let { Color(it) } ?: Color(0xFF666666),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.onTaskDelete(task)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            onClick = onDismiss,
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
