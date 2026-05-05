# Continue Watching на Android (My Euforia / Settings): полная инструкция по реализации

Документ описывает **конкретные шаги и файлы** для блока «Continue Watching» над списком настроек (экран профиля / My Euforia). Референс поведения iOS: [ios-continue-watching-map.md](ios-continue-watching-map.md) (в т.ч. §8 `smartContinueWatch`).

---

## 0. Цель и объём работ

**Цель UX:** на экране настроек (`SettingsScreen`) показать горизонтальную карточку «продолжить», в первую очередь для **soundscape-сцены**, которую пользователь недавно открывал; по тапу — открыть тот же экран сцены, что из раздела Soundscapes.

**Два варианта объёма (выберите до начала кода):**

| Вариант | Описание |
|--------|----------|
| **MVP** | Только сцены: храним последний `originalSceneId` + время + опционально `playlistId`; UI с символом ∞ (как iOS для `AudioScene`). |
| **Паритет iOS `smartContinueWatch`** | Плюс медитации/упражнения с предикатом continue-watch, окно по Remote Config `continue_watch_time_interval`, один «победитель» по `lastViewingTimestamp`. Требует готовых данных о прогрессе публикаций на Android. |

Ниже пошагово расписан **MVP**. В конце — кратко **расширение до smart**.

---

## 1. Предпосылки (уже есть в проекте)

- Строки: `continue_watch_title`, `continue_watch_empty_*` в `app/src/main/res/values/strings.xml`.
- Навигация к сцене: `HomeDestination.SoundscapesScene(sceneId: Int, playlistId: Int? = null)` в [NavDestination.kt](../app/src/main/java/digital/euforia/app/ui/navigation/NavDestination.kt); composable в [AppNavigation.kt](../app/src/main/java/digital/euforia/app/ui/navigation/AppNavigation.kt) (`composable<HomeDestination.SoundscapesScene>`).
- Пример навигации с плейлистом: [SoundscapePlaylistScreen.kt](../app/src/main/java/digital/euforia/app/ui/soundscapes/SoundscapePlaylistScreen.kt) — `navigate(HomeDestination.SoundscapesScene(sceneId, playlistId))`.
- Каталог сцен: [SoundscapesRepository.kt](../app/src/main/java/digital/euforia/app/data/repository/SoundscapesRepository.kt) — `getSceneById(id: Int)`.
- **Важно:** `SoundscapeSceneViewModel.persistLocalSceneState()` без аргументов **не сохраняет** состояние в Room, если `presetId == null`, поэтому **нельзя** опираться только на `SoundscapeSceneLocalState` для «последней каталожной сцены».

---

## 2. Фаза A — Preferences (источник истины для MVP)

**Файл:** [AppPreferences.kt](../app/src/main/java/digital/euforia/app/data/store/AppPreferences.kt)

1. Объявить ключи DataStore (рядом с существующими `keySoundscapes*`):
   - `last_viewed_soundscape_scene_id` → `Int`, значение `0` или отсутствие = «нет записи» (либо отдельный boolean `has_last_viewed_scene` — на выбор, но достаточно `id > 0`).
   - `last_viewed_soundscape_at_epoch_ms` → `Long`.
   - `last_viewed_soundscape_playlist_id` → `Int?` (хранить `-1` или не писать ключ для «нет плейлиста» — как принято в этом классе для nullable).

2. Реализовать:
   - `suspend fun setLastViewedSoundscapeScene(sceneId: Int, viewedAtEpochMs: Long, playlistId: Int?)`
   - `fun lastViewedSoundscapeSceneFlow(): Flow<Triple<Int, Long, Int?>?> ` или отдельный маленький data class `LastViewedSoundscape(val sceneId: Int, val viewedAtMs: Long, val playlistId: Int?)`.

3. При необходимости очистки (logout и т.д.) — добавить сброс в существующий поток очистки preferences (если есть аналог `clearUserData`).

**Критерий:** из любого места приложения можно записать и подписаться на Flow без обращения к Room.

---

## 3. Фаза B — Запись при открытии сцены

**Файл:** [SoundscapeSceneViewModel.kt](../app/src/main/java/digital/euforia/app/ui/soundscapes/SoundscapeSceneViewModel.kt), метод `ensureSceneLoaded()` (цепочка `intent { ... }` после успешного получения `scene`).

**Когда писать в preferences:**

