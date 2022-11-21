package dev.forcecodes.guruasana.ui.poses

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ramotion.cardslider.CardSliderLayoutManager
import com.ramotion.cardslider.CardSnapHelper
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentPoseCategoryBinding
import dev.forcecodes.guruasana.databinding.ItemIdentifyPosesLayoutBinding
import dev.forcecodes.guruasana.model.Poses
import dev.forcecodes.guruasana.utils.binding.executeAfter
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.setupToolbarNavigateUp
import javax.inject.Inject

@AndroidEntryPoint
class PoseCategoryFragment : Fragment(R.layout.fragment_pose_category) {

    private val binding by viewBinding(FragmentPoseCategoryBinding::bind)

    @Inject
    lateinit var recognizablePosesFactory: RecognizablePosesFactory

    private var cardSliderLayoutManager: CardSliderLayoutManager? = null

    private var currentPosition: Int = -1

    private val categoryLevel by lazy { requireArguments().getInt("level") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.light_gray)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarNavigateUp(binding.toolbar)

        val category = requireArguments().getString("category")
        val level = requireArguments().getInt("level")

        binding.toolbar.title = category

        val poses = recognizablePosesFactory.getPoses(categoryLevel)
        val adapter = DetectablePosesAdapter(poses) { pose, position ->
            if (cardSliderLayoutManager?.isSmoothScrolling == true)
                return@DetectablePosesAdapter

            val activePosition = cardSliderLayoutManager!!.activeCardPosition

            if (activePosition == RecyclerView.NO_POSITION) {
                return@DetectablePosesAdapter
            }

            if (position > activePosition) {
                binding.listView.smoothScrollToPosition(position)
                onPoseCardPositionChanged(position)
            }
        }

        binding.poseButton.setOnClickListener {
            val intent = Intent(requireActivity(), PoseTrackingActivity::class.java).apply {
                putExtra("level", level)
            }
            startActivity(intent)
        }

        initPosesListView(adapter)
    }

    override fun onStart() {
        super.onStart()
        onPoseCardPositionChanged(0)
    }

    private fun initPosesListView(poseAdapter: DetectablePosesAdapter) = with(binding.listView) {
        adapter = poseAdapter
        cardSliderLayoutManager = layoutManager as CardSliderLayoutManager
        CardSnapHelper().attachToRecyclerView(this)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onPoseCardChange()
                }
            }
        })

        doOnLayout {
            it.postDelayed(0) {
                smoothScrollToPosition(1)
            }
            it.postDelayed(100L) {
                smoothScrollToPosition(0)
            }
        }
    }

    private fun onPoseCardChange() {
        val position = cardSliderLayoutManager?.activeCardPosition ?: -1
        if (position == RecyclerView.NO_POSITION || position == currentPosition) {
            return
        }
        onPoseCardPositionChanged(position)
    }

    private fun onPoseCardPositionChanged(position: Int) {
        val content = recognizablePosesFactory.getPoseContent(categoryLevel, position)
        binding.description.text = content?.description
        binding.instructions.text = content?.instructions
        currentPosition = position
    }

    internal class DetectablePosesAdapter(
        val poses: List<Poses>,
        private val block: (Poses, Int) -> Unit
    ) : RecyclerView.Adapter<DetectablePosesAdapter.DetectablePosesViewHolder>() {

        inner class DetectablePosesViewHolder(
            private val binding: ItemIdentifyPosesLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    block.invoke(poses[adapterPosition], adapterPosition)
                }
            }

            fun bind(poses: Poses) {
                binding.executeAfter {
                    this.poses = poses
                    poseCardContainer.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            poses.cardColor
                        )
                    )
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
            holder.bind(poses[position])
        }

        override fun getItemCount(): Int = poses.size
    }
}