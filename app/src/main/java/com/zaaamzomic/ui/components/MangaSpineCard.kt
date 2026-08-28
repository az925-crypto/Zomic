package com.zaaamzomic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.ui.theme.*

@Composable
fun MangaSpineCard(
    title: String,
    thumbnail: String?,
    type: String?,
    genre: String?,
    chapterInfo: String?,
    description: String?,
    publicationStatus: PublicationStatus,
    readingStatus: ReadingStatus?,
    progress: Float? = null, // 0..1
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val isSedang = readingStatus == ReadingStatus.SEDANG_DIBACA
    val borderColor = if (isSedang) Hanko else outline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // Spine strip 4dp
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    when (readingStatus) {
                        ReadingStatus.SEDANG_DIBACA -> Hanko
                        ReadingStatus.BELUM_DIBACA -> GalleyGrey
                        ReadingStatus.DROPPED -> Color(0xFFB0B5BD) // striped handled via alpha
                        null -> GalleyGrey.copy(alpha = 0.35f)
                    }
                )
        ) {
            // dot for publikasi
            val dotColor = when (publicationStatus) {
                PublicationStatus.TAMAT -> OkGreen
                PublicationStatus.BELUM_TAMAT -> Color.Transparent
                PublicationStatus.UNKNOWN -> GalleyGrey
            }
            if (publicationStatus == PublicationStatus.TAMAT) {
                Box(
                    Modifier
                        .size(6.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
            } else if (publicationStatus == PublicationStatus.BELUM_TAMAT) {
                Box(
                    Modifier
                        .size(6.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        // cover — validate https only (security)
        val safeThumb = thumbnail?.takeIf { it.startsWith("https://") }
        Box {
            AsyncImage(
                model = safeThumb,
                contentDescription = title,
                modifier = Modifier
                    .size(width = 64.dp, height = 84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpineMist2),
                contentScale = ContentScale.Crop,
            )
            // badge
            val badgeText = when (publicationStatus) {
                PublicationStatus.TAMAT -> "● TAMAT"
                PublicationStatus.BELUM_TAMAT -> "○ ONGOING"
                PublicationStatus.UNKNOWN -> ""
            }
            if (badgeText.isNotEmpty()) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF6F1E7).copy(alpha = 0.92f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        badgeText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08.sp.value.sp,
                        color = if (publicationStatus == PublicationStatus.TAMAT) OkGreen else GalleyGrey,
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            val meta = listOfNotNull(type, genre, chapterInfo).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = GalleyGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }
            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(SpineMist2)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(Hanko)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Lanjut baca →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Hanko,
                )
            }
        }
    }
}

@Composable
fun MangaSpineCardCompact(
    title: String,
    thumbnail: String?,
    chapterInfo: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeThumb = thumbnail?.takeIf { it.startsWith("https://") }
    Column(
        modifier = modifier.width(112.dp).clip(RoundedCornerShape(10.dp)).background(SpineMist).clickable(onClick = onClick).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AsyncImage(
            model = safeThumb,
            contentDescription = title,
            modifier = Modifier.fillMaxWidth().height(128.dp).clip(RoundedCornerShape(8.dp)).background(SpineMist2),
            contentScale = ContentScale.Crop,
        )
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (!chapterInfo.isNullOrBlank()) {
            Text(chapterInfo, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
