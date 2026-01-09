package digital.euforia.app.domain.model.settings

import androidx.annotation.Keep

@Keep
enum class FeedbackType(val typeName: String) {
    SUPPORT("support"),
    FEEDBACK("feedback"),
    QUESTION("question"),
    REPORT("report")
}