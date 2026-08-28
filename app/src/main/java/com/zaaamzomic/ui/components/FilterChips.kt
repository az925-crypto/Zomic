package com.zaaamzomic.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaaamzomic.ui.theme.*

@Composable
fun FilterChipsRow(
    readingOptions: List<String>,
    selectedReading: String,
    onReadingSelected: (String) -> Unit,
    pubOptions: List<String>,
    selectedPub: Set<String>,
    onPubToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        readingOptions.forEach { opt ->
            val selected = opt == selectedReading
            FilterChip(
                selected = selected,
                onClick = { onReadingSelected(opt) },
                label = { Text(opt, fontSize = 12.sp) },
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Hanko,
                    selectedLabelColor = PaperIvory,
                )
            )
        }
        Box(Modifier.width(1.dp).height(16.dp).padding(horizontal = 2.dp))
        pubOptions.forEach { opt ->
            val selected = opt in selectedPub
            FilterChip(
                selected = selected,
                onClick = { onPubToggle(opt) },
                label = { Text(opt, fontSize = 12.sp) },
                shape = RoundedCornerShape(999.dp),
            )
        }
    }
}
