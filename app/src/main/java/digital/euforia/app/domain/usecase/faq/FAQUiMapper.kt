package digital.euforia.app.domain.usecase.faq

import digital.euforia.app.data.db.entity.FaqCategory
import digital.euforia.app.data.db.entity.FaqCategoryWithItems
import digital.euforia.app.data.db.entity.FaqItem
import digital.euforia.app.ui.settings.faq.UiFAQCategory
import digital.euforia.app.ui.settings.faq.UiFAQItem


fun FaqCategoryWithItems.mapToUi(): UiFAQCategory {
    return UiFAQCategory(
        id = this.category.id,
        title = this.category.name,
        items = this.items.mapToUiList(),
    )
}

fun List<FaqItem>.mapToUiList(): List<UiFAQItem> {
    return this.map { it.mapToUi() }
}

fun FaqItem.mapToUi(): UiFAQItem {
    return UiFAQItem(
        id = this.id,
        question = this.question,
        answer = this.answer,
    )
}

