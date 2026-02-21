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

package com.forvia.notes.ui.screens.homescreen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forvia.notes.ui.components.CategoryBar
import com.forvia.notes.ui.components.CategoryItems
import com.forvia.notes.ui.components.FAB
import com.forvia.notes.ui.components.NoteCard
import com.forvia.notes.ui.components.SearchBar
import com.forvia.notes.ui.components.ViewToggleRow
import com.forvia.notes.ui.theme.rememberNotesHaptics
import com.forvia.notes.utils.BiometricAuthUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNoteClick: (Int) -> Unit,
    onAddNoteClick: (String?) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val haptics = rememberNotesHaptics()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyStaggeredGridState()
    val context = LocalContext.current
    
    val locale = Locale.getDefault()
    val dateFormatter = remember(uiState.dateFormat, locale) {
        SimpleDateFormat(uiState.dateFormat, locale)
    }

    LaunchedEffect(uiState.selectedCategory) {
        listState.animateScrollToItem(0)
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val menuWidth = screenWidth * 0.6f

    Scaffold(
        floatingActionButton = {
            FAB(
                onClick = {
                    val category = if (uiState.selectedCategory == "All") null else uiState.selectedCategory
                    onAddNoteClick(category)
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            haptics.click()
                            onSettingsClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                CategoryBar(
                    categories = CategoryItems(uiState.allAvailableCategories),
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelect = { viewModel.selectCategory(it) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalStaggeredGrid(
                    state = listState,
                    columns = StaggeredGridCells.Fixed(if (uiState.isGridView) 2 else 1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(uiState.allNotes, key = { it.id }) { note ->
                        val dateStr = remember(note.createdTime, uiState.dateFormat, locale) {
                            dateFormatter.format(Date(note.createdTime))
                        }
                        var showMenu by remember { mutableStateOf(false) }
                        val menuPinnedState = remember(note.isPinned, showMenu) {
                            if (showMenu) note.isPinned else false
                        }
                        val menuLockedState = remember(note.isLocked, showMenu) {
                            if (showMenu) note.isLocked else false
                        }
                        
                        Box(modifier = Modifier.animateItem()) {
                            NoteCard(
                                note = note,
                                date = dateStr,
                                isGridView = uiState.isGridView,
                                isHighlighted = showMenu,
                                onClick = { 
                                    haptics.tick()
                                    viewModel.onNoteClicked(note.id)
                                    
                                    if (note.isLocked && uiState.isBiometricEnabled) {
                                        val activity = context as? FragmentActivity
                                        if (activity != null) {
                                            BiometricAuthUtil.authenticate(
                                                activity = activity,
                                                title = "Unlock Note",
                                                subtitle = "Authenticate to view this locked note",
                                                onSuccess = {
                                                    onNoteClick(note.id)
                                                },
                                                onError = { error ->
                                                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            onNoteClick(note.id)
                                        }
                                    } else {
                                        onNoteClick(note.id)
                                    }
                                },
                                onLongClick = {
                                    showMenu = true
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
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
                                        showMenu = false
                                        viewModel.togglePin(note)
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
                                        showMenu = false
                                        viewModel.toggleLock(note)
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
                                        showMenu = false
                                        viewModel.archiveNote(note)
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
                                        showMenu = false
                                        viewModel.deleteNote(note)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            ViewToggleRow(
                isGridView = uiState.isGridView,
                onToggle = {
                    haptics.tick()
                    viewModel.toggleGridView()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }
    }
}
