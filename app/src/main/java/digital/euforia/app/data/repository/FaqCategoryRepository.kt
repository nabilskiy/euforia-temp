package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.FaqCategoryDao
import digital.euforia.app.data.db.dao.FaqItemDao
import digital.euforia.app.data.db.entity.FaqCategory
import digital.euforia.app.data.db.entity.FaqCategoryWithItems
import digital.euforia.app.data.db.entity.FaqItem
import digital.euforia.app.data.model.NetworkFaqCategory
import digital.euforia.app.data.model.NetworkFaqItem
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaqCategoryRepository @Inject constructor(
    private val api: EuforiaApi,
    private val faqCategoryDao: FaqCategoryDao,
    private val faqItemDao: FaqItemDao,
) {

    fun getAllFlow(): Flow<List<FaqCategory>> = faqCategoryDao.getAllFlow()

    fun getAllWithItemsFlow(): Flow<List<FaqCategoryWithItems>> =
        faqCategoryDao.getAllWithItemsFlow()

//    fun getAllWithItems(): List<FaqCategoryWithItems> =
//        faqCategoryDao.getAllWithItems()

    suspend fun syncAll() {
        val result = api.getFaqCategories()
        result.onSuccess { list: List<NetworkFaqCategory> ->
            val entities = list.map(NetworkFaqCategory::toEntity)
            // Replace all to keep it simple for now
            withContext(Dispatchers.IO) {
                faqCategoryDao.clearAll()
                faqCategoryDao.insertAll(entities)
            }

            // After categories are synced, sync items for each category alias
            withContext(Dispatchers.IO) {
                try {
                    val allItems = mutableListOf<FaqItem>()
                    for (cat in entities) {
                        val itemsResult = api.getFaqItems(type = cat.alias)
                        itemsResult.onSuccess { items: List<NetworkFaqItem> ->
                            allItems += items.map(NetworkFaqItem::toEntity)
                        }.onFailure { e ->
                            Timber.e(e, "Failed to fetch FAQ items for alias=${cat.alias}")
                        }
                    }
                    // Replace all items with fetched ones
                    faqItemDao.clearAll()
                    if (allItems.isNotEmpty()) faqItemDao.insertAll(allItems)
                } catch (e: Exception) {
                    Timber.e(e, "Failed syncing FAQ items")
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to fetch FAQ categories")
        }
    }

//    suspend fun getAll(): ResultWrapper<List<FaqCategory>> {
//        return withContext(Dispatchers.IO) {
//            val result = api.getFaqCategories()
//            result.map { networkFaqCategories ->
//                networkFaqCategories.map(NetworkFaqCategory::toEntity)
//            }
//        }
//    }

    suspend fun getAllWithItems(): ResultWrapper<List<FaqCategoryWithItems>> {
        val result = api.getFaqCategories()
        return result.map { networkFaqCategories ->
            val entities = networkFaqCategories.map(NetworkFaqCategory::toEntity)

            if (entities.isNotEmpty()) {
                faqCategoryDao.clearAll()
                faqCategoryDao.insertAll(entities)
            }
            val allItems = mutableListOf<FaqItem>()
            for (cat in entities) {
                val itemsResult = api.getFaqItems(type = cat.alias)
                itemsResult.onSuccess { items: List<NetworkFaqItem> ->
                    allItems += items.map(NetworkFaqItem::toEntity)
                }.onFailure { e ->
                    Timber.e(e, "Failed to fetch FAQ items for alias=${cat.alias}")
                }
            }
            // Replace all items with fetched ones
            faqItemDao.clearAll()
            if (allItems.isNotEmpty()) faqItemDao.insertAll(allItems)

            faqCategoryDao.getAllWithItems()
        }
    }
}
