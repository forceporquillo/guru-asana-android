package dev.forcecodes.guruasana.ui.poses.routines

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramotion.cardslider.CardSliderLayoutManager
import com.ramotion.cardslider.CardSnapHelper
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentMyRoutinesBinding
import dev.forcecodes.guruasana.databinding.ItemIdentifyPosesLayoutBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.model.Poses
import dev.forcecodes.guruasana.ui.poses.PoseTrackingActivity
import dev.forcecodes.guruasana.ui.poses.RecognizablePosesFactory
import dev.forcecodes.guruasana.utils.binding.executeAfter
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle
import dev.forcecodes.guruasana.utils.extensions.navigate
import dev.forcecodes.guruasana.utils.extensions.setupToolbarNavigateUp
import javax.inject.Inject

@AndroidEntryPoint
class MyRoutinesFragment : Fragment(R.layout.fragment_my_routines) {

    private val binding by viewBinding(FragmentMyRoutinesBinding::bind)
    private val viewModel by viewModels<MyRoutinesViewModel>()

    private var cardSliderLayoutManager: CardSliderLayoutManager? = null
    private var currentPosition: Int = -1

    @Inject
    lateinit var recognizablePosesFactory: RecognizablePosesFactory

    override fun onResume() {
        super.onResume()
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
        val adapter = DetectablePosesAdapter { pose, position ->
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

        initPosesListView(adapter)
        binding.listView.adapter = adapter

        launchWithViewLifecycle {
            viewModel.myRoutinePoses.collect { poses ->
                poses ?: return@collect
                binding.apply {
                    if (poses.isEmpty()) {
                        poseButton.isVisible = false
                        scrollview.isVisible = false
                        emptyContainer.isVisible = true
                        adapter.submitList(emptyList())
                    } else {
                        onPoseCardPositionChanged(0)
                        poseButton.isVisible = true
                        scrollview.isVisible = true
                        emptyContainer.isVisible = false
                        adapter.submitList(poses)
                    }
                    toolbar.menu.findItem(R.id.edit).isVisible = poses.isNotEmpty()
                }

            }
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.edit) {
                navigate(R.id.action_my_routines_to_customize_pose)
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }
        binding.createButton.let { button ->
            button.setOnClickListener {
                if (it.isVisible.not()) return@setOnClickListener
                navigate(R.id.action_my_routines_to_customize_pose)
            }
        }

        binding.poseButton.setOnClickListener {
            val intent = Intent(requireActivity(), PoseTrackingActivity::class.java)
            startActivity(intent)
        }

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
        try {
            val pose = viewModel.poses?.get(position)
            println("Level ${pose?.level}")
            val content = recognizablePosesFactory.getAllPoses().find {
                it?.pose == pose?.title
            }

            println("Content $content")
            binding.description.text = content?.description
            binding.instructions.text = content?.instructions
            currentPosition = position
        } catch (e: IndexOutOfBoundsException) {
            Logger.e(e)
        }
    }

    internal class DetectablePosesAdapter(
        private val block: (Poses, Int) -> Unit
    ) : ListAdapter<Poses, DetectablePosesAdapter.DetectablePosesViewHolder>(Poses.COMPARATOR) {

        inner class DetectablePosesViewHolder(
            private val binding: ItemIdentifyPosesLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    block.invoke(getItem(adapterPosition), adapterPosition)
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
            holder.bind(getItem(position))
        }
    }

    override fun onStart() {
        super.onStart()
        onPoseCardPositionChanged(0)
    }
}