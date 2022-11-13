package dev.forcecodes.guruasana.poseprocessor

fun interface InferenceInfoGraphicCallback {
    fun intercept(frameLatency: Long, detectorLatency: Long, fps: Int)
}