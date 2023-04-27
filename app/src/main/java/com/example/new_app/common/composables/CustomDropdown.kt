package com.example.new_app.common.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun DropdownContextMenu(
    options: List<String>,
    modifier: Modifier,
    onActionClick: (String) -> Unit,
    imageVector: ImageVector = Icons.Default.MoreVert,
    trailingIcon: ImageVector = Icons.Default.Delete
) {
    var isExpanded by remember { mutableStateOf(false) }

    IconButton(onClick = { isExpanded = true }) {
        Icon(
            modifier = Modifier.padding(8.dp, 0.dp),
            imageVector = imageVector,
            contentDescription = "More",
            tint = Color.White
        )
    }

    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { isExpanded = false },
        modifier = Modifier.width(180.dp)
    ) {
        options.forEach { selectionOption ->
            DropdownMenuItem(
                onClick = {
                    isExpanded = false
                    onActionClick(selectionOption)
                },
                text = { Text(text = selectionOption) },
                trailingIcon = {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Delete",
                    )
                }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}