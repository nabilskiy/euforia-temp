package digital.euforia.app.domain.usecase.resources

import digital.euforia.app.data.db.entity.Resource
import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_AVATAR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetResourcesUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository,
    private val appPreferences: AppPreferences
) {

    suspend operator fun invoke(alias: String): List<Resource> {
        return withContext(Dispatchers.IO) {
            val resources = resourceRepository.getByClassAlias(alias).dataOrNull ?: emptyList()
            if (alias == CLASS_ALIAS_VOICE_AVATAR) {
                val deletedIds = appPreferences.getDeletedAvatarIds()
                resources.filterNot { deletedIds.contains(it.id) }
            } else {
                resources
            }
        }
    }
}