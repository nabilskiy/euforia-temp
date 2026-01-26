package digital.euforia.app.ui.programs

import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.db.entity.Package


fun Package.toProgramUi(): ProgramUi {
    return ProgramUi(
        id = id,
        isPremium = pro,
        authorId = authorId,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        resourceCount = publicationsCount,
    )
}

fun Article.toArticleUi(): ArticleUi {
    return ArticleUi(
        id = id,
        isPremium = pro,
        alias = alias,
        authorId = authorId,
        mainCategoryId = mainCategoryId,
        mainPackageId = mainPackageId,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        musicFileUrl = musicFileUrl,
        publishedAt = publishedAt,
        duration = (contentLength?.div(16.2))?.toInt()?.div(60)?.coerceAtLeast(1)
    )
}

fun Exercise.toExerciseUi(): ExerciseUi {
    return ExerciseUi(
        id = id,
        isPremium = pro,
        alias = alias,
        authorId = authorId,
        mainCategoryId = mainCategoryId,
        mainPackageId = mainPackageId,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        musicFileUrl = musicFileUrl,
        duration = (video?.duration ?: audio?.duration ?: 0).div(60).coerceAtLeast(1),
        publishedAt = publishedAt,
    )
}

fun Meditation.toMeditationUi(): MeditationUi {
    return MeditationUi(
        id = id,
        isPremium = pro,
        alias = alias,
        authorId = authorId,
        mainCategoryId = mainCategoryId,
        mainPackageId = mainPackageId,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        musicFileUrl = musicFileUrl,
        publishedAt = publishedAt,
        duration = (audio?.duration ?: 0).div(60).coerceAtLeast(1),
    )
}