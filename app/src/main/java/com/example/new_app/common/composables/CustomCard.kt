package com.example.new_app.common.composables

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DangerousCardEditor(
    @StringRes title: Int,
    icon: ImageVector,
    content: String,
    modifier: Modifier,
    onEditClick: () -> Unit
) {
    CardEditor(title, icon, content, onEditClick, MaterialTheme.colorScheme.primary, modifier)
}

@Composable
fun RegularCardEditor(
    @StringRes title: Int,
    icon: ImageVector,
    content: String,
    modifier: Modifier,
    location: Boolean = false,
    onEditClick: () -> Unit,
) {
    CardEditor(
        title =title,
        icon = icon,
        content = content,
        onEditClick =onEditClick,
        Color.White,
        modifier = modifier,
        location = location
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardEditor(
    @StringRes title: Int,
    icon: ImageVector,
    content: String,
    onEditClick: () -> Unit,
    highlightColor: Color,
    modifier: Modifier,
    location: Boolean = false
) {
    if (location) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF444444),
            ),
            modifier = modifier,
            onClick = onEditClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(title),
                        color = highlightColor
                    )
                }


                Spacer(modifier = Modifier.weight(1f))

                if (content.isNotBlank()) {
                    HorizontalScrollView(
                        modifier = Modifier
                            .padding(16.dp, 0.dp)
                            .weight(2f)
                    ) {
                        Row(horizontalArrangement = Arrangement.End) {
                            Text(text = content)
                        }
                    }
                }
                Icon(
                    icon,
                    contentDescription = "Icon",
                    tint = highlightColor
                )
            }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = modifier,
            onClick = onEditClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ){
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(title),
                        color = highlightColor
                    )
                }
                if (content.isNotBlank()) {
                    Text(text = content, modifier = Modifier.padding(16.dp, 0.dp))
                }

                Icon((icon), contentDescription = "Icon", tint = highlightColor)
            }
        }
    }
}


@Composable
fun HorizontalScrollView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        val scrollState = rememberScrollState(0)
        Row(
            Modifier
                .horizontalScroll(scrollState)
                .clipToBounds()
        ) {
            content()
        }
    }
}



