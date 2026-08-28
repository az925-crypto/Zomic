package com.zaaamzomic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.ui.theme.Hanko

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkSheet(
    title: String,
    current: ReadingStatus?,
    onSelect: (ReadingStatus) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember(current) { mutableStateOf(current ?: ReadingStatus.SEDANG_DIBACA) }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("Pilih status bacamu. Status publikasi (Tamat/Ongoing) otomatis dari API.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            ReadingStatus.entries.forEach { rs ->
                val label = when (rs) {
                    ReadingStatus.SEDANG_DIBACA -> "Sedang Dibaca"
                    ReadingStatus.BELUM_DIBACA -> "Belum Dibaca"
                    ReadingStatus.DROPPED -> "Dropped"
                }
                val desc = when (rs) {
                    ReadingStatus.SEDANG_DIBACA -> "Tracking aktif"
                    ReadingStatus.BELUM_DIBACA -> "Simpan buat nanti"
                    ReadingStatus.DROPPED -> "Berhenti follow"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selected == rs) Hanko.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { selected = rs }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RadioButton(selected = selected == rs, onClick = { selected = rs })
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.titleSmall)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Button(onClick = { onSelect(selected); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Text("Simpan ke Library")
            }
            Text("FR-7 • FR-8 auto-tag • FR-10 pindah kapan saja", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
        }
    }
}
