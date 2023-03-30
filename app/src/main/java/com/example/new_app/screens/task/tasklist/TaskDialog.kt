package com.example.new_app.screens.task.tasklist

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.model.Task

@Composable
fun ShowDialogWithTaskDetailsAndDelete(
    context: Context,
    task: Task,
    viewModel: TaskListViewModel,
    onDismiss: () -> Unit
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24).apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = task.title)

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 4.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                    )
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            // No action needed here
                        }
                    }
                }


            }
        },
        text = {
            Column(
                Modifier.padding( end = 65.dp)
            ) {
                Text(text = task.description)

                Spacer(modifier = Modifier.height(18.dp))

                Text(text = getDueDateAndTime(task))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.onTaskDelete(task)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        }
    )
}