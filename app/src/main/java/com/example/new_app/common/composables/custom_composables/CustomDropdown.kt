package com.example.new_app.common.composables.custom_composables

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
import com.example.new_app.common.sort.TaskSortType

@Composable
fun DropdownContextMenu(
    options: List<String>,
    modifier: Modifier,
    onActionClick: (String) -> Unit,
    imageVector: ImageVector = Icons.Default.MoreVert,
    trailingIcon: ImageVector = Icons.Default.Delete,
    activeSortType: TaskSortType
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
        options.forEachIndexed { index, selectionOption ->
            val isSelectedSortType = when (index) {
                0 -> activeSortType == TaskSortType.DATE_CREATED_ASC
                1 -> activeSortType == TaskSortType.DATE_CREATED_DESC
                2 -> activeSortType == TaskSortType.TITLE_ASC
                3 -> activeSortType == TaskSortType.TITLE_DESC
                4 -> activeSortType == TaskSortType.DUE_DATE_ASC
                5 -> activeSortType == TaskSortType.DUE_DATE_DESC
                6 -> activeSortType == TaskSortType.COLOR
                else -> false
            }

            DropdownMenuItem(
                onClick = {
                    isExpanded = false
                    onActionClick(selectionOption)
                },
                text = { Text(text = selectionOption) },
                trailingIcon = {
                    if (isSelectedSortType) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = "Selected",
                        )
                    }
                }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
fun DeleteSelectedContextMenu(
    modifier: Modifier,
    onActionClick: (String) -> Unit,
    imageVector: ImageVector = Icons.Default.MoreVert
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
        val selectionOption = "Delete All Selected"
        DropdownMenuItem(
            onClick = {
                isExpanded = false
                onActionClick(selectionOption)
            },
            text = { Text(text = selectionOption) }
        )
    }
}
