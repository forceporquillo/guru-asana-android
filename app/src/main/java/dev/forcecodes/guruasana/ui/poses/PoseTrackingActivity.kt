package dev.forcecodes.guruasana.ui.poses

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.MimeTypeMap
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.ActivityPoseDetectorBinding
import dev.forcecodes.guruasana.domain.usecase.gallery.loadImages
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel.CameraUiEvent
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel.CameraViewState
import dev.forcecodes.guruasana.ui.poses.camera.CapturePoseFragment
import dev.forcecodes.guruasana.ui.poses.camera.LivePoseFeedFragment
import dev.forcecodes.guruasana.ui.poses.gallery.GalleryFragment
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.doOnApplyWindowInsets
import dev.forcecodes.guruasana.utils.extensions.rotateViewWhenClicked
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

@AndroidEntryPoint
class PoseTrackingActivity : AppCompatActivity() {

    private val detectPoseViewModel by viewModels<DetectPosesViewModel>()
    private val binding by viewBinding(ActivityPoseDetectorBinding::inflate)
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var outputDirectory: File
    private var viewIsDestroyed = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory(this)

        detectPoseViewModel.categoryLevel = intent.getIntExtra("level", -1)

        binding.root.doOnApplyWindowInsets { _, insets, padding ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.toolbar.updateLayoutParams {
                (this as MarginLayoutParams).topMargin = padding.top + statusBar.top
            }
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.croppedView.updatePadding(bottom = padding.bottom + navBar.bottom)
        }

        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.timer) {
                binding.cameraViewType.isVisible = false
                binding.timerChipGroup.isVisible = true
                return@setOnMenuItemClickListener true
            }

            return@setOnMenuItemClickListener false
        }

        binding.timerChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val timer = when (checkedIds.first()) {
                R.id.timer_off -> 0
                R.id.timer_2_sec -> 2
                R.id.timer_5_sec -> 5
                R.id.timer_10_sec -> 10
                else -> 0
            }

            detectPoseViewModel.shutterTimer = timer

            binding.cameraViewType.isVisible = true
            binding.timerChipGroup.isVisible = false
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cameraViewType.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val event = when (checkedIds.first()) {
                R.id.live -> CameraUiEvent.OnToggleLive
                R.id.photo -> CameraUiEvent.OnTogglePhoto
                else -> throw IllegalStateException()
            }
            // send ui event asap
            detectPoseViewModel.sendEvent(event)

            if (event is CameraUiEvent.OnTogglePhoto) {
                binding.shutterButton.setImageResource(R.drawable.ic_shutter)
                detectPoseViewModel.resetClassificationState()
            }

            val fragment: Fragment = when (event) {
                is CameraUiEvent.OnTogglePhoto -> CapturePoseFragment()
                is CameraUiEvent.OnToggleLive -> LivePoseFeedFragment()
                else -> throw IllegalStateException()
            }
            commitTransaction(fragment)
        }

        lifecycleScope.launchWhenCreated {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                detectPoseViewModel.state.collect { cameraViewState ->
                    val isCameraSelected =
                        cameraViewState is CameraViewState.Photo
                    binding.progressBar.isInvisible = !isCameraSelected
                    val timerMenu = binding.toolbar.menu.findItem(R.id.timer)
                    timerMenu.isVisible = isCameraSelected

                    if (!isCameraSelected) {
                        val state = cameraViewState as CameraViewState.Live
                        val shutterResId = if (state.runClassification) {
                            R.drawable.ic_shutter_pressed
                        } else {
                            R.drawable.ic_shutter
                        }
                        binding.shutterButton.setImageResource(shutterResId)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                detectPoseViewModel.imageGallery.collectLatest {
                    val image = if (it.isNotEmpty()) {
                        it.firstOrNull()?.contentUri
                    } else {
                        loadImages(this@PoseTrackingActivity).firstOrNull()?.contentUri
                    }

                    image?.let { uri ->
                        binding.mediaGallery.load(uri)
                    }

                    binding.mediaGalleryContainer.isVisible = image != null
                }
            }
        }

        binding.cameraFlip.rotateViewWhenClicked {
            val newLensFacing =
                if (detectPoseViewModel.currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }

            detectPoseViewModel.sendEvent(
                CameraUiEvent.OnToggleLens(
                    newLensFacing, binding.cameraViewType.checkedChipId == R.id.live
                )
            )
        }

        binding.shutterButton.setOnClickListener { view ->
            if (detectPoseViewModel.currentViewState is CameraViewState.Photo) {
                detectPoseViewModel.imageCapture?.let { capture ->
                    // Create output file to hold the image
                    val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

                    // Setup image capture metadata
                    val metadata = ImageCapture.Metadata().apply {

                        // Mirror image when using the front camera
                        isReversedHorizontal =
                            detectPoseViewModel.currentLensFacing == CameraSelector.LENS_FACING_FRONT
                    }

                    // Create output options object which contains file + metadata
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                        .setMetadata(metadata)
                        .build()

                    capture.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)

                                // If the folder selected is an external media directory, this is
                                // unnecessary but otherwise other apps will not be able to access our
                                // images unless we scan them using [MediaScannerConnection]
                                val mimeType = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(savedUri.toFile().extension)

                                MediaScannerConnection.scanFile(
                                    this@PoseTrackingActivity,
                                    arrayOf(savedUri.toFile().absolutePath),
                                    arrayOf(mimeType)
                                ) { _, uri ->
                                    Logger.d("Image capture scanned into media store: $uri")
                                }

                                view.isEnabled = false

                                startShutterCountdown {
                                    // Run the operations in the view's thread
                                    detectPoseViewModel.notifyGalleryInteractor()
                                }

                                lifecycleScope.launchWhenCreated {
                                    delay(detectPoseViewModel.shutterTimer.toLong())
                                    binding.root.apply {
                                        postDelayed({
                                            foreground = ColorDrawable(Color.WHITE)
                                            postDelayed(
                                                { foreground = null },
                                                ANIMATION_FAST_MILLIS
                                            )
                                        }, ANIMATION_SLOW_MILLIS)
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Logger.e("Photo captured failed: ${exception.message}", exception)
                            }

                        })
                }
            } else {
                detectPoseViewModel.sendEvent(CameraUiEvent.OnClickLive)
            }
        }

        binding.mediaGalleryContainer.setOnClickListener {
            // Only navigate when the gallery has photos
            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
                commitTransaction(
                    GalleryFragment(),
                    false,
                    bundleOf("rootDirectory" to outputDirectory.absolutePath)
                )
                supportFragmentManager.fragments.forEach {
                    if (it is CapturePoseFragment || it is LivePoseFeedFragment) {
                        supportFragmentManager.commit { remove(it) }
                    }
                }
            }
        }
    }

    private fun startShutterCountdown(block: () -> Unit) = synchronizeWithUiThread {
        val shutterTimeMillis = detectPoseViewModel.shutterTimer.toLong()

        if (shutterTimeMillis == 0L || shutterTimeMillis.toInt() == 0) {
            block.invoke()
            shutterButton.isEnabled = true
            return@synchronizeWithUiThread
        }

        val numberOfSeconds = shutterTimeMillis / MILLIS_INTERVAL
        val factor = 100 / numberOfSeconds

        shutterTime.isVisible = true
        countDownTimer = object : CountDownTimer(shutterTimeMillis, MILLIS_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / MILLIS_INTERVAL).toInt()

                val remaining = ((numberOfSeconds - secondsRemaining) * factor).toInt()
                shutterTime.text = ((secondsRemaining + 1).takeIf { it != 0 } ?: "").toString()

                animateShutterCount()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(remaining, true)
                } else {
                    progressBar.progress = remaining
                }
            }

            override fun onFinish() {
                shutterTime.isVisible = false
                shutterButton.isEnabled = true
                root.postDelayed({
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(0, true)
                    } else {
                        progressBar.progress = 0
                    }
                }, 250L)
                block.invoke()
            }
        }

        countDownTimer?.start()
    }

    private fun animateShutterCount() {
        YoYo.with(Techniques.ZoomOut)
            .duration(MILLIS_INTERVAL)
            .playOn(binding.shutterTime)
    }

    private fun createFile(baseFolder: File, format: String, extension: String): File {
        val category = detectPoseViewModel.poseClassificationLevel?.type ?: "all_in_category"
        return File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + "-$category$extension"
        )
    }

    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(), PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Logger.i("Permission granted: $permission")
            return true
        }
        Logger.i("Permission NOT granted: $permission")
        return false
    }

    override fun onResume() {
        onAttachFragment()
        super.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraSwitchButton()
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        val enable = try {
            hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            false
        }
        binding.cameraFlip.isEnabled = enable
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return detectPoseViewModel.cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return detectPoseViewModel.cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    private fun synchronizeWithUiThread(block: ActivityPoseDetectorBinding.() -> Unit) {
        handler.post {
            try {
                if (viewIsDestroyed.not()) {
                    block.invoke(binding)
                }
            } catch (e: IllegalStateException) {
                Logger.e(e)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewIsDestroyed = true
    }

    fun onReAttachFragment() {
        onAttachFragment()
        super.onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            onAttachFragment()
        } else {
            super.onBackPressed()
        }
    }

    private fun onAttachFragment() {
        synchronizeWithUiThread {
            if (detectPoseViewModel.currentViewState is CameraViewState.Photo) {
                commitTransaction(CapturePoseFragment())
            } else {
                commitTransaction(LivePoseFeedFragment())
            }
        }
    }

    private fun commitTransaction(
        fragment: Fragment,
        replace: Boolean = true,
        bundle: Bundle? = null
    ) {
        supportFragmentManager.commit {
            if (replace) {
                replace(R.id.nav_host_camera_container, fragment)
            } else {
                add(R.id.root_container, fragment::class.java, bundle)
                addToBackStack(fragment::class.java.simpleName)
            }
        }
    }

    companion object {

        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L

        private const val MILLIS_INTERVAL = 1000L

        private const val PERMISSION_REQUESTS = 1
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        private val REQUIRED_RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

}