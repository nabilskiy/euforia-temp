package digital.euforia.app.domain.usecase.faq

import digital.euforia.app.data.db.entity.FaqCategoryWithItems
import digital.euforia.app.data.repository.FaqCategoryRepository
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFAQCategoriesUseCase @Inject constructor(
    private val faqCategoryRepository: FaqCategoryRepository
) {
    suspend operator fun invoke(): ResultWrapper<List<FaqCategoryWithItems>> {
        return withContext(Dispatchers.IO) {
            faqCategoryRepository.getAllWithItems()
//                .onFailure { error ->
//                    ResultWrapper.Failure(error)
//                }
        }
    }
}