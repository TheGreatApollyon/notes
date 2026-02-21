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

package com.forvia.notes.ui.screens.notedetailscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forvia.notes.ui.components.CreateTagSheet
import com.forvia.notes.ui.components.DeleteNoteDialog
import com.forvia.notes.ui.components.RestoreNoteDialog
import com.forvia.notes.ui.components.TagSelectionSheet
import com.forvia.notes.ui.theme.rememberNotesHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onManageCategoryClick: () -> Unit = {},
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val haptics = rememberNotesHaptics()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTagSelectionSheet by remember { mutableStateOf(false) }
    var showCreateTagSheet by remember { mutableStateOf(false) }
    var showRestoreNoteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var menuPinnedState by remember { mutableStateOf(false) }
    var menuLockedState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentFocusRequester = remember { FocusRequester() }

    val locale = Locale.getDefault()
    val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)
    val dateString = remember(uiState.createdTime, is24HourFormat, userPrefs.dateFormat, locale) {
        val timePattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        val datePattern = userPrefs.dateFormat
        val pattern = "$datePattern, $timePattern"
        SimpleDateFormat(pattern, locale).format(Date(uiState.createdTime))
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val titleStyle = remember(onSurfaceColor) {
        TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor
        )
    }

    val contentStyle = remember(onSurfaceColor) {
        TextStyle(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal,
            color = onSurfaceColor.copy(alpha = 0.85f)
        )
    }

    val cursorBrush = remember(primaryColor) {
        SolidColor(primaryColor)
    }

    LaunchedEffect(uiState.title, uiState.content, uiState.category) {
        if (uiState.title.isNotBlank() || uiState.content.isNotBlank()) {
            viewModel.autoSave()
        }
    }

    val isArchivedOrTrashed = uiState.isArchived || uiState.isTrashed
    val isCategoryBlank = uiState.category.isBlank()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isCategoryBlank) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .clickable(enabled = !isArchivedOrTrashed) {
                                        keyboardController?.hide()
                                        scope.launch {
                                            delay(100)
                                            showTagSelectionSheet = true
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = uiState.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (uiState.isNotePersisted) {
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    Surface(
                        onClick = {
                            haptics.click()
                            keyboardController?.hide()
                            viewModel.autoSave()
                            onBackClick()
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .padding(start = 12.dp)
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
                actions = {
                    if (isArchivedOrTrashed) {
                        Surface(
                            onClick = {
                                haptics.click()
                                showRestoreNoteDialog = true
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            enabled = true,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore/Unarchive",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = {
                                    haptics.click()
                                    menuPinnedState = uiState.isPinned
                                    menuLockedState = uiState.isLocked
                                    showMoreMenu = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Actions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val configuration = LocalConfiguration.current
                            val screenWidth = configuration.screenWidthDp.dp
                            val menuWidth = screenWidth * 0.6f

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.width(menuWidth)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(if (menuPinnedState) "Unpin Note" else "Pin Note")
                                            Spacer(modifier = Modifier.width(screenWidth * 0.1f))
                                            Icon(
                                                imageVector = Icons.Filled.PushPin,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        haptics.tick()
                                        showMoreMenu = false
                                        viewModel.togglePin()
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(if (menuLockedState) "Unlock Note" else "Lock Note")
                                            Spacer(modifier = Modifier.width(screenWidth * 0.1f))
                                            Icon(
                                                imageVector = if (menuLockedState) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        haptics.tick()
                                        showMoreMenu = false
                                        viewModel.toggleLock()
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Archive Note")
                                            Spacer(modifier = Modifier.width(screenWidth * 0.1f))
                                            Icon(
                                                imageVector = Icons.Default.Archive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        haptics.tick()
                                        showMoreMenu = false
                                        viewModel.archiveNote()
                                        onBackClick()
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Move to Trash",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(screenWidth * 0.1f))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    onClick = {
                                        haptics.tick()
                                        showMoreMenu = false
                                        viewModel.deleteNote()
                                        onBackClick()
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isArchivedOrTrashed) {
                        contentFocusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCategoryBlank && !isArchivedOrTrashed) {
                        Surface(
                            onClick = {
                                keyboardController?.hide()
                                scope.launch {
                                    delay(100)
                                    showTagSelectionSheet = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add tag",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add tag",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isArchived) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archived",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (uiState.isTrashed) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Trashed",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    enabled = !isArchivedOrTrashed,
                    textStyle = titleStyle,
                    cursorBrush = cursorBrush,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty() && !isArchivedOrTrashed) {
                                Text(
                                    text = "Untitled",
                                    style = titleStyle.copy(color = onSurfaceColor.copy(alpha = 0.3f))
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    enabled = !isArchivedOrTrashed,
                    textStyle = contentStyle,
                    cursorBrush = cursorBrush,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(contentFocusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showTagSelectionSheet) {
        TagSelectionSheet(
            categories = com.forvia.notes.ui.components.CategoryItems(availableCategories),
            selectedCategory = uiState.category,
            onCategorySelect = { newCategory ->
                haptics.tick()
                viewModel.updateCategory(newCategory)
            },
            onCreateNewTag = {
                showTagSelectionSheet = false
                showCreateTagSheet = true
            },
            onDismiss = { showTagSelectionSheet = false }
        )
    }

    if (showCreateTagSheet) {
        CreateTagSheet(
            onDismiss = { showCreateTagSheet = false },
            onCreateTag = { newTag ->
                haptics.success()
                viewModel.createAndSelectCategory(newTag)
            }
        )
    }

    if (showDeleteDialog) {
        DeleteNoteDialog(
            onDismiss = {
                haptics.click()
                showDeleteDialog = false
            },
            onConfirm = {
                haptics.heavy()
                showDeleteDialog = false
                viewModel.deleteNote()
                onBackClick()
            }
        )
    }

    if (showRestoreNoteDialog) {
        RestoreNoteDialog(
            onDismiss = {
                haptics.click()
                showRestoreNoteDialog = false
            },
            onConfirm = {
                haptics.success()
                showRestoreNoteDialog = false
                viewModel.restoreNote()
                onBackClick()
            }
        )
    }
}
