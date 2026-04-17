# iOS Firebase Remote Config: карта использования и интеграция

Документ описывает, как `Firebase Remote Config` используется в `ios/euforia`, и что важно учесть для дальнейшей синхронной интеграции на Android.

## 1) Базовый жизненный цикл Remote Config в iOS

- Конфиг и дефолты настраиваются на старте приложения в `AppDelegate` через `RemoteConfigHelper.shared.configure()`.
- В `configure()` выставляются:
  - `minimumFetchInterval = 0`,
  - `fetchTimeout = 20`,
  - загрузка дефолтов из plist: `setDefaults(fromPlist: "RemoteConfigDefault")`.
- Основная загрузка делается через `RemoteConfigHelper.fetchAndActivate(...)`.
  - `expirationDuration` берется из RC-ключа `remote_config_expiration_duration`,
  - в DEV принудительно `60s`,
  - при `forceRefresh = true` используется `0`.
- После успешного fetch:
  - вызывается `activate`,
  - обновляется `TimeOfDayConfig.default`,
  - выставляется флаг ready (`isReady.send(true)`),
  - запускается прелоад ресурсов из RC (`preloadResources()`),
  - обновляются remote-локализации (`LocalizationService.updateRemoteLocalizable()`).

Практически это означает: на iOS RC включен в bootstrap приложения и влияет на поведение сразу после app loading.

## 2) Технический паттерн доступа к RC-значениям

В проекте используется три уровня доступа:

- **Прямой доступ к ключам**: `RemoteConfig.remoteConfig()["key"]...`
  - для bool/string/number/data/json.
- **Типизированные computed properties в extension RemoteConfig**
  - пример: `scenePlayerConfig`, `timeOfDayConfig`, `todayVibesConfig`, `rateConfig`, `criticalUpdateConfig` и т.д.
- **Безопасный fallback**
  - если decode не удался, используется дефолтная модель/значение.
  - ошибки decode обычно логируются в Crashlytics.

Дополнительно есть helper-расширения:

- `RemoteConfigValue.fixedStringValue` — исправляет `\n` и `%s` для локализуемых строк.
- `decoded(asType:)` (используется по всему проекту) — универсальный JSON decode для typed-моделей/массивов.

## 3) Где RC используется по проекту (высокоуровнево)

Ниже категории, где RC влияет на поведение:

- **App bootstrap / runtime**
  - частота fetch и общая активация RC,
  - preload удаленных ресурсов (`preload_keys`).
- **Networking / API headers**
  - App Check: `app_check_enabled`, `app_check_required`,
  - sandbox header переключатель: `sandbox`.
- **Library / Search / Dynamic templates**
  - динамический title библиотеки: `library_title`,
  - поисковые подсказки: `search_suggestions`,
  - шаблоны композитных экранов через RC-ключи (`CompositeListTemplateProvider`).
- **Onboarding / premium flows / experiments**
  - ключи `intro_*`, `premium_*`, feature toggles.
- **Time-of-day / Today Vibes / banners / alerts**
  - `time_of_day_config`,
  - `today_vibes_config`,
  - `rate_*`, `feedback_*`, `email_alert_*`, `critical_update_config`,
  - баннеры/сезонные флаги.
- **Локализация и контентные строки**
  - remote localizable блоки и строковые ключи с fallback.

## 4) Soundscapes: детальная карта RC-ключей и поведения

### 4.1 Вход в сцену и поведение плеера

- `scene_player_config`:
  - декодится в `ScenePlayerConfig` (`isParallaxEnabled`, `ambientMode`),
  - используется в `AudioScenePlayer.activateAudioSession()`:
    - при `ambientMode = true` включает `.mixWithOthers`,
    - иначе стандартный playback режим.
  - также влияет на UI-эффекты в `SoundStudioBackgroundView` (parallax).

### 4.2 Дефолтные сцены/плейлисты

- `scenes_default`:
  - читается в `AudioScenesStore.reload()`,
  - сохраняется в `defaultSceneIds`,
  - используется для плейлиста типа `.default` через API-запрос сцен по этим id.

Назначение: управлять "дефолтным" набором сцен серверно без релиза клиента.

### 4.3 Search в SoundStudio: suggestions + popular

