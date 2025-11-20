package com.openapps.jotter.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Merge "All" + categories for the display list
    val displayList = listOf("All") + categories

    // Your Scroll Logic (Kept this, it's good!)
    LaunchedEffect(selectedCategory) {
        val index = displayList.indexOf(selectedCategory)
        if (index >= 0) {
            val layoutInfo = listState.layoutInfo
            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }

            val animSpec = tween<Float>(
                durationMillis = 500, // Reduced from 700ms to 500ms for snappier feel
                easing = FastOutSlowInEasing
            )

            if (visibleItem != null) {
                val itemStart = visibleItem.offset
                val itemEnd = itemStart + visibleItem.size
                val viewportEnd = layoutInfo.viewportEndOffset
                // 16dp buffer (matches screen padding)
                val padding = with(density) { 16.dp.toPx() }

                if (itemEnd > viewportEnd) {
                    listState.animateScrollBy((itemEnd - viewportEnd) + padding, animSpec)
                } else if (itemStart < 0) {
                    listState.animateScrollBy(itemStart.toFloat() - padding, animSpec)
                }
            } else {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        // Zero top padding to sit flush with Header
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(displayList) { _, category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = {
                    // RESTORED: Logic to toggle off and go back to "All"
                    if (isSelected && category != "All") {
                        onCategorySelect("All")
                    } else {
                        onCategorySelect(category)
                    }
                },
                label = { Text(text = category) },
                // RESTORED: 8.dp corners
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            // RESTORED: Using FilterChip ensures exact same height/shape as categories
            FilterChip(
                selected = false,
                onClick = onAddCategoryClick,
                label = { Text("Add") },
                leadingIcon = {
                    // RESTORED: Rounded Icon
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    enabled = true,
                    selected = false
                )
            )
        }
    }
}