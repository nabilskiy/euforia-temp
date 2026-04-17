# Android Remote Config vs iOS: Gap Analysis + Integration Plan

Документ сравнивает текущую реализацию `Remote Config` в Android с iOS-подходом и задает план интеграции.  
Приоритет: **сначала модуль `soundscapes`**.

## 1) Что уже есть в Android

## 1.1 Инициализация и fetch

- Есть централизованный fetcher:
  - `app/src/main/java/digital/euforia/app/data/config/FirebaseRemoteConfigFetcher.kt`
  - `app/src/main/java/digital/euforia/app/data/config/EuforiaRemoteConfigFetcher.kt`
- На старте задаются дефолты:
  - `app/src/main/java/digital/euforia/app/App.kt` -> `setDefaultsAsync(R.xml.remote_config_defaults)`
- Cold start fetch выполняется в splash:
  - `app/src/main/java/digital/euforia/app/ui/splash/SplashViewModel.kt`
  - через `UpdateRemoteConfigUseCase`.

## 1.2 Typed-access поверх RC (частично)

В `EuforiaRemoteConfigFetcher` уже есть typed методы для:

- onboarding flags,
- today/time-of-day,
- search suggestions (общий),
- feedback/rate/critical update,
- sos videos,
- library/program templates и т.д.

## 1.3 Конфиг-ключи soundscapes уже присутствуют в defaults

В `app/src/main/res/xml/remote_config_defaults.xml` есть:

- `scene_player_config`
- `scenes_popular`
- `scenes_search_suggestions`
- `scenes_default`
- `sounds_suggestions`
- `music_suggestions`

## 2) Сравнение с iOS-докой: ключевые разрывы

## 2.1 Soundscapes RC-ключи не интегрированы в Android-модуль

В Android `soundscapes` сейчас:

- не читает `scenes_popular` / `scenes_search_suggestions`,
- не читает `sounds_suggestions` для sound picker,
- не читает `music_suggestions` для music picker,
- не читает `scenes_default` для default playlist logic,
- не читает `scene_player_config` для runtime-поведения плеера.

Кодовые точки модуля:

- `app/src/main/java/digital/euforia/app/ui/soundscapes/SoundscapesViewModel.kt`
- `app/src/main/java/digital/euforia/app/ui/soundscapes/SoundscapeSceneViewModel.kt`
- `app/src/main/java/digital/euforia/app/domain/usecase/soundscapes/GetSoundscapesCatalogFlowUseCase.kt`

## 2.2 iOS имеет server-driven логику suggestions/popular, Android — нет

На iOS:

- `scenes_search_suggestions` + `scenes_popular` используются в `ScenesSearchViewController`.
- `sounds_suggestions` используется с fallback-цепочкой:
  - `category.alias` -> `categoryId` -> `"*"`.
- `music_suggestions` добавляет suggestions-секцию в music picker.

В Android эти сценарии не реализованы в `soundscapes`.

## 2.3 iOS использует `scene_player_config` для runtime, Android пока нет

На iOS `scene_player_config` влияет на:

- `ambientMode` (audio session),
- parallax / background behavior.

В Android для scene background/media cache поведение уже реализовано технически, но **не параметризовано RC**.

## 2.4 Общий архитектурный gap

- В Android есть единый fetcher, но нет отдельного domain-слоя "SoundscapesRemoteConfig" с контрактом по ключам и fallback.
- Нет явной telemetry для RC fallback/hit-механик (особенно для soundscapes recommendation блоков).

## 3) План интеграции (общий)

## Phase A — Укрепить RC-платформу (без UI-изменений)

1. Добавить typed API в `EuforiaRemoteConfigFetcher` для soundscapes-ключей:
   - `getScenePlayerConfig()`
   - `getScenesDefaultIds()`
   - `getScenesPopularIds()`
   - `getScenesSearchSuggestions()`
   - `getSoundsSuggestionsMap()`
   - `getMusicSuggestionsIds()`
