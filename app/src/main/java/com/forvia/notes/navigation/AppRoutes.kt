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

package com.forvia.notes.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoutes {

    @Serializable
    data object Home : AppRoutes

    @Serializable
    data object Settings : AppRoutes

    @Serializable
    data object Archive : AppRoutes

    @Serializable
    data object Trash : AppRoutes

    @Serializable
    data object AddCategory : AppRoutes

    @Serializable
    data object BackupRestore : AppRoutes

    @Serializable
    data object PrivacyPolicy : AppRoutes

    @Serializable
    data object About : AppRoutes

    @Serializable
    data class NoteDetail(
        val noteId: Int = -1,
        val category: String? = null
    ) : AppRoutes
}