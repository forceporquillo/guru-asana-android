package dev.forcecodes.guruasana.poseprocessor.classification

import android.util.Pair
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import dev.forcecodes.guruasana.poseprocessor.classification.PoseEmbedding.getPoseEmbedding
import java.util.ArrayList
import java.util.PriorityQueue

/**
 * Classifies [Pose] based on given [PoseSample]s.
 *
 *
 * Inspired by K-Nearest Neighbors Algorithm with outlier filtering.
 * https://en.wikipedia.org/wiki/K-nearest_neighbors_algorithm
 */
class PoseClassifier @JvmOverloads constructor(
    private val poseSamples: List<PoseSample>,
    private val maxDistanceTopK: Int = MAX_DISTANCE_TOP_K,
    private val meanDistanceTopK: Int = MEAN_DISTANCE_TOP_K,
    private val axesWeights: PointF3D = AXES_WEIGHTS
) {
    /**
     * Returns the max range of confidence values.
     *
     *
     * <Since we calculate confidence by counting></Since>[PoseSample]s that survived
     * outlier-filtering by maxDistanceTopK and meanDistanceTopK, this range is the minimum of two.
     */
    fun confidenceRange(): Int {
        return maxDistanceTopK.coerceAtMost(meanDistanceTopK)
    }

    fun classify(pose: Pose): ClassificationResult {
        return classify(extractPoseLandmarks(pose))
    }

    fun classify(landmarks: List<PointF3D>): ClassificationResult {
        val result = ClassificationResult()
        // Return early if no landmarks detected.
        if (landmarks.isEmpty()) {
            return result
        }

        // We do flipping on X-axis so we are horizontal (mirror) invariant.
        val flippedLandmarks: List<PointF3D> = ArrayList(landmarks)
        Utils.multiplyAll(flippedLandmarks.toMutableList(), PointF3D.from(-1f, 1f, 1f))
        val embedding = getPoseEmbedding(landmarks)
        val flippedEmbedding = getPoseEmbedding(flippedLandmarks)

        // Classification is done in two stages:
        //  * First we pick top-K samples by MAX distance. It allows to remove samples that are almost
        //    the same as given pose, but maybe has few joints bent in the other direction.
        //  * Then we pick top-K samples by MEAN distance. After outliers are removed, we pick samples
        //    that are closest by average.

        // Keeps max distance on top so we can pop it when top_k size is reached.
        val maxDistances = PriorityQueue(
            maxDistanceTopK
        ) { o1: Pair<PoseSample, Float?>, o2: Pair<PoseSample, Float?> ->
            -(o1.second!!).compareTo(o2.second!!)
        }
        // Retrieve top K poseSamples by least distance to remove outliers.
        for (poseSample in poseSamples) {
            val sampleEmbedding = poseSample.embedding
            var originalMax = 0f
            var flippedMax = 0f
            for (i in embedding.indices) {
                originalMax = originalMax.coerceAtLeast(
                    Utils.maxAbs(
                        Utils.multiply(
                            Utils.subtract(
                                embedding[i], sampleEmbedding[i]
                            ), axesWeights
                        )
                    )
                )
                flippedMax = flippedMax.coerceAtLeast(
                    Utils.maxAbs(
                        Utils.multiply(
                            Utils.subtract(
                                flippedEmbedding[i], sampleEmbedding[i]
                            ), axesWeights
                        )
                    )
                )
            }
            // Set the max distance as min of original and flipped max distance.
            maxDistances.add(Pair(poseSample, originalMax.coerceAtMost(flippedMax)))
            // We only want to retain top n so pop the highest distance.
            if (maxDistances.size > maxDistanceTopK) {
                maxDistances.poll()
            }
        }

        // Keeps higher mean distances on top so we can pop it when top_k size is reached.
        val meanDistances = PriorityQueue(
            meanDistanceTopK
        ) { o1: Pair<PoseSample, Float?>, o2: Pair<PoseSample, Float?> ->
            -(o1.second!!).compareTo(o2.second!!)
        }
        // Retrieve top K poseSamples by least mean distance to remove outliers.
        for (sampleDistances in maxDistances) {
            val poseSample = sampleDistances.first
            val sampleEmbedding = poseSample.embedding
            var originalSum = 0f
            var flippedSum = 0f
            for (i in embedding.indices) {
                originalSum += Utils.sumAbs(
                    Utils.multiply(
                        Utils.subtract(embedding[i], sampleEmbedding[i]), axesWeights
                    )
                )
                flippedSum += Utils.sumAbs(
                    Utils.multiply(
                        Utils.subtract(
                            flippedEmbedding[i], sampleEmbedding[i]
                        ), axesWeights
                    )
                )
            }
            // Set the mean distance as min of original and flipped mean distances.
            val meanDistance = originalSum.coerceAtMost(flippedSum) / (embedding.size * 2)
            meanDistances.add(Pair(poseSample, meanDistance))
            // We only want to retain top k so pop the highest mean distance.
            if (meanDistances.size > meanDistanceTopK) {
                meanDistances.poll()
            }
        }
        for (sampleDistances in meanDistances) {
            val className = sampleDistances.first.className
            result.incrementClassConfidence(className)
        }
        return result
    }

    companion object {

        private const val MAX_DISTANCE_TOP_K = 30
        private const val MEAN_DISTANCE_TOP_K = 10

        // Note Z has a lower weight as it is generally less accurate than X & Y.
        private val AXES_WEIGHTS = PointF3D.from(1f, 1f, 0.2f)
        private fun extractPoseLandmarks(pose: Pose): List<PointF3D> {
            val landmarks: MutableList<PointF3D> = ArrayList()
            for (poseLandmark in pose.allPoseLandmarks) {
                landmarks.add(poseLandmark.position3D)
            }
            return landmarks
        }
    }
}