package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetProgramWithChildrenUseCase @Inject constructor(
    private val repository: PackageRepository
) {
    suspend operator fun invoke(id: Int): ResultWrapper<PackageWithChildren?> {
        return withContext(Dispatchers.IO) {
            try {
                repository.getProgramWithChildrenById(id)
            } catch (e: Exception) {
                ResultWrapper.Failure(e)
            }
        }
    }
}