package dev.forcecodes.guruasana.poseprocessor.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import dev.forcecodes.guruasana.poseprocessor.camera.GraphicOverlay.Graphic

/**
 * Draw camera image to background.
 */
class CameraImageGraphic(
    overlay: GraphicOverlay?,
    private val bitmap: Bitmap
) : Graphic(overlay) {
    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, transformationMatrix, null)
    }
}