package com.jawahir.amoro.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jawahir.amoro.ui.theme.AMOROTheme
import com.jawahir.amoro.ui.theme.Dimens

data class DropDownItem(
    val label: String,
    val onSelected: () -> Unit
)

@Composable
fun DropDownChip(
    label: String,
    isActive: Boolean,
    items: List<DropDownItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = isActive,
            onClick = { expanded = true },
            label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.width(Dimens.ChipWidth)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        item.onSelected()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDropDownChip(){
    AMOROTheme {
        DropDownChip(
            label = "Genre",
            isActive = true,
            items = listOf(DropDownItem("Action",{}),DropDownItem("Thriller",{}),DropDownItem("Drama",{})),
        )
    }
}