`ScenesSearchViewController.updateItems()`:

- `scenes_search_suggestions` (`[String]`)
  - показывает блок `SearchSuggestionsSection`.
- `scenes_popular` (`[Int]`)
  - маппится в сцены по id и показывает блок "popular scenes".
- fallback:
  - если `scenes_popular` пуст или невалиден -> берется случайный `prefix(5)` из всех сцен.

### 4.4 Suggestions для Sounds picker

`SoundsListViewController.updateDataSource()`:

- `sounds_suggestions` (`[String: [Int]]`), где ключом может быть:
  - alias категории сцены,
  - строковый categoryId,
  - `"*"` как общий fallback.
- Логика выбора:
  1. пытается взять suggestions по `category.alias`,
  2. если нет — по `"\(categoryId)"`,
  3. если нет — по `"*"`.
- Затем ids маппятся на `SoundsStore.allSounds` и добавляются в секцию "Suggestions".

Это уже server-driven персонализация рекомендаций звуков в редакторе сцены.

### 4.5 Suggestions для Music picker

`SoundStudioMusicPickerViewModel.updateMusicsByCategory()`:

- `music_suggestions` (`[Int]`),
- ids маппятся на `MusicsStore.allMusics`,
- формируется секция `suggestions` вверху списка музыки.

### 4.6 Смежные ключи для soundscapes-опыта

- `scenes_default` — дефолтный набор сцен,
- `scenes_popular` — популярные сцены в поиске,
- `scenes_search_suggestions` — поисковые запросы в scene-search,
- `sounds_suggestions` — рекомендации звуков по категории/alias,
- `music_suggestions` — рекомендации музыки,
- `scene_player_config` — runtime-поведение плеера/фонового режима.

## 5) Важные fallback-принципы из iOS, которые стоит сохранить в Android

- Любой RC-decode должен быть non-fatal:
  - при ошибке брать безопасный дефолт и продолжать сценарий.
- Для recommendation-блоков должны быть "мягкие" fallback:
  - пустые suggestions — не показывать секцию,
  - пустой popular — подставить локально рассчитанный список.
- RC должен влиять на UI/поведение без app restart (после `fetchAndActivate`).
- Желательно централизовать доступ через typed фасад (`RemoteConfigRepository`/`RemoteConfigManager`), а не читать ключи хаотично в UI.

## 6) Рекомендации по интеграции в Android (практический план)

### Шаг 1. Единый typed-слой RC

Сделать централизованный слой, аналогичный iOS extension-подходу:

- `getScenePlayerConfig()`
- `getScenesDefaultIds()`
- `getScenesPopularIds()`
- `getScenesSearchSuggestions()`
- `getSoundsSuggestionsMap()`
- `getMusicSuggestionsIds()`

С decode + fallback внутри этого слоя.

### Шаг 2. Привязка к soundscapes-модулям

- **Scene player**: `scene_player_config.ambientMode` и визуальные флаги.
- **Scene search**: `scenes_search_suggestions` + `scenes_popular` + fallback random top N.
- **Sounds picker**: поддержать map-логику `alias -> id -> "*"` как в iOS.
- **Music picker**: секция suggestions из `music_suggestions`.
- **Default playlist**: использовать `scenes_default` как источник id.

### Шаг 3. Наблюдаемость и отладка

- Логировать RC decode failures (аналог Crashlytics логики iOS).
- Добавить debug-экран/лог dump активных RC-значений для QA.
- Добавить метрики отображения RC-секций (напр. suggestions shown / empty fallback used).

### Шаг 4. Контракт с backend/контентом

Зафиксировать формат ключей:

- `sounds_suggestions`: поддерживаем `alias`, `categoryId` и `"*"`,
- `music_suggestions`: список валидных id,
- `scenes_popular`: список id существующих сцен.

И отдельно договориться о правилах очистки/валидности id, чтобы не ломать UX.

## 7) Быстрый список ключей для soundscapes (для синхронизации iOS/Android)

- `scene_player_config`
- `scenes_default`
- `scenes_popular`
- `scenes_search_suggestions`
- `sounds_suggestions`
- `music_suggestions`

Если цель — parity с iOS для soundscapes, это минимальный обязательный набор RC-ключей.

