package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetProgramsWithChildrenUseCase @Inject constructor(
    private val repository: PackageRepository
) {
    suspend operator fun invoke(): ResultWrapper<List<PackageWithChildren>> {
        return withContext(Dispatchers.IO) {
            try {
                repository.getAllPackagesWithChildren()
            } catch (e: Exception) {
                ResultWrapper.Failure(e)
            }
        }
    }
}