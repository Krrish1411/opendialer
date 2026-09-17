package com.opendialer.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimBadge(
    simLabel: String,
    slotIndex: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = if (slotIndex == 0) {
        Pair(Color(0xFF8B5CF6).copy(alpha = 0.18f), Color(0xFF8B5CF6))
    } else {
        Pair(Color(0xFF06B6D4).copy(alpha = 0.18f), Color(0xFF06B6D4))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = simLabel.ifEmpty { "SIM ${slotIndex + 1}" },
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
