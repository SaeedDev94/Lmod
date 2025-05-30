package io.github.saeeddev94.lmod.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectDialog(
    label: String,
    options: List<String>,
    selected: Int,
    onOptionSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Text(label, Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 8.dp))
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$prefix${options[selected]}$suffix")
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select ringtone",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                LazyColumn {
                    items(options.size) { index ->
                        val option = options[index]
                        ItemRow(
                            name = "$prefix$option$suffix",
                            isSelected = index == selected,
                            onSelect = {
                                onOptionSelect(index)
                                showDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ItemRow(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
