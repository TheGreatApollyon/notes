package com.openappslabs.jotter.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimeFormatButton(
    is24Hour: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeContentColor = MaterialTheme.colorScheme.primary
    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val activeContainerColor = MaterialTheme.colorScheme.secondaryContainer

    val iconButtonSize = 48.dp

    val offsetX by animateDpAsState(
        targetValue = if (is24Hour) iconButtonSize else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "SlidingIndicatorOffset"
    )

    Box(
        modifier = modifier
            .width(iconButtonSize * 2)
            .height(iconButtonSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        // 1. Sliding Indicator
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .size(iconButtonSize)
                .clip(CircleShape)
                .background(activeContainerColor)
        )

        // 2. Buttons
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { if (is24Hour) onToggle() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "12h",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (!is24Hour) activeContentColor else inactiveContentColor
                )
            }

            // 24h Button Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { if (!is24Hour) onToggle() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "24h",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (is24Hour) activeContentColor else inactiveContentColor
                )
            }
        }
    }
}