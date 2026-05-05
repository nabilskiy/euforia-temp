/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.soundscapes.scene.AvailableSoundUi
import digital.euforia.app.ui.soundscapes.scene.SceneMusicCategoryUi
import digital.euforia.app.ui.soundscapes.scene.SceneMusicUi
import digital.euforia.app.ui.soundscapes.scene.SoundCategoryUi
import digital.euforia.app.ui.soundscapes.scene.SoundFloatingButtonUi
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun MusicOptionsBottomSheetContent(
    musicTitle: String,
    musicCoverUrl: String?,
    isPlaying: Boolean,
    sceneMusicUrl: String?,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onChangeMusicClick: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White.copy(alpha = 0.14f)),
            ) {
                if (!musicCoverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = musicCoverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SoundscapeSceneMusicIndicatorThumbnailOverlay(
                        isPlaying = isPlaying,
                        sceneMusicUrl = sceneMusicUrl,
                        musicVolume = volume,
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    localizedRes.string(R.string.audio_scene_background_music_settings),
                    color = White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (musicTitle.isNotBlank()) {
                    Text(
                        text = musicTitle,
                        color = White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            localizedRes.string(R.string.audio_scene_volume),
            color = White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = White,
                activeTrackColor = White,
                inactiveTrackColor = White.copy(alpha = 0.28f)
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onChangeMusicClick).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(localizedRes.string(R.string.audio_scene_change_music), color = White, style = MaterialTheme.typography.titleSmall)
            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = White)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black)
        ) {
            Text(
                localizedRes.string(R.string.done),
                color = Black,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SoundLayerBottomSheetContent(
    title: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = White)
            }
        }
        HorizontalDivider(color = White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))
        Text(
            localizedRes.string(R.string.audio_scene_volume),
            color = White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = White,
                activeTrackColor = White,
                inactiveTrackColor = White.copy(alpha = 0.25f)
            )
        )
    }
}

