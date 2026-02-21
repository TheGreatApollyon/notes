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

package com.forvia.notes.ui.screens.settingsscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forvia.notes.BuildConfig
import com.forvia.notes.ui.components.ClearAllDataDialog
import com.forvia.notes.ui.components.DisableLockWarningDialog
import com.forvia.notes.ui.screens.settingsscreen.components.SettingsGroup
import com.forvia.notes.ui.screens.settingsscreen.components.SettingsItemArrow
import com.forvia.notes.ui.screens.settingsscreen.components.SettingsItemDateFormat
import com.forvia.notes.ui.screens.settingsscreen.components.SettingsItemNoteLock
import com.forvia.notes.ui.screens.settingsscreen.components.SettingsItemSwitch
import com.forvia.notes.ui.screens.settingsscreen.components.TinyGap
import com.forvia.notes.ui.theme.rememberNotesHaptics
import com.forvia.notes.utils.BiometricAuthUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onTrashClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val haptics = rememberNotesHaptics()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDisableLockWarningDialog by remember { mutableStateOf(false) }

    val appBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    Surface(
                        onClick = {
                            haptics.click()
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
                colors = appBarColors
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = innerPadding.calculateTopPadding() + 8.dp, 
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val isSystemDarkTheme = isSystemInDarkTheme()
                
                SettingsGroup(title = "Appearance") {
                    AnimatedVisibility(
                        visible = isSystemDarkTheme,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            SettingsItemSwitch(
                                icon = Icons.Default.Brightness2,
                                title = "True Black Mode",
                                subtitle = "Use pure black for OLED displays",
                                checked = uiState.isTrueBlackEnabled,
                                onCheckedChange = viewModel::updateTrueBlackMode
                            )
                            TinyGap()
                        }
                    }

                    SettingsItemDateFormat(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Date Format",
                        subtitle = "Change how dates appear",
                        currentFormat = uiState.dateFormat,
                        onFormatSelected = viewModel::updateDateFormat
                    )
                }
            }

            item {
                SettingsGroup(title = "General & Navigation") {
                    SettingsItemArrow(
                        icon = Icons.Default.Archive,
                        title = "Archived Notes",
                        subtitle = "Notes you archive appear here",
                        onClick = onArchiveClick
                    )
                    TinyGap()

                    SettingsItemArrow(
                        icon = Icons.Default.Delete,
                        title = "Trash",
                        subtitle = "Notes you delete appear here",
                        onClick = onTrashClick
                    )
                    TinyGap()

                    SettingsItemSwitch(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on touch interactions",
                        checked = uiState.isHapticEnabled,
                        onCheckedChange = viewModel::updateHapticEnabled
                    )
                }
            }

            item {
                SettingsGroup(title = "Security") {
                    val authSupport = remember(context) {
                        BiometricAuthUtil.getAuthenticationSupport(context)
                    }
                    val isBiometricAvailable = authSupport.hasFingerprint || authSupport.hasDeviceCredential

                    AnimatedVisibility(
                        visible = isBiometricAvailable,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            SettingsItemNoteLock(
                                title = "Note Lock",
                                subtitle = "Require authentication to open",
                                checked = uiState.isBiometricEnabled,
                                authSupport = authSupport,
                                onCheckedChange = { isEnabled ->
                                    if (isEnabled) {
                                        viewModel.updateBiometricEnabled(true)
                                    } else {
                                        val activity = context as? FragmentActivity
                                        if (activity != null) {
                                            BiometricAuthUtil.authenticate(
                                                activity = activity,
                                                title = "Confirm Identity",
                                                subtitle = "Authenticate To Disable Note Lock",
                                                onSuccess = {
                                                    showDisableLockWarningDialog = true
                                                },
                                                onError = { }
                                            )
                                        }
                                    }
                                }
)
                    TinyGap()
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = "Data Management") {
                    SettingsItemArrow(
                        icon = Icons.Default.Backup,
                        title = "Backup & Restore",
                        subtitle = "Export or import notes",
                        onClick = onBackupRestoreClick
                    )
                    TinyGap()

                    SettingsItemArrow(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Local Data",
                        subtitle = "Reset app and delete all notes permanently",
                        onClick = { showClearAllDialog = true },
                        isDestructive = true
                    )
                }
            }

            item {
                SettingsGroup(title = "About") {
                    SettingsItemArrow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = onPrivacyPolicyClick
                    )

                    TinyGap()

                    SettingsItemArrow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = BuildConfig.VERSION_NAME,
                        onClick = onAboutClick
                    )
                }
            }
        }

        if (showClearAllDialog) {
            ClearAllDataDialog(
                onDismiss = { 
                    haptics.click()
                    showClearAllDialog = false 
                },
                onConfirm = {
                    haptics.heavy()
                    showClearAllDialog = false
                    viewModel.clearAllData()
                }
            )
        }

        if (showDisableLockWarningDialog) {
            DisableLockWarningDialog(
                onDismiss = { 
                    haptics.click()
                    showDisableLockWarningDialog = false 
                },
                onConfirm = {
                    haptics.heavy()
                    showDisableLockWarningDialog = false
                    viewModel.updateBiometricEnabled(false)
                }
            )
        }
    }
}