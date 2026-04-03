/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.SceneCategoryDao
import digital.euforia.app.data.db.dao.SceneDao
import digital.euforia.app.data.db.dao.SoundscapeDownloadDao
import digital.euforia.app.data.db.dao.SoundscapePlaylistDao
import digital.euforia.app.data.db.dao.SoundscapePresetDao
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SceneCategory
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.model.NetworkCategory
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapesRepository @Inject constructor(
    private val api: EuforiaApi,
    private val sceneCategoryDao: SceneCategoryDao,
    private val sceneDao: SceneDao,
    private val playlistDao: SoundscapePlaylistDao,
    private val presetDao: SoundscapePresetDao,
    private val downloadDao: SoundscapeDownloadDao,
) {
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
                val playlistEntities = playlists.map { it.toEntity() }
                sceneDao.upsertAll(sceneEntities)
                playlistDao.upsertAll(playlistEntities)
                if (sceneEntities.isNotEmpty()) {
                    sceneDao.deleteAllExcept(sceneEntities.map { it.id })
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

    suspend fun getSceneById(id: Int): Scene? = sceneDao.getById(id)
    suspend fun getSceneDetails(id: Int): ResultWrapper<NetworkScene> {
        return api.scene(id).onSuccess { networkScene ->
            sceneDao.upsertAll(listOf(networkScene.toEntity()))
        }
    }
    fun getPlaylistsFlow(): Flow<List<SoundscapePlaylist>> = playlistDao.getAllFlow()

    suspend fun searchScenes(query: String): ResultWrapper<List<Scene>> {
        return api.search(query = query, searchScenes = 1).map { response ->
            response.scenes.map { it.toEntity() }
        }
    }

    fun getPresetsFlow(): Flow<List<SoundscapePreset>> = presetDao.getAllFlow()
    suspend fun upsertPreset(preset: SoundscapePreset) = presetDao.upsert(preset)
    suspend fun deletePreset(id: Int) = presetDao.deleteById(id)

    fun getDownloadsFlow(): Flow<List<SoundscapeDownloadItem>> = downloadDao.getAllFlow()
    suspend fun queueDownload(item: SoundscapeDownloadItem) = downloadDao.upsert(item)
    suspend fun updateDownload(item: SoundscapeDownloadItem) = downloadDao.upsert(item)
    suspend fun getDownload(id: String): SoundscapeDownloadItem? = downloadDao.getById(id)
    suspend fun deleteDownload(id: String) = downloadDao.deleteById(id)

    suspend fun reconcileDownloads(existingPaths: Set<String>) {
        val current = downloadDao.getAllFlow().firstOrNull().orEmpty()
        current.forEach { item ->
            if (item.localPath != null && !existingPaths.contains(item.localPath)) {
                downloadDao.upsert(item.copy(status = SoundscapeDownloadItem.STATUS_EXPIRED, progress = 0))
            }
        }
    }
}

private fun NetworkCategory.toSceneCategoryEntity(index: Int) = SceneCategory(
    id = id,
    alias = alias,
    name = name,
    position = position ?: index,
)

