# iOS Continue Watching: implementation map

This note documents how `Continue Watching` is implemented in `ios/euforia`.

## 1) UI entry points

- Main screen block: `ios/euforia/Euforia/ViewControllers/ContinueWatchViewController.swift`
  - Builds sections:
    - `.meditationContinueWatch`
    - `.exerciseContinueWatch`
  - Screen title: `continue_watch_title`
  - Uses composite template key: `"continue_watch"`
- Section implementations:
  - `ios/euforia/Euforia/ViewControllers/CompositeList/Sections/ContinueWatchCollectionSection.swift`
  - `ios/euforia/Euforia/ViewControllers/CompositeList/Sections/ContinueWatchPublicationsCollectionSection.swift`
- Cell:
  - `ios/euforia/Euforia/Views/Cells/ContinueWatchCollectionViewCell.swift`
  - For publications, reads state from `PublicationsService.shared.getPublicationState(for:)`
  - Displays `playingProgress` via `UIProgressView`

## 2) End-to-end data flow (Continue Watching)

### Composite-list flow

1. `ContinueWatchViewController` requests composite sections.
2. `CompositeListFetcher.requestParametersItem(from:)` handles:
   - `.meditationContinueWatch`
   - `.exerciseContinueWatch`
3. For each section, fetcher calls:
   - `PublicationsService.shared.getContinueWatchingPublicationsStates(for:limit:)`
4. Service proxies to CoreData layer:
   - `DataStore.getStatesForContinueWatchingPublications(for:minTimeInterval:limit:)`
5. CoreData predicate for continue state:
   - `isPlayingCompleted == FALSE && playingTime > 0`
   - sorted by `lastViewingTimestamp DESC`
6. Fetcher extracts `entityId` list and sends API query with:
   - `filters["ids"] = [...]`
   - `filters["sort-attribute"] = "ids"`
7. Content payload is fetched via composite endpoint:
   - `API.getCompositeList(...)`

### Dedicated list flow (full screen)

- `ContinueWatchPublicationsListViewController.getIDs()`:
  - gets IDs from `getContinueWatchingPublicationsStates(...)`
- `HistoryPublicationsListViewController.getIDs()`:
  - gets IDs from `getLastViewingPublicationsStates(...)`
- `PublicationsIDsListViewController.setupPaginatedDataSource()`:
  - calls `API.getPublications(type:options:pagination:)`
  - forces order by IDs (`sortAttribute = "ids"`)

## 3) Playback progress write + resume

### Where progress is written

- `PublicationPlayerViewController` writes playback state in:
  - `viewWillDisappear` -> `savePlayingTime()`
  - app terminate notification -> `savePlayingTime()`
  - playback completion -> `savePlayingTime(isFinished: true)`
- `savePlayingTime(...)` computes:
  - `time` (or full duration if completed)
  - `duration`
  - `isCompleted`
- Then calls:
  - `PublicationsService.updatePlayingTime(for:time:duration:isCompleted:)`
- Service persists to CoreData:
  - `playingTime`
  - `playingDuration`
  - `isPlayingCompleted = oldValue || isCompleted`

### Where "last viewed" is written

- `PublicationViewController.updateViews()` calls once per screen appearance logic:
  - `PublicationsService.shared.updateLastViewingTimestamp(for: publication)`
- Service stores:
  - `lastViewingTimestamp = Date()`
  - `viewCount += 1`

### How resume works

- `ContinueWatch` open path passes `continueWatch: true` to publication screen/player.
- In `PublicationPlayerViewController.setupPlayerItem(continueWatch:)`:
  - reads state from `PublicationsService.getPublicationState(for:)`
  - seeks to `state.playingTime` using `player.seek(...)`

## 4) Continue Watching vs History

- Continue Watching:
  - only incomplete playback with non-zero progress
  - predicate: `isPlayingCompleted == FALSE && playingTime > 0`
- History:
  - anything viewed with timestamp
  - predicate: `lastViewingTimestamp != NULL`

## 5) Related soundscapes branch

Soundscapes use a parallel store, not `PublicationsService`:

- Last viewed:
  - `AudioScenesStore.updateLastViewingTimestamp(for:)`
  - `DataStore.getStatesForLastViewingAudioScenes(...)`
- Listening duration:
  - `AudioScenePlayer.savePlayingDuration()`
  - `AudioScenesStore.appendPlayingDuration(_:for:)`
- Playlist type mapping:
  - `AudioScenePlaylistType.continueWatch`
  - resolves to `AudioScenesStore.getLastViewingAudioScenes(limit:)`

## 6) Localization keys relevant to requested screen

File: `ios/euforia/Euforia/Resources/en.lproj/Localizable.strings`

- `continue_watch_title` = `Continue Watching`
- `profile_title` = `My Euforia`
- `playlists_title` = `Playlists`
- `favorites_title` = `Favorites`

Note: exact string `Recently viewed` is not present; iOS naming is primarily `Continue Watching` and `History`.

## 7) Mermaid overview

```mermaid
flowchart TD
  continueVC[ContinueWatchViewController] --> compositeFetcher[CompositeListFetcher]
  compositeFetcher --> publicationsService[PublicationsService]
  publicationsService --> dataStore[DataStore]
  dataStore --> publicationState[PublicationStateCoreData]
  compositeFetcher --> compositeApi[APIGetCompositeList]
  continueCell[ContinueWatchCollectionViewCell] --> publicationsService
  playerVC[PublicationPlayerViewController] --> savePlayingTime[savePlayingTime]
  savePlayingTime --> publicationsService
  playerVC --> setupPlayerItem[setupPlayerItemContinueWatch]
  setupPlayerItem --> publicationsService
```