@Composable
fun SoundsPickerBottomSheetContent(
    sounds: List<AvailableSoundUi>,
    categories: List<SoundCategoryUi>,
    defaultSceneSoundIds: List<Int>,
    suggestedSoundIds: List<Int>,
    sceneSoundButtons: List<SoundFloatingButtonUi>,
    initialSelectedIds: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
    onApply: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    val playingNowScroll = rememberScrollState()
    var query by remember(sounds) { mutableStateOf("") }
    var selectedIds by remember(initialSelectedIds, sounds) { mutableStateOf(initialSelectedIds) }
    val categoryById = categories.associateBy { it.id }
    val defaultIdSet = remember(defaultSceneSoundIds) { defaultSceneSoundIds.toSet() }

    val playingNowSounds = remember(sounds, defaultSceneSoundIds, selectedIds, query, sceneSoundButtons) {
        val soundById = sounds.associateBy { it.id }
        val buttonById = sceneSoundButtons.associateBy { it.id }
        val orderedSelectedIds = buildList {
            defaultSceneSoundIds.filter { it in selectedIds }.forEach(::add)
            selectedIds.filterNot { it in defaultIdSet }.sorted().forEach(::add)
        }
        orderedSelectedIds.mapNotNull { id ->
            val fromCatalog = soundById[id]
            if (fromCatalog != null) {
                fromCatalog.takeIf { soundMatchesQuery(it, query) }
            } else {
                val fb = buttonById[id]
                if (fb != null && (query.isBlank() || fb.title.lowercase().contains(query.trim().lowercase()))) {
                    AvailableSoundUi(
                        id = id,
                        categoryId = 0,
                        title = fb.title,
                        imageUrl = fb.imageUrl,
                        fileUrl = null
                    )
                } else null
            }
        }
    }

    val suggestedIdSet = remember(suggestedSoundIds) { suggestedSoundIds.toSet() }
    val suggestions = remember(sounds, suggestedSoundIds, query, defaultIdSet) {
        val soundsById = sounds.associateBy { it.id }
        suggestedSoundIds
            .mapNotNull { soundsById[it] }
            .filter { it.id !in defaultIdSet && soundMatchesQuery(it, query) }
    }
    val orderedCategories = remember(categories) { categories.orderedForSoundPicker() }

    Column(modifier = modifier.fillMaxHeight(0.9f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                localizedRes.string(R.string.sounds_title),
                color = White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onDismiss) {
                Icon(painter = painterResource(R.drawable.ic_close), contentDescription = null, tint = White)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(localizedRes.string(R.string.scenes_search), color = White.copy(alpha = 0.5f)) },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.65f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = White,
                focusedTextColor = White,
                unfocusedBorderColor = White.copy(alpha = 0.2f),
                focusedBorderColor = White.copy(alpha = 0.45f),
                unfocusedContainerColor = White.copy(alpha = 0.06f),
                focusedContainerColor = White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item("playing_now_title") {
                Text(localizedRes.string(R.string.sounds_section_play_now), color = White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
            }
            item("playing_now_items") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(playingNowSounds, key = { it.id }) { sound ->
                        SoundPickerItem(
                            title = sound.title,
                            imageUrl = sound.imageUrl,
                            subtitle = categoryById[sound.categoryId]?.title,
                            selected = sound.id in selectedIds,
                            onClick = {
                                val updated = toggleSoundSelection(selectedIds, sound.id)
                                selectedIds = updated
                                onSelectionChanged(updated)
                            }
                        )
                    }
                }
            }
            if (suggestions.isNotEmpty()) {
                item("suggestions_title") {
                    Text(
                        text = localizedRes.string(R.string.sounds_category_suggestions),
                        color = White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                suggestions.chunked(4).forEachIndexed { idx, row ->
                    item("suggestions_row_$idx") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { sound ->
                                SoundPickerItem(
                                    title = sound.title,
                                    imageUrl = sound.imageUrl,
                                    subtitle = null,
                                    selected = sound.id in selectedIds,
                                    onClick = {
                                        val updated = toggleSoundSelection(selectedIds, sound.id)
                                        selectedIds = updated
                                        onSelectionChanged(updated)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            orderedCategories.forEach { category ->
                val categorySounds = sounds
                    .filter {
                        it.categoryId == category.id &&
                            it.id !in defaultIdSet &&
                            it.id !in suggestedIdSet &&
                            soundMatchesQuery(it, query)
                    }
                    .sortedBy { it.title }
                if (categorySounds.isEmpty()) return@forEach
                item("cat_title_${category.id}") {
                    Text(
                        text = category.title,
                        color = White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                categorySounds.chunked(4).forEachIndexed { idx, row ->
                    item("cat_${category.id}_row_$idx") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { sound ->
                                SoundPickerItem(
                                    title = sound.title,
                                    imageUrl = sound.imageUrl,
                                    subtitle = null,
                                    selected = sound.id in selectedIds,
                                    onClick = {
                                        val updated = toggleSoundSelection(selectedIds, sound.id)
                                        selectedIds = updated
                                        onSelectionChanged(updated)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = {
                    selectedIds = emptySet()
                    onSelectionChanged(emptySet())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedRes.string(R.string.sounds_picker_clear), color = White)
            }
            Button(
                onClick = { onApply(selectedIds) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black)
            ) {
                Text(
                    localizedRes.string(R.string.sounds_picker_apply),
                    color = Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun MusicPickerBottomSheetContent(
    music: List<SceneMusicUi>,
    categories: List<SceneMusicCategoryUi>,
    suggestedMusicIds: List<Int>,
    favoriteMusicIds: Set<Int>,
    initialSelectedId: Int?,
    isScenePlaying: Boolean,
    musicVolume: Float,
    onFavoriteClick: (Int) -> Unit,
    onMusicClick: (Int?) -> Unit,
    onDismiss: () -> Unit,
    onApply: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    var query by remember(music) { mutableStateOf("") }
    var selectedId by remember(initialSelectedId, music) { mutableStateOf(initialSelectedId) }
    var expandedCategoryIds by remember(categories) {
        mutableStateOf(categories.map { it.id }.toSet())
    }
    var favoritesExpanded by remember(favoriteMusicIds) { mutableStateOf(favoriteMusicIds.isNotEmpty()) }
    var popularExpanded by remember(suggestedMusicIds) { mutableStateOf(true) }
    val suggestedMusicIdSet = remember(suggestedMusicIds) { suggestedMusicIds.toSet() }
    val favoriteMusicIdSet = remember(favoriteMusicIds) { favoriteMusicIds }
    val suggestions = remember(music, suggestedMusicIds, query) {
        val musicById = music.associateBy { it.id }
        suggestedMusicIds
            .mapNotNull { musicById[it] }
            .filter { musicMatchesQuery(it, query) }
    }
    val favoriteItems = remember(music, favoriteMusicIdSet, query) {
        music.filter { it.id in favoriteMusicIdSet && musicMatchesQuery(it, query) }
    }
    val orderedCategories = remember(categories) { categories.orderedForMusicPicker() }

    Column(modifier = modifier.fillMaxHeight(0.9f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                localizedRes.string(R.string.audio_scene_select_background_sound),
                color = White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onDismiss) {
                Icon(painter = painterResource(R.drawable.ic_close), contentDescription = null, tint = White)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(localizedRes.string(R.string.scenes_search), color = White.copy(alpha = 0.5f)) },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.65f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = White,
                focusedTextColor = White,
                unfocusedBorderColor = White.copy(alpha = 0.2f),
                focusedBorderColor = White.copy(alpha = 0.45f),
                unfocusedContainerColor = White.copy(alpha = 0.06f),
                focusedContainerColor = White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
        ) {
            if (suggestions.isNotEmpty()) {
                item("music_suggestions_title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { popularExpanded = !popularExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedRes.string(R.string.music_category_suggestions),
                            color = White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (popularExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.75f)
                        )
                    }
                }
                if (popularExpanded) {
                    item("music_suggestions_card") {
                        MusicPickerCategoryCard(
                            items = suggestions,
                            selectedId = selectedId,
                            isScenePlaying = isScenePlaying,
                            musicVolume = musicVolume,
                            favoriteMusicIdSet = favoriteMusicIdSet,
                            onFavoriteClick = onFavoriteClick,
                            onRowClick = { musicItem ->
                                selectedId = musicItem.id
                                onMusicClick(musicItem.id)
                            },
                        )
                    }
                }
            }
            if (favoriteItems.isNotEmpty()) {
                item("music_favorites_title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { favoritesExpanded = !favoritesExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedRes.string(R.string.sounds_category_favorites),
                            color = White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (favoritesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.75f)
                        )
                    }
                }
                if (favoritesExpanded) {
                    item("music_favorites_card") {
                        MusicPickerCategoryCard(
                            items = favoriteItems,
                            selectedId = selectedId,
                            isScenePlaying = isScenePlaying,
                            musicVolume = musicVolume,
                            favoriteMusicIdSet = favoriteMusicIdSet,
                            onFavoriteClick = onFavoriteClick,
                            onRowClick = { musicItem ->
                                selectedId = musicItem.id
                                onMusicClick(musicItem.id)
                            },
                        )
                    }
                }
            }
            orderedCategories.forEach { category ->
                val categoryMusic = music
                    .filter {
                        it.categoryId == category.id &&
                            it.id !in suggestedMusicIdSet &&
                            it.id !in favoriteMusicIdSet &&
                            musicMatchesQuery(it, query)
                    }
                if (categoryMusic.isEmpty()) return@forEach
                val expanded = category.id in expandedCategoryIds
                item("music_cat_header_${category.id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                expandedCategoryIds = if (expanded) {
                                    expandedCategoryIds - category.id
                                } else {
                                    expandedCategoryIds + category.id
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.title,
                            color = White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.75f)
                        )
                    }
                }
                if (expanded) {
                    item("music_cat_card_${category.id}") {
                        MusicPickerCategoryCard(
                            items = categoryMusic,
                            selectedId = selectedId,
                            isScenePlaying = isScenePlaying,
                            musicVolume = musicVolume,
                            favoriteMusicIdSet = favoriteMusicIdSet,
                            onFavoriteClick = onFavoriteClick,
                            onRowClick = { musicItem ->
                                selectedId = musicItem.id
                                onMusicClick(musicItem.id)
                            },
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { selectedId = null }, modifier = Modifier.weight(1f)) {
                Text(localizedRes.string(R.string.sounds_picker_clear), color = White)
            }
            Button(
                onClick = { onApply(selectedId) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black)
            ) {
                Text(
                    localizedRes.string(R.string.sounds_picker_apply),
                    color = Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private val MusicPickerCategoryCardShape = RoundedCornerShape(16.dp)

@Composable
private fun MusicPickerCategoryCard(
    items: List<SceneMusicUi>,
    selectedId: Int?,
    isScenePlaying: Boolean,
    musicVolume: Float,
    favoriteMusicIdSet: Set<Int>,
    onFavoriteClick: (Int) -> Unit,
    onRowClick: (SceneMusicUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(MusicPickerCategoryCardShape)
            .background(White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        items.forEachIndexed { index, musicItem ->
            MusicPickerRowItem(
                item = musicItem,
                selected = selectedId == musicItem.id,
                showPlayingOverlay = selectedId == musicItem.id,
                isScenePlaying = isScenePlaying,
                musicVolume = musicVolume,
                isFavorite = musicItem.id in favoriteMusicIdSet,
                onFavoriteClick = { onFavoriteClick(musicItem.id) },
                onClick = { onRowClick(musicItem) },
                showBottomDivider = index < items.lastIndex,
            )
        }
    }
}

@Composable
private fun MusicPickerRowItem(
    item: SceneMusicUi,
    selected: Boolean,
    showPlayingOverlay: Boolean,
    isScenePlaying: Boolean,
    musicVolume: Float,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    showBottomDivider: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White.copy(alpha = 0.08f))
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (showPlayingOverlay) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SoundscapeSceneMusicIndicatorThumbnailOverlay(
                            isPlaying = isScenePlaying,
                            sceneMusicUrl = item.fileUrl,
                            musicVolume = musicVolume,
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = item.title,
                color = White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = White.copy(alpha = if (isFavorite) 1f else 0.88f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (showBottomDivider) {
            HorizontalDivider(
                color = White.copy(alpha = if (selected) 0.25f else 0.14f),
                modifier = Modifier.padding(top = 10.dp, start = 70.dp)
            )
        }
    }
}

private fun soundMatchesQuery(s: AvailableSoundUi, query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    return s.title.lowercase().contains(q)
}

private fun SoundCategoryUi.isSuggestionsCategory(): Boolean =
    title.contains("suggestion", ignoreCase = true) ||
        alias.contains("suggestion", ignoreCase = true) ||
        alias.equals("suggestions", ignoreCase = true)

private fun List<SoundCategoryUi>.orderedForSoundPicker(): List<SoundCategoryUi> {
    val suggestions = filter { it.isSuggestionsCategory() }.minByOrNull { it.position }
    val rest = filter { it.id != suggestions?.id }.sortedWith(compareBy({ it.position }, { it.id }))
    return listOfNotNull(suggestions) + rest
}

private fun SceneMusicCategoryUi.isSuggestionsCategory(): Boolean =
    title.contains("suggestion", ignoreCase = true) ||
        alias.contains("suggestion", ignoreCase = true) ||
        alias.equals("suggestions", ignoreCase = true)

private fun List<SceneMusicCategoryUi>.orderedForMusicPicker(): List<SceneMusicCategoryUi> {
    val suggestions = filter { it.isSuggestionsCategory() }.minByOrNull { it.position }
    val rest = filter { it.id != suggestions?.id }.sortedWith(compareBy({ it.position }, { it.id }))
    return listOfNotNull(suggestions) + rest
}

private fun musicMatchesQuery(item: SceneMusicUi, query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    return item.title.lowercase().contains(q)
}

private fun toggleSoundSelection(selected: Set<Int>, soundId: Int): Set<Int> {
    if (soundId in selected) return selected - soundId
    if (selected.size >= 10) return selected
    return selected + soundId
}

@Composable
private fun SoundPickerItem(
    title: String,
    imageUrl: String?,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.width(76.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0xFF2B4AA9) else White.copy(alpha = 0.12f))
                .border(1.dp, White.copy(alpha = if (selected) 0.9f else 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_sounds),
                    contentDescription = title,
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
