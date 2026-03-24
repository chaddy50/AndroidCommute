package com.chaddy50.morningcommute.view.busTimingCard

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun StatusChip(label: String, color: Color) {
    SuggestionChip(
        onClick = {},
        label = { Text(label, fontSize = 20.sp) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color,
            labelColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}
