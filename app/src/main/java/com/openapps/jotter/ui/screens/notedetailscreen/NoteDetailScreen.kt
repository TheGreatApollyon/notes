package com.openapps.jotter.ui.screens.notedetailscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openapps.jotter.ui.components.Header

@Composable
fun NoteDetailScreen(
    modifier: Modifier = Modifier,
    noteId: Int? = null,
    initialTitle: String = "",
    initialContent: String = "",
    onBackClick: () -> Unit,
    onSave: (title: String, content: String) -> Unit,
) {
    // 1. STATE & DATA
    // State initialization is reset when a new noteId is passed (Static Data Fix)
    var title by remember(noteId) { mutableStateOf(initialTitle) }
    var content by remember(noteId) { mutableStateOf(initialContent) }

    val isEditing = noteId != null

    // Check if the current content differs from the initial content (to enable Save button)
    val isContentModified = (title.trim() != initialTitle.trim()) || (content.trim() != initialContent.trim())

    // Save is enabled only if content is modified AND at least one field is non-blank
    val isSaveEnabled = isContentModified && (title.isNotBlank() || content.isNotBlank())

    // 2. VIEW/EDIT MODE STATE
    var isViewMode by remember { mutableStateOf(isEditing) }
    if (!isEditing) {
        isViewMode = false
    }

    Scaffold(
        topBar = {
            Header(
                title = if (!isEditing) "New Note" else "",
                onBackClick = onBackClick,

                // CENTER: Edit/View Toggle Button Logic
                isEditing = !isViewMode,
                onToggleEditView = if (isEditing) {
                    { isViewMode = !isViewMode }
                } else {
                    null
                },

                // RIGHT: Save Icon Button Logic (Circular Surface Fix)
                actions = {
                    Surface(
                        onClick = {
                            if (isSaveEnabled) {
                                onSave(title.trim(), content.trim())
                            }
                        },
                        // Consistent style: Circle shape with surfaceContainer background
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        // Control interaction based on state
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(48.dp)
                            .clip(CircleShape)   // 🔥 This is the missing line
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize() // Fills the 48.dp surface perfectly
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = if (isEditing) "Save Changes" else "Save Note",
                                // Control icon color based on enabled state
                                tint = if (isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title Field
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(text = "Title") },
                singleLine = true,
                // Controlled by View Mode
                readOnly = isViewMode,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content Field
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(text = "Content") },
                // Controlled by View Mode
                readOnly = isViewMode,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}