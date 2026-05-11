/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.soundscapes

import digital.euforia.app.data.db.entity.SavedSoundscape
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import org.json.JSONObject

private const val KEY_SCHEMA = "schemaVersion"
private const val KEY_SOURCE = "sourceCatalogSceneId"
private const val KEY_LOCAL = "localState"

const val SOUNDSCAPE_SAVED_PAYLOAD_SCHEMA_V1 = 1

fun emptySavedPayloadJson(sourceCatalogSceneId: Int): String =
    JSONObject()
        .put(KEY_SCHEMA, SOUNDSCAPE_SAVED_PAYLOAD_SCHEMA_V1)
        .put(KEY_SOURCE, sourceCatalogSceneId)
        .put(KEY_LOCAL, JSONObject())
        .toString()

fun encodeSavedPayloadJson(sourceCatalogSceneId: Int, local: SoundscapeSceneLocalState): String {
    val localJson = JSONObject()
        .put("musicVolume", local.musicVolume.toDouble())
        .put("selectedMusicUrl", local.selectedMusicUrl ?: JSONObject.NULL)
        .put("selectedMusicTitle", local.selectedMusicTitle ?: JSONObject.NULL)
        .put("layersJson", local.layersJson)
        .put("buttonsJson", local.buttonsJson)
        .put("backgroundImageUrl", local.backgroundImageUrl ?: JSONObject.NULL)
        .put("backgroundVideoUrl", local.backgroundVideoUrl ?: JSONObject.NULL)
        .put("backgroundSource", local.backgroundSource ?: JSONObject.NULL)
    if (local.selectedMusicId != null) {
        localJson.put("selectedMusicId", local.selectedMusicId)
    } else {
        localJson.put("selectedMusicId", JSONObject.NULL)
    }
    return JSONObject()
        .put(KEY_SCHEMA, SOUNDSCAPE_SAVED_PAYLOAD_SCHEMA_V1)
        .put(KEY_SOURCE, sourceCatalogSceneId)
        .put(KEY_LOCAL, localJson)
        .toString()
}

fun decodeSavedPayloadToLocalState(
    payloadJson: String,
    storageSceneId: Int,
): SoundscapeSceneLocalState? {
    if (payloadJson.isBlank()) return null
    return runCatching {
        val root = JSONObject(payloadJson)
        if (root.optInt(KEY_SCHEMA, 0) != SOUNDSCAPE_SAVED_PAYLOAD_SCHEMA_V1) return@runCatching null
        val local = root.optJSONObject(KEY_LOCAL) ?: return@runCatching null
        if (local.length() == 0) return@runCatching null
        SoundscapeSceneLocalState(
            sceneId = storageSceneId,
            musicVolume = local.optDouble("musicVolume", 1.0).toFloat().coerceIn(0f, 1f),
            selectedMusicId = if (local.isNull("selectedMusicId")) null else local.optInt("selectedMusicId"),
            selectedMusicUrl = local.optString("selectedMusicUrl").takeIf { it.isNotBlank() && !local.isNull("selectedMusicUrl") },
            selectedMusicTitle = local.optString("selectedMusicTitle").takeIf { it.isNotBlank() && !local.isNull("selectedMusicTitle") },
            layersJson = local.optString("layersJson", ""),
            buttonsJson = local.optString("buttonsJson", ""),
            backgroundImageUrl = local.optString("backgroundImageUrl").takeIf { it.isNotBlank() && !local.isNull("backgroundImageUrl") },
            backgroundVideoUrl = local.optString("backgroundVideoUrl").takeIf { it.isNotBlank() && !local.isNull("backgroundVideoUrl") },
            backgroundSource = local.optString("backgroundSource").takeIf { it.isNotBlank() && !local.isNull("backgroundSource") },
            updatedAt = System.currentTimeMillis(),
        )
    }.getOrNull()
}

fun sourceCatalogSceneIdFromPayload(payloadJson: String): Int? =
    runCatching {
        val root = JSONObject(payloadJson)
        root.optInt(KEY_SOURCE).takeIf { it > 0 }
    }.getOrNull()

fun SavedSoundscape.toLocalStateForEditor(storageSceneId: Int): SoundscapeSceneLocalState? =
    decodeSavedPayloadToLocalState(payloadJson, storageSceneId)

fun SavedSoundscape.withUpdatedPayloadFromLocal(
    sourceCatalogSceneId: Int,
    displayName: String,
    local: SoundscapeSceneLocalState,
): SavedSoundscape = copy(
    sourceCatalogSceneId = sourceCatalogSceneId,
    name = displayName,
    payloadJson = encodeSavedPayloadJson(sourceCatalogSceneId, local),
    schemaVersion = SOUNDSCAPE_SAVED_PAYLOAD_SCHEMA_V1,
    updatedAt = System.currentTimeMillis(),
)