2. Для каждого метода определить строгий fallback:
   - decode error -> пустое/дефолтное безопасное значение.
3. Добавить unit-тесты decode/fallback (JSON happy-path + broken JSON).

## Phase B — Наблюдаемость

1. Логировать RC decode failures в единый канал (Timber + Crashlytics event).
2. Добавить debug dump актуальных soundscapes RC значений (по build flag).

## Phase C — Консистентность lifecycle

1. Проверить, что soundscapes-экраны работают корректно, если RC еще не fetched (используют defaults).
2. Проверить hot-start обновление (когда RC подтянулся после старта).

## 4) Приоритетный план: Soundscapes First

## Sprint S1 — RC для поиска сцен (popular + suggestions)

Цель: parity с iOS поиска сцен.

1. Добавить use case/mapper для секции поиска:
   - источник `scenes_search_suggestions`,
   - источник `scenes_popular`.
2. Реализовать fallback как в iOS:
   - если popular IDs пустые/невалидные -> random top N из доступных сцен.
3. Внедрить в UI поиска soundscapes (или текущий каталог, если отдельного поиска нет).

Acceptance:

- при пустом ключе popular отображается fallback,
- при валидном ключе порядок/состав соответствует RC id list.

## Sprint S2 — RC suggestions для Sounds picker

Цель: server-driven рекомендации звуков в редакторе сцены.

1. Добавить чтение `sounds_suggestions` как `Map<String, List<Int>>`.
2. Реализовать стратегию выбора IDs:
   - `category.alias`
   - `categoryId.toString()`
   - `"*"`
3. В `SoundscapeSceneViewModel` добавить suggestions-секцию отдельно от общего каталога звуков.

Acceptance:

- при наличии alias-ключа берется alias-ветка,
- при отсутствии alias корректно падает в `categoryId`/`*`,
- отсутствие ключа не ломает picker.

## Sprint S3 — RC suggestions для Music picker

Цель: parity с iOS music suggestions.

1. Добавить чтение `music_suggestions`.
2. Маппить IDs на `availableMusic`.
3. Добавить suggestions-секцию вверху списка музыки (без дублей).

Acceptance:

- секция скрыта, если suggestions пустые,
- секция не показывает невалидные id.

## Sprint S4 — `scenes_default` для дефолтного плейлиста

Цель: управлять default scene set через RC.

1. Определить точку использования default playlist в Android-флоу.
2. Подключить `scenes_default` в соответствующий use case/repository query.
3. Добавить fallback на текущую серверную/локальную логику.

Acceptance:

- при валидном RC default формируется по RC ids,
- при пустом RC используется fallback.

## Sprint S5 — `scene_player_config` в runtime-поведение

Цель: параметризовать ключевые флаги soundscape player через RC.

1. Ввести model `ScenePlayerConfig` (Android domain).
2. Подключить минимум:
   - `ambientMode` (если применимо к audio focus policy),
   - визуальные флаги (parallax/эффекты), если нужны в Compose.
3. Не трогать стабильный cache-путь, но добавить feature-flag gate при необходимости.

Acceptance:

- изменение RC отражается без релиза,
- отсутствие/битый ключ не ломает воспроизведение.

## 5) Риски и что проверить заранее

- Несоответствие alias между backend категориями и локальными category alias.
- Невалидные id в RC (sound/music/scene) — обязательна фильтрация по существующим сущностям.
- Состояние до завершения fetch (использование defaults) — UI не должен мигать/ломаться.
- Возможные дубли в suggestions-секциях и конфликт с текущими фильтрами.

## 6) Рекомендуемый порядок выполнения

1. **S1 (scenes popular/suggestions)**  
2. **S2 (sounds suggestions)**  
3. **S3 (music suggestions)**  
4. **S4 (scenes_default)**  
5. **S5 (scene_player_config)**  

Это дает быстрый user-visible эффект в `soundscapes`, затем закрывает parity по iOS.

