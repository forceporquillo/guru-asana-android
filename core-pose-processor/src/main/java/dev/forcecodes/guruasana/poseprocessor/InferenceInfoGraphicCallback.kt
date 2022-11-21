package dev.forcecodes.guruasana.poseprocessor

fun interface InferenceInfoGraphicCallback {
    fun onInferenceInfoChangedListener(frameLatency: Long, detectorLatency: Long, framesPerSecond: Int)
}