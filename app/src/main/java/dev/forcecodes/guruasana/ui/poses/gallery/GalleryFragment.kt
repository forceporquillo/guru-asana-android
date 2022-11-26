package dev.forcecodes.guruasana.ui.poses.gallery

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.UseCase
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import coil.load
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentGalleryBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.model.PostProcessMetrics
import dev.forcecodes.guruasana.poseprocessor.ConfidencePoseGraphicCallback
import dev.forcecodes.guruasana.poseprocessor.InferenceInfoGraphicCallback
import dev.forcecodes.guruasana.poseprocessor.PoseDetectorProcessor
import dev.forcecodes.guruasana.poseprocessor.VisionImageProcessor
import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor
import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor.PoseDifficultyLevel
import dev.forcecodes.guruasana.poseprocessor.utils.BitmapUtils
import dev.forcecodes.guruasana.ui.history.HistoryViewModel
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel
import dev.forcecodes.guruasana.ui.poses.PoseTrackingActivity
import dev.forcecodes.guruasana.ui.poses.camera.CameraPoseProviderFragment
import dev.forcecodes.guruasana.ui.poses.camera.LivePoseFeedFragment
import dev.forcecodes.guruasana.ui.poses.camera.capitalizeWords
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.doOnApplyWindowInsets
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

val EXTENSION_WHITELIST = arrayOf("JPG")

