/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.Red
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.OptionsMenu

@Composable
fun SoundscapeSceneTopBar(
    title: String,
    subtitle: String,
    showMaxBadge: Boolean,
    isDownloaded: Boolean,
    onCollapse: () -> Unit,
    onSaveChanges: () -> Unit,
    onSaveAndDownload: (String) -> Unit,
    onRenameScene: (String) -> Unit,
    onDeleteDownloaded: () -> Unit,
    hasActiveTimer: Boolean,
    onTimer1hClick: () -> Unit,
    onTimer2hClick: () -> Unit,
    onTimerSetupClick: () -> Unit,
    onTimerStopClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onShare: () -> Unit,
    canShare: Boolean,
    onModalVisibilityChanged: (Boolean) -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    var showMenu by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showSaveAndDownloadDialog by remember { mutableStateOf(false) }
    var renameValue by remember(title) { mutableStateOf(title) }
    var saveAndDownloadName by remember(title) { mutableStateOf(title) }
    LaunchedEffect(showMenu, showRename, showSaveAndDownloadDialog) {
        onModalVisibilityChanged(showMenu || showRename || showSaveAndDownloadDialog)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = localizedRes.string(R.string.soundscape_collapse),
                tint = White
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showMaxBadge) {
                    Spacer(Modifier.size(8.dp))
                    MaxView()
                }
            }
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = localizedRes.string(R.string.soundscape_more_actions),
                    tint = White
                )
            }
            OptionsMenu(
                expanded = showMenu,
                onExpandedChange = { showMenu = it },
                menuItems = if (isDownloaded) {
                    val timerSubmenu = listOf(
                        MenuItem(
                            titleRes = R.string.audio_scene_menu_timer_1h,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimer1hClick
                        ),
                        MenuItem(
                            titleRes = R.string.audio_scene_menu_timer_2h,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimer2hClick
                        ),
                        MenuItem(
                            titleRes = if (hasActiveTimer) R.string.audio_scene_menu_timer_edit else R.string.audio_scene_menu_timer_set,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimerSetupClick
                        ),
                    ) + if (hasActiveTimer) {
                        listOf(
                            MenuItem(
                                titleRes = R.string.audio_scene_menu_timer_stop,
                                color = Red,
                                onClick = onTimerStopClick
                            )
                        )
                    } else emptyList()
                    buildList {
                        add(
                            MenuItem(
                                titleRes = R.string.timer,
                                iconRes = R.drawable.ic_timer,
                                children = timerSubmenu,
                                onClick = {}
                            )
                        )
                        if (canShare) {
                            add(
                                MenuItem(
                                    titleRes = R.string.share,
                                    iconRes = R.drawable.ic_share,
                                    onClick = onShare
                                )
                            )
                        }
                        add(
                            MenuItem(
                                titleRes = R.string.scenes_preferences,
                                iconRes = R.drawable.ic_nav_settings,
                                onClick = onPreferencesClick
                            )
                        )
                        add(
                            MenuItem(
                                titleRes = R.string.scenes_rename,
                                onClick = { showRename = true }
                            )
                        )
                        add(
                            MenuItem(
                                titleRes = R.string.audio_scene_save_changes,
                                iconRes = R.drawable.ic_done,
                                onClick = onSaveChanges
                            )
                        )
                        add(
                            MenuItem(
                                titleRes = R.string.scenes_delete,
                                color = Red,
                                onClick = onDeleteDownloaded
                            )
                        )
                    }
                } else {
                    val timerSubmenu = listOf(
                        MenuItem(
                            titleRes = R.string.audio_scene_menu_timer_1h,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimer1hClick
                        ),
                        MenuItem(
                            titleRes = R.string.audio_scene_menu_timer_2h,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimer2hClick
                        ),
                        MenuItem(
                            titleRes = if (hasActiveTimer) R.string.audio_scene_menu_timer_edit else R.string.audio_scene_menu_timer_set,
                            iconRes = R.drawable.ic_timer,
                            onClick = onTimerSetupClick
                        ),
                    ) + if (hasActiveTimer) {
                        listOf(
                            MenuItem(
                                titleRes = R.string.audio_scene_menu_timer_stop,
                                color = Red,
                                onClick = onTimerStopClick
                            )
                        )
                    } else emptyList()
                    buildList {
                        add(
                            MenuItem(
                                titleRes = R.string.timer,
                                iconRes = R.drawable.ic_timer,
                                children = timerSubmenu,
                                onClick = {}
                            )
                        )
                        if (canShare) {
                            add(
                                MenuItem(
                                    titleRes = R.string.share,
                                    iconRes = R.drawable.ic_share,
                                    onClick = onShare
                                )
                            )
                        }
                        add(
                            MenuItem(
                                titleRes = R.string.scenes_preferences,
                                iconRes = R.drawable.ic_nav_settings,
                                onClick = onPreferencesClick
                            )
                        )
                        add(
                            MenuItem(
                                titleRes = R.string.scenes_save_download,
                                iconRes = R.drawable.ic_done,
                                onClick = {
                                    saveAndDownloadName = title
                                    showSaveAndDownloadDialog = true
                                }
                            )
                        )
                    }
                }
            )
        }
    }
    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            confirmButton = {
                TextButton(onClick = {
                    onRenameScene(renameValue)
                    showRename = false
                }) { Text(localizedRes.string(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text(localizedRes.string(R.string.cancel)) }
            },
            title = { Text(localizedRes.string(R.string.scenes_rename)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true
                )
            }
        )
    }
    if (showSaveAndDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAndDownloadDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onSaveAndDownload(saveAndDownloadName)
                    showSaveAndDownloadDialog = false
                }) { Text(localizedRes.string(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAndDownloadDialog = false }) {
                    Text(localizedRes.string(R.string.cancel))
                }
            },
            title = { Text(localizedRes.string(R.string.rename_audio_scene_title)) },
            text = {
                Column {
                    Text(
                        text = localizedRes.string(R.string.save_audio_scene_message),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = saveAndDownloadName,
                        onValueChange = { saveAndDownloadName = it },
                        singleLine = true,
                        placeholder = {
                            Text(localizedRes.string(R.string.save_audio_scene_name_placeholder))
                        }
                    )
                }
            }
        )
    }
}
