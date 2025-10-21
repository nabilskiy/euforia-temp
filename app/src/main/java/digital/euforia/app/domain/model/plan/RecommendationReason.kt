package digital.euforia.app.domain.model.plan

enum class RecommendationReason {
    CURRENT_SESSION,        // Поточна активна сесія
    NEXT_AVAILABLE,         // Наступна доступна
    MOST_PLAYED,            // Найбільш прослуховувана
    FAVORITE,               // Улюблена
    RECOMMENDED,            // Рекомендація системи
    TIME_OF_DAY_MATCH,      // Відповідає поточному часу доби
    DEMO_NEXT              // Наступний день демо-циклу
}