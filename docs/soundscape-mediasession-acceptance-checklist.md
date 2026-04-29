## Soundscape MediaSession acceptance checklist

- [x] `SoundscapePlaybackService` switched to `DefaultMediaNotificationProvider` and updates notification from `MediaItem` metadata.
- [x] `SoundscapeSceneScreen` attaches `MediaController` (`SessionToken` -> `SoundscapePlaybackService`) and uses controller transport (`play/pause`).
- [x] Service transport wiring moved to `MediaSession` callback custom commands (`next/prev`) instead of service intent actions.
- [x] Layer runtime identity introduced via `SoundscapeLayerState.instanceKey` and used in `SoundscapeSoundsManager` player map.
- [x] Manifest keeps only `MediaSessionService` declaration for soundscape path; manager starts/stops service lifecycle only.
- [x] `SoundscapeSoundsManager` lifecycle moved to `SoundscapePlaybackService` (render/release no longer UI-owned).
- [x] Scene music playback moved from Compose background to service-owned player synchronized by `SoundscapePlaybackController`.
- [x] UI background no longer pauses core playback on `ON_STOP`; playback ownership is service-side.
- [x] `:app:compileDebugKotlin` passed after changes.
- [x] IDE lint diagnostics report no new issues in changed files.

## Manual smoke scenarios (to execute on device)

- [ ] Open a soundscape scene, send app to background, confirm playback continues and lockscreen play/pause works.
- [ ] Verify notification shows scene title and artwork (with fallback when artwork URL is empty).
- [ ] Verify notification play/pause controls affect layers and scene music in sync (service-owned transport path).
- [ ] Use next/prev flow (if playlist context available) and verify navigation opens adjacent scene.
- [ ] Open two scenes that contain duplicated sound IDs and verify layer volume slider changes only selected layer.
- [ ] Return app to foreground and verify Compose state remains synchronized with controller playback state.
