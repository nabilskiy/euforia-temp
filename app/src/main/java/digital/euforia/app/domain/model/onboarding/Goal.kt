package digital.euforia.app.domain.model.onboarding

import com.squareup.moshi.JsonClass
import digital.euforia.app.R

//enum class Goal(val identifier: String, val code: String, val titleRes: Int) {
//    RELAX("relax", "G1", R.string.goal_relax),
//    SLEEP("sleep", "G2", R.string.goal_sleep),
//    TOXIC("toxic", "G3", R.string.goal_toxic),
//    EMOTIONAL("emotional", "G4", R.string.goal_emotional),
//    CONFIDENCE("confidence", "G5", R.string.goal_confidence),
//    WARMTH("warmth", "G6", R.string.goal_warmth),
//    LONELINESS("loneliness", "G7", R.string.goal_loneliness),
//    ESTEEM("esteem", "G8", R.string.goal_esteem),
//    INSPIRED("inspired", "G9", R.string.goal_inspired),
//    OTHER("other", "FF", R.string.goal_other);
//
//    companion object {
//        fun fromIdentifier(id: String): Goal? = entries.firstOrNull { it.identifier == id }
//        fun fromCode(code: String): Goal? = entries.firstOrNull { it.code == code }
//    }
//}

@JsonClass(generateAdapter = true)
data class Goal(
    val identifier: String,
    val code: String,
    val text: String
)