/** Fragment used to present the user with a gallery of photos taken */
@AndroidEntryPoint
class GalleryFragment internal constructor() :
    CameraPoseProviderFragment(R.layout.fragment_gallery),
    InferenceInfoGraphicCallback, ConfidencePoseGraphicCallback {

    private lateinit var mediaList: MutableList<File>

    private val binding by viewBinding(FragmentGalleryBinding::bind)

    private var file: File? = null

    /** Fragment used for each individual page showing a photo inside of [GalleryFragment] */

    private var visionImageProcessor: VisionImageProcessor? = null

    private var selectedFile: File? = null

    private val historyViewModel by viewModels<HistoryViewModel>()

    internal class PhotoFragment internal constructor() : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ) = ImageView(context)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val args = arguments ?: return
            val resource = args.getString(FILE_NAME_KEY)?.let { File(it) } ?: R.drawable.ic_photo
            (view as ImageView).load(resource)
        }

        companion object {
            private const val FILE_NAME_KEY = "file_name"

            fun create(image: File) = PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(FILE_NAME_KEY, image.absolutePath)
                }
            }
        }
    }

    override fun onBindCameraUseCase(cameraViewState: DetectPosesViewModel.CameraViewState) =
        emptyArray<UseCase>()

    private fun onSaveClassificationResults(formattedPose: String, confidence: Float?) {
        val lastPathSegment = file?.toUri()?.lastPathSegment ?: ""

        var level: PoseDifficultyLevel? = null

        if (lastPathSegment.contains("beginner")) {
            level = PoseDifficultyLevel.BEGINNER
        } else if (lastPathSegment.contains("intermediate")) {
            level = PoseDifficultyLevel.INTERMEDIATE
        } else if (lastPathSegment.contains("advanced")) {
            level = PoseDifficultyLevel.ADVANCED
        }

        val poseMetrics = PostProcessMetrics(
            poseName = formattedPose,
            confidence = "${confidence?.times(100)?.toInt()}",
            tag = file?.toString(),
            category = level?.type
        )
        historyViewModel.saveClassificationResults(poseMetrics)
    }

    override fun onGetPoseClassificationChangeListener(result: List<PoseClassifierProcessor.PoseResult>) {
        synchronizeWithUiThread(binding) {
            for (poseResult in result) {
                val pose = if (poseResult.poseName == null) {
                    getString(R.string.recognizing_default_pose)
                } else {
                    val confidence = poseResult.confidence?.times(100)?.toInt()
                    "${poseResult.poseName} - $confidence% Confidence"
                }
                val formattedPose = pose.replace(LivePoseFeedFragment.REGEX, "")
                    .replace("_", " ")
                binding.poseName.text = formattedPose.capitalizeWords.trimStart()
                if (!pose.startsWith("Recognizing your pose")) {
                    Logger.d("Saving image $file")
                    onSaveClassificationResults(
                        poseResult.poseName?.replace(
                            LivePoseFeedFragment.REGEX,
                            ""
                        )?.replace("_", " ")
                            ?.capitalizeWords
                            ?.trimStart() ?: "N/A", poseResult.confidence
                    )
                }
            }
        }
    }

    override fun onInferenceInfoChangedListener(
        frameLatency: Long,
        detectorLatency: Long,
        framesPerSecond: Int
    ) {
        synchronizeWithUiThread(binding) {
            frameLatencyTv.text = getString(R.string.frame_latency_ms, frameLatency.toString())
            detectorLatencyTv.text =
                getString(R.string.detector_latency_ms, detectorLatency.toString())
        }
    }

    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    private val insetsController: WindowInsetsControllerCompat? by lazy {
        activity?.window?.let { window -> WindowInsetsControllerCompat(window, window.decorView) }
    }

    private val detectorOptions by lazy {
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
            .setExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    private fun setInsetLight(light: Boolean) {
        insetsController?.isAppearanceLightStatusBars = light
        insetsController?.isAppearanceLightNavigationBars = light
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = arguments?.getString("rootDirectory")?.let { File(it) }

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory?.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()

        setInsetLight(true)
    }

    private fun ensureImageProcessorIsInitialized(file: File) {
        binding.graphicOverlay.clear()
        this.file = file

        // stop existing process
        if (visionImageProcessor != null) {
            visionImageProcessor?.stop()
            visionImageProcessor = null
        }

        val lastPathSegment = file.toUri().lastPathSegment ?: ""

        var level: PoseDifficultyLevel? = null

        if (lastPathSegment.contains("beginner")) {
            level = PoseDifficultyLevel.BEGINNER
        } else if (lastPathSegment.contains("intermediate")) {
            level = PoseDifficultyLevel.INTERMEDIATE
        } else if (lastPathSegment.contains("advanced")) {
            level = PoseDifficultyLevel.ADVANCED
        } else if (lastPathSegment.contains("all_category")) {
            level = PoseDifficultyLevel.ALL
        }

        if (level == null) {
            binding.poseName.text = "Failed to classify still pose."
            Toast.makeText(
                requireContext().applicationContext,
                "Failed to process: Pose Level metadata was not found. Do not modify image name.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        visionImageProcessor = PoseDetectorProcessor(
            requireContext(),
            detectorOptions,
            level,
            true,
            true,
            this,
            this
        )

        val bitmap = decodePath(file)

        bitmap?.let {
            binding.graphicOverlay.setImageSourceInfo(
                it.width, it.height, false
            )
            Logger.d("Width ${it.width} Height ${it.height}")
            visionImageProcessor?.processBitmap(it, binding.graphicOverlay)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        clearMetadataInfo()

        binding.root.doOnApplyWindowInsets { _, insets, padding ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.toolbar.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).topMargin = padding.top + statusBar.top
            }
            binding.photoViewPager.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).topMargin = padding.top + statusBar.top
            }
            binding.graphicOverlay.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).topMargin = padding.top + statusBar.top
            }
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.root.updatePadding(bottom = padding.bottom + navBar.bottom)
        }

        //Checking media files list
        if (mediaList.isEmpty()) {
            //  fragmentGalleryBinding.deleteButton.isEnabled = false
            binding.classifyPose.isEnabled = false
        } else {
            selectedFile = mediaList.get(0)
        }
        // Populate the ViewPager and implement a cache of two media items
        binding.photoViewPager.apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }
        binding.photoViewPager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                selectedFile = mediaList[position]
            }

            override fun onPageScrollStateChanged(state: Int) {
                binding.classifyPose.isEnabled = state == SCROLL_STATE_IDLE
                binding.graphicOverlay.clear()
                clearMetadataInfo()
            }
        })

        binding.classifyPose.setOnClickListener {
            selectedFile?.let {
                binding.poseName.text = "Classifying still pose. Please wait..."
                ensureImageProcessorIsInitialized(it)
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as PoseTrackingActivity)
                .onReAttachFragment()
        }
    }

    private fun decodePath(file: File): Bitmap? {
        val contentResolver = requireActivity().contentResolver
        return BitmapUtils.getBitmapFromContentUri(contentResolver, file.toUri())
    }

    override fun onDestroyView() {
        setInsetLight(false)
        super.onDestroyView()
    }

    private fun clearMetadataInfo() {
        binding.apply {
            poseName.text = "Image not classified"
            frameLatencyTv.text = "Frame Latency: N/A"
            detectorLatencyTv.text = "Detector Latency: N/A"
        }
    }
}
