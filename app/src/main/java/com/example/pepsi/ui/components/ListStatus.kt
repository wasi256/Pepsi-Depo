package com.example.pepsi.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Renders a loading spinner, an error message, or an empty-state message —
 * whichever applies. Callers only need to render their content when none apply.
 */
@Composable
fun ListStatus(
    isLoading: Boolean,
    error: String?,
    isEmpty: Boolean,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> Row(modifier = modifier.padding(16.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        error != null -> Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(16.dp),
        )
        isEmpty -> Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(16.dp),
        )
    }
}
