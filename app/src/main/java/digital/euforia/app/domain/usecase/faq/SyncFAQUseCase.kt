package digital.euforia.app.domain.usecase.faq

import digital.euforia.app.data.repository.FaqCategoryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncFAQUseCase @Inject constructor(
    private val faqCategoryRepository: FaqCategoryRepository,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            faqCategoryRepository.syncAll()
        }
    }
}