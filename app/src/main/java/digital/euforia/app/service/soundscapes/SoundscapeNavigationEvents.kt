/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapeNavigationEvents @Inject constructor() {
    private val _openScene = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val openScene: SharedFlow<Int> = _openScene

    fun openScene(sceneId: Int) {
        _openScene.tryEmit(sceneId)
    }
}

