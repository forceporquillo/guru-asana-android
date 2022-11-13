package dev.forcecodes.guruasana.poseprocessor

import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor

fun interface ConfidencePoseGraphicCallback {
    fun onGetPoseClassification(result: List<PoseClassifierProcessor.PoseResult>)
}