/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.SavedSoundscapeDao
import digital.euforia.app.data.db.dao.SceneCategoryDao
import digital.euforia.app.data.db.dao.SceneDao
import digital.euforia.app.data.db.dao.SoundscapeDownloadDao
import digital.euforia.app.data.db.dao.SoundscapePlaylistDao
import digital.euforia.app.data.db.dao.SoundscapePresetDao
import digital.euforia.app.data.db.dao.SoundscapeSceneLocalStateDao
import digital.euforia.app.data.db.dao.SoundscapeSoundDao
import digital.euforia.app.data.db.entity.SavedSoundscape
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SceneCategory
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import digital.euforia.app.data.db.entity.SoundscapeSound
import digital.euforia.app.data.db.entity.SoundscapeSoundCategory
import digital.euforia.app.data.model.NetworkCategory
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.data.soundscapes.encodeSavedPayloadJson
import digital.euforia.app.data.soundscapes.emptySavedPayloadJson
import digital.euforia.app.domain.soundscapes.copySceneIdFromPresetId
import digital.euforia.app.domain.usecase.soundscapes.mergeSoundscapeSceneLocalBackground
import digital.euforia.app.domain.usecase.soundscapes.mergeSoundscapeScenesWithLocalBackground
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapesRepository @Inject constructor(
    private val api: EuforiaApi,
    private val sceneCategoryDao: SceneCategoryDao,
    private val sceneDao: SceneDao,
    private val soundDao: SoundscapeSoundDao,
    private val localStateDao: SoundscapeSceneLocalStateDao,
    private val playlistDao: SoundscapePlaylistDao,
    private val presetDao: SoundscapePresetDao,
    private val downloadDao: SoundscapeDownloadDao,
    private val savedSoundscapeDao: SavedSoundscapeDao,
) {
    private val seedMutex = Mutex()
    @Volatile
    private var savedSeeded = false

    suspend fun ensureSavedSoundscapesSeeded() {
        if (savedSeeded) return
        seedMutex.withLock {
            if (savedSeeded) return
            seedSavedSoundscapesFromLegacy()
            savedSeeded = true
        }
    }

    private suspend fun seedSavedSoundscapesFromLegacy() {
        val presets = presetDao.getAll()
        for (preset in presets) {
            if (savedSoundscapeDao.getById(preset.id) != null) continue
            val copyId = copySceneIdFromPresetId(preset.id)
            val localAtCopy = localStateDao.getBySceneId(copyId)
            val localAtSource = localStateDao.getBySceneId(preset.sceneId)
            if (localAtCopy == null && localAtSource != null) {
                localStateDao.upsert(localAtSource.copy(sceneId = copyId))
            }
            val effective = localStateDao.getBySceneId(copyId)
            val payloadJson = if (effective != null) {
                encodeSavedPayloadJson(preset.sceneId, effective)
            } else {
                emptySavedPayloadJson(preset.sceneId)
            }
            savedSoundscapeDao.upsert(
                SavedSoundscape(
                    id = preset.id,
                    sourceCatalogSceneId = preset.sceneId,
                    name = preset.name,
                    payloadJson = payloadJson,
                )
            )
        }
    }

    suspend fun syncCatalog(): ResultWrapper<Unit> {
        val categories = api.getSceneCategories().dataOrNull.orEmpty()
        return api.scenes().flatMap { flatScenes ->
            api.playlists().map { playlists ->
                val sceneIdToCategoryId = LinkedHashMap<Int, Int>()
                categories.forEach { cat ->
                    cat.scenes.forEach { ns ->
                        sceneIdToCategoryId.putIfAbsent(ns.id, cat.id)
                    }
                }
                val categoryEntities = categories.mapIndexed { index, c -> c.toSceneCategoryEntity(index) }
                if (categoryEntities.isNotEmpty()) {
                    sceneCategoryDao.upsertAll(categoryEntities)
                    sceneCategoryDao.deleteAllExcept(categoryEntities.map { it.id })
                } else {
                    sceneCategoryDao.clear()
                }
                val sceneEntities = if (flatScenes.isNotEmpty()) {
                    flatScenes.map { ns ->
                        val cid = sceneIdToCategoryId[ns.id] ?: ns.categoryId
                        ns.toEntity(resolvedCategoryId = cid)
                    }
                } else {
                    categories.flatMap { cat ->
                        cat.scenes.map { it.toEntity(resolvedCategoryId = cat.id) }
                    }
                }
                val playlistEntities = playlists.map { it.toEntity() }.map { incoming ->
                    mergePlaylistWithStored(incoming)
                }
                sceneDao.upsertAll(sceneEntities)
                playlistDao.upsertAll(playlistEntities)
                val sceneIdsToKeep = buildSet {
                    addAll(sceneEntities.map { it.id })
                    playlistEntities.forEach { addAll(it.sceneIds) }
                }
                if (sceneIdsToKeep.isNotEmpty()) {
                    sceneDao.deleteAllExcept(sceneIdsToKeep.toList())
                }
                if (playlistEntities.isNotEmpty()) {
                    playlistDao.deleteAllExcept(playlistEntities.map { it.id })
                }
            }
        }
    }

    fun getSceneCategoriesFlow(): Flow<List<SceneCategory>> = sceneCategoryDao.getAllOrderedFlow()
    fun getScenesFlow(): Flow<List<Scene>> = sceneDao.getAllFlow()

    fun getSceneFlow(id: Int): Flow<Scene?> = sceneDao.getByIdFlow(id)

    fun getLocalSceneStatesFlow(): Flow<Map<Int, SoundscapeSceneLocalState>> =
        localStateDao.observeAll().map { rows -> rows.associateBy { it.sceneId } }

    fun getSavedSoundscapesFlow(): Flow<List<SavedSoundscape>> = savedSoundscapeDao.observeAllFlow()

    suspend fun getSavedSoundscape(id: Int): SavedSoundscape? = savedSoundscapeDao.getById(id)

    suspend fun upsertSavedSoundscape(item: SavedSoundscape) = savedSoundscapeDao.upsert(item)

    suspend fun ensureSavedRowForNewPreset(preset: SoundscapePreset) {
        if (savedSoundscapeDao.getById(preset.id) != null) return
        val copyId = copySceneIdFromPresetId(preset.id)
        val localAtCopy = localStateDao.getBySceneId(copyId)
        val localAtSource = localStateDao.getBySceneId(preset.sceneId)
        if (localAtCopy == null && localAtSource != null) {
            localStateDao.upsert(localAtSource.copy(sceneId = copyId))
        }
        val effective = localStateDao.getBySceneId(copyId)
        val payloadJson = if (effective != null) {
            encodeSavedPayloadJson(preset.sceneId, effective)
        } else {
            emptySavedPayloadJson(preset.sceneId)
        }
        savedSoundscapeDao.upsert(
            SavedSoundscape(
                id = preset.id,
                sourceCatalogSceneId = preset.sceneId,
                name = preset.name,
                payloadJson = payloadJson,
            )
        )
    }

    suspend fun deleteSavedSoundscape(id: Int) {
        savedSoundscapeDao.deleteById(id)
    }

    suspend fun getSceneById(id: Int): Scene? {
        val scene = sceneDao.getById(id) ?: return null
        return scene.mergeSoundscapeSceneLocalBackground(localStateDao.getBySceneId(id))
    }
    suspend fun getSceneDetails(id: Int): ResultWrapper<NetworkScene> {
        return api.scene(id).onSuccess { networkScene ->
            sceneDao.upsertAll(listOf(networkScene.toEntity()))
        }
    }
    suspend fun syncSoundsCatalog(): ResultWrapper<Unit> {
        return api.getSoundCategories().flatMap { categories ->
            api.sounds().map { sounds ->
                val categoryEntities = categories.mapIndexed { index, category ->
                    category.toEntity(index)
                }
                val soundEntities = sounds.map { it.toEntity() }
                if (categoryEntities.isNotEmpty()) {
                    soundDao.upsertCategories(categoryEntities)
                    soundDao.deleteCategoriesExcept(categoryEntities.map { it.id })
                } else {
                    soundDao.clearCategories()
                }
                if (soundEntities.isNotEmpty()) {
                    soundDao.upsertSounds(soundEntities)
                    soundDao.deleteSoundsExcept(soundEntities.map { it.id })
                } else {
                    soundDao.clearSounds()
                }
            }
        }
    }
    fun getAllSoundsFlow(): Flow<List<SoundscapeSound>> = soundDao.getAllSoundsFlow()
    suspend fun getAllSounds(): List<SoundscapeSound> = soundDao.getAllSounds()
    fun getAllSoundCategoriesFlow(): Flow<List<SoundscapeSoundCategory>> = soundDao.getAllCategoriesFlow()

    fun getPlaylistsFlow(): Flow<List<SoundscapePlaylist>> = playlistDao.getAllFlow()
    fun getPlaylistFlow(id: Int): Flow<SoundscapePlaylist?> = playlistDao.getByIdFlow(id)
    suspend fun getPlaylistById(id: Int): SoundscapePlaylist? = playlistDao.getById(id)
    suspend fun getScenesByIds(ids: List<Int>): List<Scene> {
        if (ids.isEmpty()) return emptyList()
        val scenes = sceneDao.getByIds(ids)
        if (scenes.isEmpty()) return emptyList()
        val locals = localStateDao.getBySceneIds(ids)
        return mergeSoundscapeScenesWithLocalBackground(scenes, locals.associateBy { it.sceneId })
    }
    suspend fun getPlaylistDetails(id: Int): ResultWrapper<SoundscapePlaylist> {
        return api.playlist(id).map { playlist ->
            val sceneEntities = playlist.scenes.map { it.toEntity() }
            val entity = playlist.toEntity()
            if (sceneEntities.isNotEmpty()) {
                sceneDao.upsertAll(sceneEntities)
            }
            playlistDao.upsertAll(listOf(entity))
            entity
        }
    }
    suspend fun getLocalSceneState(sceneId: Int): SoundscapeSceneLocalState? = localStateDao.getBySceneId(sceneId)
    suspend fun upsertLocalSceneState(state: SoundscapeSceneLocalState) = localStateDao.upsert(state)
    suspend fun deleteLocalSceneState(sceneId: Int) = localStateDao.deleteBySceneId(sceneId)

    suspend fun searchScenes(query: String): ResultWrapper<List<Scene>> {
        return api.search(query = query, searchScenes = 1).map { response ->
            response.scenes.map { it.toEntity() }
        }
    }

    fun getPresetsFlow(): Flow<List<SoundscapePreset>> = presetDao.getAllFlow()
    suspend fun upsertPreset(preset: SoundscapePreset): Int = presetDao.upsert(preset).toInt()
    suspend fun getPresetById(id: Int): SoundscapePreset? = presetDao.getById(id)
    suspend fun deletePresetsBySceneId(sceneId: Int) = presetDao.deleteBySceneId(sceneId)
    suspend fun deletePreset(id: Int) = presetDao.deleteById(id)

    fun getDownloadsFlow(): Flow<List<SoundscapeDownloadItem>> = downloadDao.getAllFlow()
    suspend fun getDownloadsSnapshot(): List<SoundscapeDownloadItem> = downloadDao.getAll()
    suspend fun queueDownload(item: SoundscapeDownloadItem) = downloadDao.upsert(item)
    suspend fun updateDownload(item: SoundscapeDownloadItem) = downloadDao.upsert(item)
    suspend fun getDownload(id: String): SoundscapeDownloadItem? = downloadDao.getById(id)
    suspend fun deleteDownload(id: String) = downloadDao.deleteById(id)
    suspend fun clearDownloads() = downloadDao.deleteAll()

    suspend fun reconcileDownloads(existingPaths: Set<String>) {
        val current = downloadDao.getAllFlow().firstOrNull().orEmpty()
        current.forEach { item ->
            if (item.localPath != null && !existingPaths.contains(item.localPath)) {
                downloadDao.upsert(item.copy(status = SoundscapeDownloadItem.STATUS_EXPIRED, progress = 0))
            }
        }
    }

    /** List API may omit `scene_ids`; keep ids loaded earlier via playlist details. */
    private suspend fun mergePlaylistWithStored(incoming: SoundscapePlaylist): SoundscapePlaylist {
        if (incoming.sceneIds.isNotEmpty()) return incoming
        val stored = playlistDao.getById(incoming.id) ?: return incoming
        if (stored.sceneIds.isEmpty()) return incoming
        return incoming.copy(sceneIds = stored.sceneIds)
    }
}

private fun NetworkCategory.toSceneCategoryEntity(index: Int) = SceneCategory(
    id = id,
    alias = alias,
    name = name,
    position = position ?: index,
)
