package com.example.new_app.common.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(onRequestPermission: () -> Unit) {
    var showWarningDialog by remember { mutableStateOf(true) }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
            },
            modifier = Modifier.wrapContentWidth().wrapContentHeight()
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "The notification permission is important for this app. Please grant " +
                                "the permission if you want to receive notifications.",
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = {
                            onRequestPermission()
                            showWarningDialog = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Grant")
                    }
                }
            }
        }
    }
}

@Composable
fun RationaleSnackbar(
    snackbarHostState: SnackbarHostState,
    onRequestPermission: () -> Unit
) {
    LaunchedEffect(snackbarHostState) {
        val result = snackbarHostState.showSnackbar(
            message = "The notification permission is important for this app. If you want to receive" +
                    " notifications, please grant the permission.",
            actionLabel = "Grant",
            duration =  SnackbarDuration.Long,
            withDismissAction = true
        )
        if (result == SnackbarResult.ActionPerformed) {
            onRequestPermission()
        }
    }
}

