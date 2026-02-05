package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import javax.inject.Inject

class GetSearchResultsUseCase @Inject constructor(
    private val euforiaApi: EuforiaApi,
    private val mapper: PublicationInfoMapper
) {
    suspend operator fun invoke(query: String): ResultWrapper<SearchResults> {
        return euforiaApi.search(query).map { networkSearchResults ->
            val exercises = networkSearchResults.exercises.map {
                mapper.fromNetworkExercise(it)
            }
            val articles = networkSearchResults.articles.map {
                mapper.fromNetworkArticle(it)
            }
            val meditations = networkSearchResults.meditations.map {
                mapper.fromNetworkMeditation(it)
            }
            SearchResults(
                exercises = exercises,
                articles = articles,
                meditations = meditations,
            )
        }
    }
}

data class SearchResults(
    val exercises: List<PublicationInfo>,
    val articles: List<PublicationInfo>,
    val meditations: List<PublicationInfo>,
//    val scenes: List<String>,
) {

    fun hasResults(): Boolean {
        return exercises.isNotEmpty() || articles.isNotEmpty() || meditations.isNotEmpty()
    }
}