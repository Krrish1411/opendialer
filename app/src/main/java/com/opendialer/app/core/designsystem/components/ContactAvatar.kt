package com.opendialer.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.abs

@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val initial = remember(name) {
        name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }

    val gradient = remember(name) {
        val hash = abs(name.hashCode())
        val colors = listOf(
            listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
            listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
            listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
            listOf(Color(0xFF10B981), Color(0xFF059669)),
            listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
            listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
        )
        colors[hash % colors.size]
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrEmpty()) {
            AsyncImage(
                model = photoUri,
                contentDescription = name,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
