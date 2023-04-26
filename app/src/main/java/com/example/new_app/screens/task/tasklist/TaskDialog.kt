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
import com.example.new_app.model.Task
import com.google.android.gms.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
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
                            .weight(1f)
                    ) {
                        TextField(
                            value = task.description,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                color = Color.White
                            ),
                            maxLines = 6,
                            readOnly = true,
                            interactionSource = remember { MutableInteractionSource() },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
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

                        Text(
                            text = task.locationName ?: "No location",
                            fontSize = 12.sp,
                            color = Color.White,
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
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    //check the oriantation of the device if in landscape show the map with only 75.dp height else show it with 350.dp height

                    if (task.location != null) {
                        DialogMap(
                            task = task,
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(250.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(250.dp)
                                .background(Color(0xFF666666), shape = RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = task.locationName ?: "No location name",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = task.description,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            color = Color.White
                        ),
                        maxLines = 4,
                        readOnly = true,
                        interactionSource = remember { MutableInteractionSource() },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getDueDateAndTime(task),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
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
