package dev.forcecodes.guruasana.ui.poses

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.model.PoseContent
import dev.forcecodes.guruasana.model.PoseJournals
import dev.forcecodes.guruasana.model.Poses
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecognizablePosesFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val beginner = arrayOf(
        Poses(
            "Cobra Pose",
            "Bhujangasana",
            R.drawable.pose_beginner_cobra_pose,
            R.color.light_blue,
            0
        ),
        Poses(
            "Mountain Pose",
            "Tadasana",
            R.drawable.pose_beginner_mountain_pose,
            R.color.light_blue,
            0
        ),
        Poses(
            "Upward Salute",
            "Urdhva Hastasana",
            R.drawable.pose_beginner_upward_salute,
            R.color.light_blue,
            0
        ),
        Poses(
            "Standing Forward",
            "Uttanasana",
            R.drawable.pose_beginner_standing_forward,
            R.color.light_blue,
            0
        )
    )
    private val intermediate = arrayOf(
        Poses(
            "Balancing Table",
            "Dandayamna Bharmanasa",
            R.drawable.pose_intermediate_balancing_table,
            R.color.light_peach,
            1
        ),
        Poses(
            "Cat Pose",
            "Marjariasana",
            R.drawable.pose_intermediate_cat_pose,
            R.color.light_peach,
            1
        ),
        Poses(
            "Cow Pose",
            "Bitilasana",
            R.drawable.pose_intermediate_cow_pose,
            R.color.light_peach,
            1
        ),
        Poses(
            "Table Top Pose",
            "Bharmanasana",
            R.drawable.pose_intermediate_table_top_pose,
            R.color.light_peach,
            1
        )
    )
    private val advanced = arrayOf(
        Poses(
            "Warrior 1",
            "Virabhadrasana 1",
            R.drawable.pose_advanced_warrior_1,
            R.color.light_leaf,
            2
        ),
        Poses(
            "Warrior 2",
            "Virabhadrasana 2",
            R.drawable.pose_advanced_warrior_2,
            R.color.light_leaf,
            2
        ),
        Poses(
            "Warrior 3",
            "Virabhadrasana 2",
            R.drawable.pose_advanced_warrior_3,
            R.color.light_leaf,
            2
        )
    )

    fun getPoses(level: Int): List<Poses> {
        return when (level) {
            0 -> beginner
            1 -> intermediate
            2 -> advanced
            else -> throw IllegalStateException("Unknown Yoga category, but was $level")
        }.toList()
    }

    fun getPoseContent(level: Int, index: Int): PoseContent? {
        return getPoseList(level)?.get(index)
    }

    fun getPoseList(level: Int): List<PoseContent>? {
        val journals = PoseDescription.getFromCache(context)
        return when (level) {
            0 -> journals?.beginner
            1 -> journals?.intermediate
            2 -> journals?.advanced
            else -> throw IllegalStateException("Unknown Yoga category, but was $level")
        }
    }

    fun getAllPoses(): List<PoseContent?> {
        val journals = PoseDescription.getFromCache(context)
        val beginner = journals?.beginner ?: emptyList()
        val intermediate = journals?.intermediate ?: emptyList()
        val advanced = journals?.advanced ?: emptyList()
        return beginner + intermediate + advanced
    }

    private object PoseDescription {

        private var journals: PoseJournals? = null
        private const val FILENAME = "yoga_descriptions/yoga_journals.json"

        @JvmStatic
        fun getFromCache(context: Context): PoseJournals? {
            if (journals == null) {
                val string = context
                    .assets
                    .open(FILENAME)
                    .bufferedReader()
                    .use { it.readText() }

                val items = Moshi.Builder()
                    .build()
                    .adapter(PoseJournals::class.java)
                    .fromJson(string)

                journals = items
            }

            return journals
        }
    }
}