- После того как определён валидный контент для пользователя: `scene != null`, **нет** раннего выхода на paywall (`SoundscapeSceneSideEffect.NavigateToPaywall`).
- Использовать **`originalSceneId`** из состояния (уже есть в `reduce` / `loaded` — это id базовой сцены в каталоге, а не copy-id пресета для навигации каталога).
- `playlistId` брать из того же state / аргументов загрузки (`playlistId` из `SavedStateHandle`), как в финальном `state.copy(...)`.

**Когда не писать (или не перезаписывать осмысленно):**

- Ошибка загрузки (`error` в state, `scene == null`).
- Пользователь не премиум и сцена pro → уход на paywall до показа сцены.

**Реализация:** один вызов `appPreferences.setLastViewedSoundscapeScene(...)` с `System.currentTimeMillis()` (или `Clock.System.now()` если проект уже на kotlin.time) в конце успешной ветки `ensureSceneLoaded`, **на Main-смысле** внутри уже существующего `intent { }` / корутины ViewModel.

**Критерий:** открыли каталожную сцену без пресета → в DataStore появились id и timestamp.

---

## 4. Фаза C — Модель карточки для UI

**Новый небольшой тип (рядом с Settings или в `domain/model/settings/`):**

```kotlin
data class ContinueWatchingCardUi(
    val sceneId: Int,
    val title: String,
    val imageUrl: String?,
    val playlistId: Int?,
)
```

**Разрешение title/imageUrl:**

- Вызов `soundscapesRepository.getSceneById(sceneId)` (suspend) → имя сцены, `imagePreviewUrl` / `imageUrl` (как в списках soundscapes).
- Если `null` (каталог ещё не подтянут): `title = "…"` или строка из ресурсов; `imageUrl = null` — Coil/AsyncImage покажет placeholder.

**Опционально:** отдельный use case `ResolveContinueWatchingCardUseCase` с инжектом `SoundscapesRepository` + чтение preferences, чтобы не раздувать `SettingsViewModel`.

---

## 5. Фаза D — SettingsViewModel

**Файл:** [SettingsViewModel.kt](../app/src/main/java/digital/euforia/app/ui/settings/SettingsViewModel.kt)

1. Добавить в конструктор зависимости: `AppPreferences`, (опционально) `SoundscapesRepository` или use case из фазы C.

2. В `SettingsState` добавить поле, например `continueWatching: ContinueWatchingCardUi? = null`.

3. В `onCreate` / `init` блока контейнера:
   - Запустить `viewModelScope.launch` + `combine`/`flatMapLatest`: `lastViewedSoundscapeSceneFlow()` и при необходимости flow каталога, либо просто при каждом изменении id вызывать suspend `getSceneById` в `launch`.
   - `reduce { state.copy(continueWatching = resolvedOrNull) }`.

4. Если `sceneId <= 0` или нет записи — отдавать `null` (секция скрыта) либо пустое состояние для empty UI.

**Критерий:** при возврате на Settings после просмотра сцены карточка обновляется без перезапуска процесса (если Flow настроен).

---

## 6. Фаза E — SettingsScreen (Compose)

**Файл:** [SettingsScreen.kt](../app/src/main/java/digital/euforia/app/ui/settings/SettingsScreen.kt), функция `SettingsContent`, `LazyColumn`.

**Место вставки:** сразу **после** `notificationItem(...)`, **перед** циклом `for ((index, settingGroup) in settingGroups.withIndex())`.

**Содержимое нового `item`:**

1. Заголовок секции: `localizedRes.string(R.string.continue_watch_title)` — стиль согласовать с существующими группами (`settingGroupItem` использует `titleSmall` uppercase + `titleLarge` для подзаголовка; можно повторить или упростить до одного заголовка как на iOS).

2. `LazyRow` с `contentPadding` только по горизонтали при необходимости; одна карточка фиксированной ширины (~70% ширины экрана как ориентир с iOS `itemFractionalWidth: 0.7`).

3. **Карточка:**
   - `Box` / `Card` с `RoundedCornerShape` как в приложении.
   - Превью 16:9 или 1.5:1 — согласовать с дизайн-системой soundscapes.
   - По центру превью — иконка play (как на макете).
   - Под превью — **полоска с символом ∞** (для MVP только сцены): отдельный маленький `Row` с двумя `Box` weight и `Text("∞")` или vector из SF-аналога; **не** использовать линейный прогресс-бар с фейковым процентом.
   - Под полоской / в нижней зоне — название (`continueWatching.title`).

