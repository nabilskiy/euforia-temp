package digital.euforia.app.domain.usecase.program

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.config.ExerciseBlockType
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class GetExerciseBlocksComposeUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val publicationInfoMapper: PublicationInfoMapper
) {

    suspend operator fun invoke(): ResultWrapper<List<ExerciseUiBlock>> {
        val config = configFetcher.getExercisesConfig()

        return withContext(Dispatchers.IO) {
            // Build compose request structure from config
            val requestItems = config.mapNotNull { block ->
                when (block.type) {
                    ExerciseBlockType.EXERCISE_LIST, ExerciseBlockType.EXERCISE_PREMIUM -> {
                        val ids = block.entityIds ?: emptyList()
                        if (ids.isEmpty()) return@mapNotNull null
                        val items = ids.map { id ->
                            mutableMapOf<String, Any>(
                                "type" to "exercise",
                                "id" to id,
                                "key" to "ex_${id}"
                            )
                        }
                        mutableMapOf<String, Any>(
                            "key" to (block.data?.title ?: "list_${ids.joinToString("_")}"),
                            "type" to "list",
                            "items" to items
                        )
                    }

                    ExerciseBlockType.EXERCISE_CATEGORY -> {
                        val categoryId = block.entityId ?: return@mapNotNull null
                        val filters = mutableMapOf<String, Any>(
                            "category_id" to listOf(categoryId)
                        )
                        mutableMapOf<String, Any>(
                            "key" to (block.data?.title ?: "exercise_category_$categoryId"),
                            "type" to "exercise_list",
                            "filters" to filters
                        )
                    }

                    ExerciseBlockType.EXERCISE -> {
                        val id = block.entityId ?: return@mapNotNull null
                        mutableMapOf<String, Any>(
                            "key" to (block.data?.title ?: "exercise_$id"),
                            "type" to "exercise",
                            "id" to id
                        )
                    }

                    else -> null // Skip unsupported types for compose request
                }
            }

            if (requestItems.isEmpty()) {
                return@withContext ResultWrapper.Failure(Exception("No valid blocks for compose"))
            }

            val moshi = Moshi.Builder().build()
            val listType = Types.newParameterizedType(List::class.java, MutableMap::class.java)
            val adapter = moshi.adapter<List<MutableMap<String, Any>>>(listType)
            val json = adapter.toJson(requestItems)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody: RequestBody = json.toRequestBody(mediaType)

            api.compose(requestBody).map { response ->
                ExerciseUiBlock.fromComposeResponse(
                    config = config,
                    response = response,
                    publicationInfoMapper = publicationInfoMapper,
                    moshi = moshi
                )
            }
        }
    }
}