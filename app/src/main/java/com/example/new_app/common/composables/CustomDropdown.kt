package com.example.new_app.common.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun CustomDropdown(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded.value = true }) {
            Text(selectedItem)
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Expand Dropdown"
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    onItemSelected(item)
                }) {
                    Text(item)
                }
            }
        }
    }
}
