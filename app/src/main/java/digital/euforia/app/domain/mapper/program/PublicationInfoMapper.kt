package digital.euforia.app.domain.mapper.program

import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.model.NetworkArticle
import digital.euforia.app.data.model.NetworkExercise
import digital.euforia.app.data.model.NetworkMeditation
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.programs.publication.PublicationType
import javax.inject.Inject

class PublicationInfoMapper @Inject constructor() {

    fun fromMeditation(meditation: Meditation): PublicationInfo =
        buildPublication(
            id = meditation.id,
            categoryId = meditation.mainCategoryId,
            isPremium = meditation.pro,
            type = PublicationType.MEDITATION,
            title = meditation.name,
            subtitle = meditation.subtitle,
            imageUrl = meditation.imageUrl,
            color1 = meditation.color1,
            color2 = meditation.color2,
            color3 = meditation.color3,
            publishedAt = meditation.publishedAt,
            durationMinutes = meditation.computeDurationMinutes(),
        )

    fun fromExercise(exercise: Exercise): PublicationInfo =
        buildPublication(
            id = exercise.id,
            categoryId = exercise.mainCategoryId,
            isPremium = exercise.pro,
            type = PublicationType.EXERCISE,
            title = exercise.name,
            subtitle = exercise.subtitle,
            imageUrl = exercise.imageUrl,
            color1 = exercise.color1,
            color2 = exercise.color2,
            color3 = exercise.color3,
            publishedAt = exercise.publishedAt,
            durationMinutes = exercise.computeDurationMinutes(),
        )

    fun fromArticle(article: Article): PublicationInfo =
        buildPublication(
            id = article.id,
            categoryId = article.mainCategoryId,
            isPremium = article.pro,
            type = PublicationType.ARTICLE,
            title = article.name,
            subtitle = article.subtitle,
            imageUrl = article.imageUrl,
            color1 = article.color1,
            color2 = article.color2,
            color3 = article.color3,
            publishedAt = article.publishedAt,
            durationMinutes = article.computeDurationMinutes(),
        )

    fun fromNetworkMeditation(meditation: NetworkMeditation): PublicationInfo =
        buildPublication(
            id = meditation.id,
            categoryId = meditation.mainCategoryId,
            isPremium = meditation.pro,
            type = PublicationType.MEDITATION,
            title = meditation.name,
            subtitle = meditation.subtitle,
            imageUrl = meditation.imageUrl,
            color1 = meditation.color1,
            color2 = meditation.color2,
            color3 = meditation.color3,
            publishedAt = meditation.publishedAt,
            durationMinutes = secondsToMinutesAtLeast1(meditation.audio?.duration),
        )

    fun fromNetworkExercise(exercise: NetworkExercise): PublicationInfo =
        buildPublication(
            id = exercise.id,
            categoryId = exercise.mainCategoryId,
            isPremium = exercise.pro,
            type = PublicationType.EXERCISE,
            title = exercise.name,
            subtitle = exercise.subtitle,
            imageUrl = exercise.imageUrl,
            color1 = exercise.color1,
            color2 = exercise.color2,
            color3 = exercise.color3,
            publishedAt = exercise.publishedAt,
            durationMinutes = secondsToMinutesAtLeast1(exercise.video?.duration ?: exercise.audio?.duration),
        )

    fun fromNetworkArticle(article: NetworkArticle): PublicationInfo =
        buildPublication(
            id = article.id,
            categoryId = article.mainCategoryId,
            isPremium = article.pro,
            type = PublicationType.ARTICLE,
            title = article.name,
            subtitle = article.subtitle,
            imageUrl = article.imageUrl,
            color1 = article.color1,
            color2 = article.color2,
            color3 = article.color3,
            publishedAt = article.publishedAt,
            durationMinutes = contentLengthToMinutes(article.contentLength),
        )

    private fun buildPublication(
        id: Int,
        categoryId: Int?,
        isPremium: Boolean,
        type: PublicationType,
        title: String,
        subtitle: String?,
        imageUrl: String?,
        color1: String?,
        color2: String?,
        color3: String?,
        publishedAt: Long?,
        durationMinutes: Int,
    ): PublicationInfo =
        PublicationInfo(
            id = id,
            categoryId = categoryId,
            isPremium = isPremium,
            publicationType = type,
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl,
            color1 = color1,
            color2 = color2,
            color3 = color3,
            publishedAt = publishedAt,
            durationMinutes = durationMinutes,
        )

    private fun secondsToMinutesAtLeast1(seconds: Int?): Int =
        (seconds ?: 0).div(60).coerceAtLeast(1)

    private fun contentLengthToMinutes(contentLength: Int?): Int =
        contentLength
            ?.div(16.2)
            ?.toInt()
            ?.div(60)
            ?.coerceAtLeast(1)
            ?: 0
}