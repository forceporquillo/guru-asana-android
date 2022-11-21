package dev.forcecodes.guruasana.poseprocessor.classification

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.common.base.Preconditions
import com.google.mlkit.vision.pose.Pose
import dev.forcecodes.guruasana.poseprocessor.classification.PoseSample.Companion.getPoseSample
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Accepts a stream of [Pose] for classification and Rep counting.
 */
@Suppress("unused")
class PoseClassifierProcessor @WorkerThread constructor(
    context: Context,
    isStreamMode: Boolean,
    level: PoseDifficultyLevel
) {
    private val isStreamMode: Boolean
    private var emaSmoothing: EMASmoothing? = null
    private var repCounters: MutableList<RepetitionCounter>? = null
    private var poseClassifier: PoseClassifier? = null
    private var lastRepResult: PoseResult = PoseResult(null, null, null)

    data class PoseResult(
        val poseName: String?,
        val confidence: Float?,
        val repeatCount: Int? = 0
    )

    enum class PoseDifficultyLevel(val type: String) {
        BEGINNER("beginner"),
        INTERMEDIATE("intermediate"),
        ADVANCED("advanced"),
        ALL("all_category")
    }

    init {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper())
        this.isStreamMode = isStreamMode
        if (isStreamMode) {
            emaSmoothing = EMASmoothing()
            repCounters = ArrayList()
            lastRepResult = PoseResult(null, null, null)
        }
        loadPoseSamples(context, level)
    }

    private fun loadPoseSamples(context: Context, poseDifficultyLevel: PoseDifficultyLevel) {
        val poseSamples: MutableList<PoseSample> = ArrayList()
        try {
            Log.d(TAG,"Retrieving Trained Samples: ${context.assets.open("poses/${poseDifficultyLevel.type}.csv")}")
            val reader = BufferedReader(
                InputStreamReader(context.assets.open("poses/${poseDifficultyLevel.type}.csv"))
            )
            var csvLine = reader.readLine()
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                val poseSample = getPoseSample(csvLine, ",")
                if (poseSample != null) {
                    poseSamples.add(poseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error when loading pose samples.\n$e")
        }
        poseClassifier = PoseClassifier(poseSamples)
    }

    /**
     * Given a new [Pose] input, returns a list of formatted [String]s with Pose
     * classification results.
     *
     *
     * Currently it returns up to 2 strings as following:
     * 0: PoseClass : X reps
     * 1: PoseClass : [0.0-1.0] confidence
     */
    @WorkerThread
    fun getPoseResult(pose: Pose): MutableList<PoseResult> {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper())
        val result: MutableList<PoseResult> = ArrayList()
        var classification = poseClassifier!!.classify(pose)

        if (isStreamMode) {
            // Feed pose to smoothing even if no pose found.
            classification = emaSmoothing!!.getSmoothedResult(classification)

            // Return early without updating repCounter if no pose found.
            if (pose.allPoseLandmarks.isEmpty()) {
                result.add(lastRepResult)
                return result
            }
            result.add(lastRepResult)
        }

        // Add maxConfidence class of current frame to result if pose is found.
        if (pose.allPoseLandmarks.isNotEmpty()) {
            val maxConfidenceClass = classification.maxConfidenceClass
            val poseResult = PoseResult(
                maxConfidenceClass,
                classification.getClassConfidence(maxConfidenceClass)
                        / poseClassifier!!.confidenceRange()
            )
            result.add(poseResult)
        }
        return result
    }

    companion object {
        private const val TAG = "PoseClassifierProcessor"
    }
}