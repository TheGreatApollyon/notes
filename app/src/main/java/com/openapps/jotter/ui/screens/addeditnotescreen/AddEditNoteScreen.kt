package com.openapps.jotter.ui.screens.addeditnotescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // <-- Use standard IconButton for the Top Bar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openapps.jotter.ui.components.Header // Your updated custom Header

@Composable
fun AddEditNoteScreen(
    modifier: Modifier = Modifier,
    noteId: Int? = null,
    initialTitle: String = "",
    initialContent: String = "",
    onBackClick: () -> Unit,
    onSave: (title: String, content: String) -> Unit,
) {
    // Note: The use of initialTitle/Content here is correct for the initial state.
    // Ensure the calling function provides the correct initial data for editing (See previous response).
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }

    val isEditing = noteId != null
    val isSaveEnabled = title.isNotBlank() || content.isNotBlank()

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Note" else "New Note",
                onBackClick = onBackClick,
                // ✨ Injecting the Save Icon Button into the new actions slot
                actions = {
                    IconButton(
                        onClick = { onSave(title.trim(), content.trim()) },
                        enabled = isSaveEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = if (isEditing) "Save Changes" else "Save Note",
                            // Use a distinct color for the icon button to be visible against the surface
                            tint = if (isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content Field
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(text = "Content") },
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

            // The bottom Button has been successfully removed!
        }
    }
}