4. `clickable` на карточке: вызвать колбэк `onContinueWatchingClick(sceneId, playlistId)`, проброшенный из `SettingsScreen` → `SettingsContent`.

5. **Если `continueWatching == null`:** либо не рендерить весь `item`, либо показать empty из `continue_watch_empty_title` / `continue_watch_empty_subtitle` — зафиксировать один вариант в продукте.

**Критерий:** визуально узнаваемый блок над первой группой настроек; не ломает скролл и `BlurredAppBar`.

---

## 7. Фаза F — Навигация по тапу

**В `SettingsScreen` / `SettingsContent`:**

```kotlin
navController.navigate(
    HomeDestination.SoundscapesScene(
        sceneId = card.sceneId,
        playlistId = card.playlistId,
    )
)
```

Импорт: `digital.euforia.app.ui.navigation.HomeDestination`.

**Проверка:** с экрана Settings открывается тот же `SoundscapeSceneScreen`, что и из `SoundscapesScreen` / плейлиста; `playlistId` восстанавливает контекст плейлиста, если сохраняли.

---

## 8. Фаза G — Ручная проверка (чеклист)

1. Чистая установка / сброс ключей → блок скрыт или empty, без crash.
2. Открыть сцену из каталога (без «My scene» пресета) → вернуться на Settings → id и обложка совпадают.
3. Открыть сцену из плейлиста с сохранённым `playlistId` → тап с Settings ведёт в сцену с тем же playlist context.
4. Премиум-locked сцена для не-премиум: если запись не должна появляться — убедиться, что запись в preferences **не** выполняется при уходе на paywall (см. фазу B).
5. Смена языка / локализации — заголовок секции берётся из строковых ресурсов.

---

## 9. Расширение до паритета iOS `smartContinueWatch` (после MVP)

1. **Remote Config:** добавить в [EuforiaRemoteConfigFetcher.kt](../app/src/main/java/digital/euforia/app/data/config/EuforiaRemoteConfigFetcher.kt) чтение ключа `continue_watch_time_interval` (дни; дефолт уже может быть в `remote_config_defaults.xml`).

2. **Кандидаты:**
   - Сцена: как сейчас, но отфильтровать запись, если `now - viewedAt > interval` (как iOS `minTimeInterval`).
   - Медитация / упражнение: нужны локальные аналоги `PublicationState` (просмотр + `playingTime`, `isPlayingCompleted`) — если на Android уже пишутся в БД/Preferences, собрать два optional state + timestamps.

3. **Выбор одной карточки:** отсортировать кандидатов по `lastViewingTimestamp` DESC, взять первого; переключить UI: для публикации — линейный прогресс (нужны доли из плеера); для сцены — ∞.

4. **Навигация:** для публикации — существующий `HomeDestination.PublicationPlayer` / `PublicationDetails` по правилам приложения (уточнить по коду плана).

---

## 10. Файлы, которые точно затрагиваются (MVP)

| Файл | Изменение |
|------|-----------|
| [AppPreferences.kt](../app/src/main/java/digital/euforia/app/data/store/AppPreferences.kt) | Ключи + get/set + Flow |
| [SoundscapeSceneViewModel.kt](../app/src/main/java/digital/euforia/app/ui/soundscapes/SoundscapeSceneViewModel.kt) | Запись last viewed после успешной загрузки |
| [SettingsViewModel.kt](../app/src/main/java/digital/euforia/app/ui/settings/SettingsViewModel.kt) | State + подписка + resolve карточки |
| [SettingsScreen.kt](../app/src/main/java/digital/euforia/app/ui/settings/SettingsScreen.kt) | Новый блок UI + колбэк навигации |
| Опционально новый `.kt` | `ContinueWatchingCardUi` + composable карточки |

**Hilt:** если добавляете use case — зарегистрировать в модуле DI (по аналогии с другими `UseCase` в проекте).

---

## 11. Порядок внедрения (рекомендуемый)

1. AppPreferences (фаза A) — можно покрыть unit-тестом маппинга keys, если есть тесты на DataStore.
2. Запись из `SoundscapeSceneViewModel` (фаза B).
3. `SettingsViewModel` + state (фазы C–D).
4. UI + navigate (фазы E–F).
5. Ручной чеклист (фаза G).
6. При необходимости — фаза 9 (smart).

После этого функция MVP считается **готовой к ревью**.
