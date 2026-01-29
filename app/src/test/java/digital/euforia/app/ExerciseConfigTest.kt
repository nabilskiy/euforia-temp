package digital.euforia.app

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import digital.euforia.app.data.config.NetworkExerciseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExerciseConfigTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testBannerEntityParsing() {
        val json = """
        [
          {
            "type": "banner",
            "entity": {
              "img_url": "https://euforia.digital/storage/manual/jpg/3/32d08c57af9c238eb8cb7a4cfa7fc470.jpg",
              "action_url": "euforia://exercises?ids=37,27,10"
            }
          }
        ]
        """.trimIndent()

        val listType = Types.newParameterizedType(List::class.java, NetworkExerciseConfig::class.java)
        val adapter = moshi.adapter<List<NetworkExerciseConfig>>(listType)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val banner = result[0]
        assertEquals("banner", banner.type)
        assertNotNull("Entity should not be null", banner.entity)
        assertEquals("https://euforia.digital/storage/manual/jpg/3/32d08c57af9c238eb8cb7a4cfa7fc470.jpg", banner.entity?.imageUrl)
        assertEquals("euforia://exercises?ids=37,27,10", banner.entity?.actionUrl)
    }

    @Test
    fun testExerciseParsingWithEntityId() {
        val json = """
        [
          {
            "type": "exercise",
            "entityId": 49,
            "data": {
              "title": "Awake your body and mind",
              "description": "The outcome will suprise you"
            }
          }
        ]
        """.trimIndent()

        val listType = Types.newParameterizedType(List::class.java, NetworkExerciseConfig::class.java)
        val adapter = moshi.adapter<List<NetworkExerciseConfig>>(listType)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val exercise = result[0]
        assertEquals("exercise", exercise.type)
        assertEquals(49, exercise.entityId)
        assertNotNull(exercise.data)
        assertEquals("Awake your body and mind", exercise.data?.title)
        assertEquals("The outcome will suprise you", exercise.data?.description)
    }

    @Test
    fun testExerciseListParsingWithEntityIds() {
        val json = """
        [
          {
            "type": "exercise_list",
            "entityIds": [
              81,
              5,
              25
            ],
            "data": {
              "title": "Welcome",
              "maxItems": 10
            }
          }
        ]
        """.trimIndent()

        val listType = Types.newParameterizedType(List::class.java, NetworkExerciseConfig::class.java)
        val adapter = moshi.adapter<List<NetworkExerciseConfig>>(listType)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val exerciseList = result[0]
        assertEquals("exercise_list", exerciseList.type)
        assertEquals(listOf(81, 5, 25), exerciseList.entityIds)
        assertEquals("Welcome", exerciseList.data?.title)
        assertEquals(10, exerciseList.data?.maxItems)
    }
}
