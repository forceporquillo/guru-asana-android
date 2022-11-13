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
    level: PoseLevel
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

    enum class PoseLevel(val type: String) {
        BEGINNER("beginner"),
        INTERMEDIATE("intermediate"),
        ADVANCED("advanced")
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

    private fun loadPoseSamples(context: Context, poseLevel: PoseLevel) {
        val poseSamples: MutableList<PoseSample> = ArrayList()
        try {
            Log.d(TAG,"Retrieving Trained Samples: ${context.assets.open("poses/${poseLevel.type}.csv")}")
            val reader = BufferedReader(
                InputStreamReader(context.assets.open("poses/${poseLevel.type}.csv"))
            )
            var csvLine = reader.readLine()
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                val poseSample = getPoseSample(csvLine, ",")
                Log.d(TAG, "PoseSamples ${poseSample.toString()}")
                if (poseSample != null) {
                    poseSamples.add(poseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error when loading pose samples.\n$e")
        }
        poseClassifier = PoseClassifier(poseSamples)
        if (isStreamMode) {
            for (className in POSE_CLASSES) {
                repCounters!!.add(RepetitionCounter(className))
            }
        }
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

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.
        if (isStreamMode) {
            // Feed pose to smoothing even if no pose found.
            classification = emaSmoothing!!.getSmoothedResult(classification)

            // Return early without updating repCounter if no pose found.
            if (pose.allPoseLandmarks.isEmpty()) {
                result.add(lastRepResult)
                return result
            }
//            for (repCounter in repCounters!!) {
//                val repsBefore = repCounter.numRepeats
//                val repsAfter = repCounter.addClassificationResult(classification)
//                if (repsAfter > repsBefore) {
//                    // Play a fun beep when rep counter updates.
//                    val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
//                    tg.startTone(ToneGenerator.TONE_PROP_BEEP)
////                    lastRepResult = String.format(
////                        Locale.US, "%s : %d reps", repCounter.className, repsAfter
////                    )
//                    lastRepResult = PoseResult(repCounter.className, lastRepResult.confidence, repsAfter)
//                    break
//                }
//            }
            result.add(lastRepResult)
        }

        // Add maxConfidence class of current frame to result if pose is found.
        if (!pose.allPoseLandmarks.isEmpty()) {
            val maxConfidenceClass = classification.maxConfidenceClass
            val maxConfidenceClassResult = String.format(
                Locale.US,
                "%s : %.2f confidence",
                maxConfidenceClass, classification.getClassConfidence(maxConfidenceClass)
                        / poseClassifier!!.confidenceRange()
            )
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

        // Specify classes for which we want rep counting.
        // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
        // for your pose samples.
        private const val PUSHUPS_CLASS = "pushups_down"
        private const val SQUATS_CLASS = "squats_down"
        private val POSE_CLASSES = arrayOf(
            PUSHUPS_CLASS, SQUATS_CLASS
        )
    }
}