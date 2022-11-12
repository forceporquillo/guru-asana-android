package dev.forcecodes.guruasana.ui.poses

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.ramotion.cardslider.CardSliderLayoutManager
import com.ramotion.cardslider.CardSnapHelper
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentPoseDetectionBinding
import dev.forcecodes.guruasana.databinding.ItemIdentifyPosesLayoutBinding
import dev.forcecodes.guruasana.model.Poses
import dev.forcecodes.guruasana.utils.binding.executeAfter
import dev.forcecodes.guruasana.utils.binding.viewBinding

class PoseDetectionFragment : Fragment() {

    companion object {
        fun newInstance() = PoseDetectionFragment()
    }

    private lateinit var viewModel: PoseDetectionViewModel

    @Suppress("DEPRECATION")
    fun invertInsets(darkTheme: Boolean, window: Window) {
        if (Build.VERSION.SDK_INT >= 30) {
            //Correct way of doing things
            val statusBar = APPEARANCE_LIGHT_STATUS_BARS
            val navBar = APPEARANCE_LIGHT_NAVIGATION_BARS
            if (!darkTheme) {
                window.insetsController?.setSystemBarsAppearance(statusBar, statusBar)
                //   window.insetsController?.setSystemBarsAppearance(navBar, navBar)
            } else {
                window.insetsController?.setSystemBarsAppearance(0, statusBar)
                //   window.insetsController?.setSystemBarsAppearance(0, navBar)
            }
        } else {
            // Does bitwise operations (or to add, inverse or to remove)
            // This is depreciated but the new version is API 30+ so I should have this here
            val flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    if (Build.VERSION.SDK_INT >= 26) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0

            if (!darkTheme) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or flags
            } else {
                window.decorView.systemUiVisibility =
                    (window.decorView.systemUiVisibility.inv() or flags).inv()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = requireActivity().window
        //   window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white_2)
        //()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pose_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DetectablePosesAdapter()
        val layoutManger = CardSliderLayoutManager(requireContext())
        FragmentPoseDetectionBinding.bind(view).listView.run {
            //   this.adapter = InfiniteScrollAdapter.wrap(adapter)
            this.adapter = adapter
//            setItemTransformer(ScaleTransformer.Builder()
//                .setMinScale(.9f)
//                .build())
//            setOrientation(DSVOrientation.HORIZONTAL)
//            setItemTransitionTimeMillis(250)
//            setOffscreenItems(2)
            CardSnapHelper().attachToRecyclerView(this)
        }
    }

    internal class DetectablePosesAdapter :
        RecyclerView.Adapter<DetectablePosesAdapter.DetectablePosesViewHolder>() {

        private val items = mutableListOf(
            Poses(
                "Cobra Pose",
                "Bhujangasana",
                R.drawable.beginner_cobra_pose,
                R.color.light_blue
            ),
            Poses(
                "Mountain Pose",
                "Tadasana",
                R.drawable.beginner_mountain_pose,
                R.color.light_blue
            ),
            Poses(
                "Upward Salute",
                "Urdhva Hastasana",
                R.drawable.beginner_upward_salute,
                R.color.light_blue
            ),
            Poses(
                "Standing Forward",
                "Uttanasana",
                R.drawable.beginner_standing_forward,
                R.color.light_blue
            )
        )

        internal class DetectablePosesViewHolder(
            private val binding: ItemIdentifyPosesLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(poses: Poses) {
                binding.executeAfter {
                    this.poses = poses
                    poseCardContainer.setCardBackgroundColor(ContextCompat.getColor(root.context, poses.cardColor))
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DetectablePosesViewHolder {
            val binding = parent.viewBinding(ItemIdentifyPosesLayoutBinding::inflate)
            return DetectablePosesViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DetectablePosesViewHolder, position: Int) {
           holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}