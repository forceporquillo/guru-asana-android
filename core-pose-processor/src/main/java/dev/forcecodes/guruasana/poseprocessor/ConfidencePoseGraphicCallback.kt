package dev.forcecodes.guruasana.poseprocessor

import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor

fun interface ConfidencePoseGraphicCallback {
    fun onGetPoseClassificationChangeListener(result: List<PoseClassifierProcessor.PoseResult>)
}