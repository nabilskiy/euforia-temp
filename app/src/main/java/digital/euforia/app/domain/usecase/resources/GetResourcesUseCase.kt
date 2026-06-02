package digital.euforia.app.domain.usecase.resources

import digital.euforia.app.data.db.entity.Resource
import digital.euforia.app.data.repository.ResourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetResourcesUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {

    suspend operator fun invoke(alias: String): List<Resource> {
        return withContext(Dispatchers.IO) {
            resourceRepository.getByClassAlias(alias).dataOrNull ?: emptyList()
        }
    }
}