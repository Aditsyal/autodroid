package com.aditsyal.autodroid.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import com.aditsyal.autodroid.presentation.theme.MotionTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.utils.rememberDebouncedClick

@Composable
fun MacroCard(
    macro: MacroDTO,
    onToggle: (id: Long, enabled: Boolean) -> Unit,
    onExecute: (id: Long) -> Unit,
    onView: (id: Long) -> Unit,
    onEdit: (id: Long) -> Unit,
    onDelete: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
    sharedElementKey: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.Scale.Press else 1f,
        animationSpec = MotionTokens.MotionSpec.Press,
        label = "card_scale"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Card is not clickable, only buttons */ },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = macro.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = macro.description.ifBlank { "No description provided" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = macro.enabled,
                    onCheckedChange = { onToggle(macro.id, it) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroStatChip(label = "Triggers", count = macro.triggers.size)
                MacroStatChip(label = "Actions", count = macro.actions.size)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    onClick = rememberDebouncedClick { onExecute(macro.id) },
                    icon = Icons.Default.PlayArrow,
                    contentDescription = "Execute macro",
                    tint = MaterialTheme.colorScheme.primary
                )
                ActionButton(
                    onClick = { onView(macro.id) },
                    icon = Icons.Default.Visibility,
                    contentDescription = "View macro",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ActionButton(
                    onClick = { onEdit(macro.id) },
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit macro",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ActionButton(
                    onClick = { onDelete(macro.id) },
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete macro",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.Scale.Press else 1f,
        animationSpec = MotionTokens.MotionSpec.Press,
        label = "button_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .scale(buttonScale),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MacroStatChip(label: String, count: Int) {
    AssistChip(
        onClick = { },
        label = {
            Text("$label: $count")
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        shape = MaterialTheme.shapes.small
    )
}
