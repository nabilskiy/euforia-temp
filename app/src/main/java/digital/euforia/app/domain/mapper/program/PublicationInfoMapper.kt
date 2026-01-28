package digital.euforia.app.domain.mapper.program

import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.model.NetworkArticle
import digital.euforia.app.data.model.NetworkExercise
import digital.euforia.app.data.model.NetworkMeditation
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.programs.publication.PublicationType
import javax.inject.Inject

class PublicationInfoMapper @Inject constructor() {

    fun fromMeditation(meditation: Meditation, pkg: Package? = null): PublicationInfo =
        buildPublication(
            id = meditation.id,
            packageId = meditation.mainPackageId,
            categoryId = meditation.mainCategoryId,
            videoCoverUrl = pkg?.videoCoverUrl,
            isPremium = meditation.pro,
            type = PublicationType.MEDITATION,
            alias = meditation.alias,
            title = meditation.name,
            subtitle = meditation.subtitle,
            imageUrl = meditation.imageUrl,
            videoUrl = meditation.videoUrl ?: meditation.audioUrl,
            color1 = meditation.color1,
            color2 = meditation.color2,
            color3 = meditation.color3,
            publishedAt = meditation.publishedAt,
            durationMinutes = meditation.computeDurationMinutes(),
            isFavourite = meditation.isFavourite
        )

    fun fromExercise(exercise: Exercise): PublicationInfo =
        buildPublication(
            id = exercise.id,
            packageId = exercise.mainPackageId,
            categoryId = exercise.mainCategoryId,
            isPremium = exercise.pro,
            type = PublicationType.EXERCISE,
            alias = exercise.alias,
            title = exercise.name,
            subtitle = exercise.subtitle,
            imageUrl = exercise.imageUrl,
            videoUrl = exercise.videoUrl,
            color1 = exercise.color1,
            color2 = exercise.color2,
            color3 = exercise.color3,
            publishedAt = exercise.publishedAt,
            durationMinutes = exercise.computeDurationMinutes(),
            isFavourite = exercise.isFavourite
        )

    fun fromArticle(article: Article): PublicationInfo =
        buildPublication(
            id = article.id,
            packageId = article.mainPackageId,
            categoryId = article.mainCategoryId,
            isPremium = article.pro,
            type = PublicationType.ARTICLE,
            alias = article.alias,
            title = article.name,
            subtitle = article.subtitle,
            imageUrl = article.imageUrl,
            videoUrl = null,
            color1 = article.color1,
            color2 = article.color2,
            color3 = article.color3,
            publishedAt = article.publishedAt,
            durationMinutes = article.computeDurationMinutes(),
            isFavourite = article.isFavourite
        )

    fun fromNetworkMeditation(
        meditation: NetworkMeditation,
        videoCoverUrl: String? = null
    ): PublicationInfo =
        buildPublication(
            id = meditation.id,
            packageId = meditation.mainPackageId,
            videoCoverUrl = videoCoverUrl,
            categoryId = meditation.mainCategoryId,
            isPremium = meditation.pro,
            type = PublicationType.MEDITATION,
            alias = meditation.alias,
            title = meditation.name,
            subtitle = meditation.subtitle,
            imageUrl = meditation.imageUrl,
            videoUrl = meditation.videoUrl ?: meditation.audioUrl,
            color1 = meditation.color1,
            color2 = meditation.color2,
            color3 = meditation.color3,
            publishedAt = meditation.publishedAt,
            durationMinutes = secondsToMinutesAtLeast1(meditation.audio?.duration),
        )

    fun fromNetworkExercise(exercise: NetworkExercise): PublicationInfo =
        buildPublication(
            id = exercise.id,
            packageId = exercise.mainPackageId,
            categoryId = exercise.mainCategoryId,
            isPremium = exercise.pro,
            type = PublicationType.EXERCISE,
            alias = exercise.alias,
            title = exercise.name,
            subtitle = exercise.subtitle,
            imageUrl = exercise.imageUrl,
            videoUrl = exercise.videoUrl,
            color1 = exercise.color1,
            color2 = exercise.color2,
            color3 = exercise.color3,
            publishedAt = exercise.publishedAt,
            durationMinutes = secondsToMinutesAtLeast1(
                exercise.video?.duration ?: exercise.audio?.duration
            ),
        )

    fun fromNetworkArticle(article: NetworkArticle): PublicationInfo =
        buildPublication(
            id = article.id,
            packageId = article.mainPackageId,
            categoryId = article.mainCategoryId,
            isPremium = article.pro,
            type = PublicationType.ARTICLE,
            alias = article.alias,
            title = article.name,
            subtitle = article.subtitle,
            imageUrl = article.imageUrl,
            videoUrl = null,
            color1 = article.color1,
            color2 = article.color2,
            color3 = article.color3,
            publishedAt = article.publishedAt,
            durationMinutes = contentLengthToMinutes(article.contentLength),
        )

    private fun buildPublication(
        id: Int,
        packageId: Int?,
        categoryId: Int?,
        isPremium: Boolean,
        type: PublicationType,
        alias: String,
        title: String,
        subtitle: String?,
        imageUrl: String?,
        videoUrl: String?,
        color1: String?,
        color2: String?,
        color3: String?,
        publishedAt: Long?,
        durationMinutes: Int,
        videoCoverUrl: String? = null,
        isFavourite: Boolean = false,
    ): PublicationInfo =
        PublicationInfo(
            id = id,
            packageId = packageId,
            categoryId = categoryId,
            categoryVideoCoverUrl = videoCoverUrl,
            isPremium = isPremium,
            publicationType = type,
            alias = alias,
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            color1 = color1,
            color2 = color2,
            color3 = color3,
            publishedAt = publishedAt,
            durationMinutes = durationMinutes,
            isFavourite = isFavourite
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