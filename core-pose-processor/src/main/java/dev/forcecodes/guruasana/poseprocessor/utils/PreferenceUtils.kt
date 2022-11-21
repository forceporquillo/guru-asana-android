package dev.forcecodes.guruasana.poseprocessor.utils

import android.content.Context
import android.preference.PreferenceManager
import android.util.Size
import androidx.annotation.StringRes
import dev.forcecodes.guruasana.poseprocessor.camera.CameraSource.SizePair
import dev.forcecodes.guruasana.poseprocessor.camera.CameraSource
import androidx.camera.core.CameraSelector
import com.google.common.base.Preconditions
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import dev.forcecodes.guruasana.poseprocessor.R
import java.lang.Exception
import java.util.concurrent.Executors

/**
 * Utility class to retrieve shared preferences.
 */
@Suppress("deprecation")
object PreferenceUtils {
    private const val POSE_DETECTOR_PERFORMANCE_MODE_FAST = 1
    fun saveString(context: Context, @StringRes prefKeyId: Int, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(prefKeyId), value)
            .apply()
    }

    @JvmStatic
    fun getCameraPreviewSizePair(context: Context, cameraId: Int): SizePair? {
        Preconditions.checkArgument(
            cameraId == CameraSource.CAMERA_FACING_BACK
                    || cameraId == CameraSource.CAMERA_FACING_FRONT
        )
        val previewSizePrefKey: String
        val pictureSizePrefKey: String
        if (cameraId == CameraSource.CAMERA_FACING_BACK) {
            previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size)
        } else {
            previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size)
        }
        return try {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            SizePair(
                com.google.android.gms.common.images.Size.parseSize(
                    sharedPreferences.getString(
                        previewSizePrefKey,
                        null
                    )!!
                ),
                com.google.android.gms.common.images.Size.parseSize(
                    sharedPreferences.getString(
                        pictureSizePrefKey,
                        null
                    )!!
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getCameraXTargetResolution(context: Context, lensfacing: Int): Size? {
        Preconditions.checkArgument(
            lensfacing == CameraSelector.LENS_FACING_BACK
                    || lensfacing == CameraSelector.LENS_FACING_FRONT
        )
        val prefKey =
            if (lensfacing == CameraSelector.LENS_FACING_BACK)
                context.getString(R.string.pref_key_camerax_rear_camera_target_resolution)
            else context.getString(R.string.pref_key_camerax_front_camera_target_resolution)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            Size.parseSize(sharedPreferences.getString(prefKey, null))
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun shouldHideDetectionInfo(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_info_hide)
        return sharedPreferences.getBoolean(prefKey, false)
    }

    fun getPoseDetectorOptionsForLivePreview(context: Context): PoseDetectorOptionsBase {
        val performanceMode = getModeTypePreferenceValue(
            context,
            R.string.pref_key_live_preview_pose_detection_performance_mode,
            POSE_DETECTOR_PERFORMANCE_MODE_FAST
        )
        val preferGPU = preferGPUForPoseDetection(context)
        return if (performanceMode == POSE_DETECTOR_PERFORMANCE_MODE_FAST) {
            val builder = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setExecutor(Executors.newSingleThreadExecutor())
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        } else {
            val builder = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .setExecutor(Executors.newSingleThreadExecutor())
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        }
    }

    fun getPoseDetectorOptionsForStillImage(context: Context): PoseDetectorOptionsBase {
        val performanceMode = getModeTypePreferenceValue(
            context,
            R.string.pref_key_still_image_pose_detection_performance_mode,
            POSE_DETECTOR_PERFORMANCE_MODE_FAST
        )
        val preferGPU = preferGPUForPoseDetection(context)
        return if (performanceMode == POSE_DETECTOR_PERFORMANCE_MODE_FAST) {
            val builder =
                PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        } else {
            val builder = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        }
    }

    fun preferGPUForPoseDetection(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_pose_detector_prefer_gpu)
        return sharedPreferences.getBoolean(prefKey, true)
    }

    fun shouldShowPoseDetectionInFrameLikelihoodLivePreview(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey =
            context.getString(R.string.pref_key_live_preview_pose_detector_show_in_frame_likelihood)
        return sharedPreferences.getBoolean(prefKey, true)
    }

    fun shouldShowPoseDetectionInFrameLikelihoodStillImage(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey =
            context.getString(R.string.pref_key_still_image_pose_detector_show_in_frame_likelihood)
        return sharedPreferences.getBoolean(prefKey, true)
    }

    fun shouldPoseDetectionVisualizeZ(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_pose_detector_visualize_z)
        return sharedPreferences.getBoolean(prefKey, true)
    }

    fun shouldPoseDetectionRescaleZForVisualization(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_pose_detector_rescale_z)
        return sharedPreferences.getBoolean(prefKey, true)
    }

    fun shouldPoseDetectionRunClassification(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_pose_detector_run_classification)
        return sharedPreferences.getBoolean(prefKey, false)
    }

    /**
     * Mode type preference is backed by [android.preference.ListPreference] which only support
     * storing its entry value as string type, so we need to retrieve as string and then convert to
     * integer.
     */
    private fun getModeTypePreferenceValue(
        context: Context, @StringRes prefKeyResId: Int, defaultValue: Int
    ): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyResId)
        return sharedPreferences.getString(prefKey, defaultValue.toString())!!.toInt()
    }

    @JvmStatic
    fun isCameraLiveViewportEnabled(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_camera_live_viewport)
        return sharedPreferences.getBoolean(prefKey, false)
    }
}