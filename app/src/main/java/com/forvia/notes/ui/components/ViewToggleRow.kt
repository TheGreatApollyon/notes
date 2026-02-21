/*
 * Copyright (c) 2026 Forvia
 *
 * This file is part of Notes
 *
 * Notes is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Notes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Notes.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.forvia.notes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
private fun ViewToggleSegmentButton(
    modifier: Modifier,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    activeContentColor: Color,
    inactiveContentColor: Color,
    activeCornerRadius: Dp,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDesc: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 250),
        label = "bgColor"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (active) activeCornerRadius else 14.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cornerRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDesc,
            tint = if (active) activeContentColor else inactiveContentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun ViewToggleRow(
    isGridView: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rowCorners = 50.dp

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(rowCorners))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val commonModifier = Modifier
                .width(52.dp)
                .height(40.dp)

            ViewToggleSegmentButton(
                modifier = commonModifier,
                active = !isGridView,
                activeColor = MaterialTheme.colorScheme.primary,
                activeCornerRadius = rowCorners,
                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onToggle,
                imageVector = Icons.AutoMirrored.Outlined.List,
                contentDesc = "List"
            )

            ViewToggleSegmentButton(
                modifier = commonModifier,
                active = isGridView,
                activeColor = MaterialTheme.colorScheme.primary,
                activeCornerRadius = rowCorners,
                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onToggle,
                imageVector = Icons.Outlined.GridView,
                contentDesc = "Grid"
            )
        }
    }
}
