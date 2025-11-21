package com.openapps.jotter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    // Optional: Only used for Home Screen
    isGridView: Boolean = false,
    onToggleView: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    // Optional: Only used for Detail Screens
    onBackClick: (() -> Unit)? = null,
    // ✨ NEW PARAMETER: Actions for the right side of the Top Bar
    actions: @Composable RowScope.() -> Unit = {},
    // ✨ NEW PARAMETERS for Edit/View Toggle
    isEditing: Boolean = false,
    onToggleEditView: (() -> Unit)? = null
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (onBackClick != null) {
        // --- DETAIL SCREEN MODE (Back Icon, Center Title/Button, Actions) ---
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                // ✨ LOGIC ADDED: Display EditViewButton if the toggle function is provided.
                if (onToggleEditView != null) {
                    EditViewButton(
                        isEditing = isEditing,
                        onToggle = onToggleEditView
                    )
                } else {
                    // Default title for new notes or settings screens
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                // The Circle Back Button (Left)
                Surface(
                    onClick = onBackClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            // The actions slot (Right) for the Save button
            actions = actions,
            colors = colors
        )
    } else {
        // --- HOME SCREEN MODE (Left Aligned + Action Buttons) ---
        TopAppBar(
            modifier = modifier,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            // Existing Home Screen actions remain the same
            actions = {
                if (onToggleView != null) {
                    FilledTonalIconButton(onClick = onToggleView) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onSettingsClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Optical balance padding for Home Screen
                Spacer(modifier = Modifier.width(8.dp))
            },
            colors = colors
        )
    }
}