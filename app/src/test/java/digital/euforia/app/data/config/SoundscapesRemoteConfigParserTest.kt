/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */
package digital.euforia.app.data.config

import com.squareup.moshi.Moshi
import digital.euforia.app.domain.model.config.ScenePlayerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundscapesRemoteConfigParserTest {
    private val moshi = Moshi.Builder().build()

    @Test
    fun `parse scenes_default returns ids for valid json`() {
        val ids = SoundscapesRemoteConfigParser.parseScenesDefaultIds(
            json = "[291,292,293]",
            moshi = moshi,
            onError = {}
        )

        assertEquals(listOf(291, 292, 293), ids)
    }

    @Test
    fun `parse scenes_default returns empty list for broken json`() {
        var called = false
        val ids = SoundscapesRemoteConfigParser.parseScenesDefaultIds(
            json = "[291,]",
            moshi = moshi,
            onError = { called = true }
        )

        assertTrue(called)
        assertTrue(ids.isEmpty())
    }

    @Test
    fun `parse scene_player_config returns model for valid json`() {
        val config = SoundscapesRemoteConfigParser.parseScenePlayerConfig(
            json = "{\"isParallaxEnabled\":false,\"ambientMode\":true}",
            moshi = moshi,
            onError = {}
        )

        assertEquals(ScenePlayerConfig(isParallaxEnabled = false, ambientMode = true), config)
    }

    @Test
    fun `parse scene_player_config returns fallback for broken json`() {
        var called = false
        val config = SoundscapesRemoteConfigParser.parseScenePlayerConfig(
            json = "{\"isParallaxEnabled\":",
            moshi = moshi,
            onError = { called = true }
        )

        assertTrue(called)
        assertEquals(ScenePlayerConfig(), config)
    }
}

