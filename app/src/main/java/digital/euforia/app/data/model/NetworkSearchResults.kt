package digital.euforia.app.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkSearchResults(
    val exercises: List<NetworkExercise>,
    val articles: List<NetworkArticle>,
    val meditations: List<NetworkMeditation>,
    val scenes: List<NetworkScene>,
)