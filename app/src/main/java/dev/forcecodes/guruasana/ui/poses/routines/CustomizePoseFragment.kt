package dev.forcecodes.guruasana.ui.poses.routines

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentCustomizePoseBinding
import dev.forcecodes.guruasana.databinding.ItemCustomizeLayoutBinding
import dev.forcecodes.guruasana.model.CustomizePose
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle
import dev.forcecodes.guruasana.utils.extensions.setupToolbarNavigateUp

@AndroidEntryPoint
class CustomizePoseFragment : Fragment(R.layout.fragment_customize_pose) {

    private val binding by viewBinding(FragmentCustomizePoseBinding::bind)

    private val viewModel by viewModels<CustomizePoseViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarNavigateUp(binding.toolbar)

        val adapter = CustomizePoseAdapter {
            viewModel.updatePose(it)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.getPoseCategory(checkedIds.first())
        }

        launchWithViewLifecycle {
            viewModel.customizePose.collect { customizePoses ->
                val isAllSelected = customizePoses.all { it.isChecked == true }
                binding.clearOrSelectAll.text = if (isAllSelected) {
                    "Clear all"
                } else {
                    "Select all"
                }
                adapter.submitList(customizePoses)
            }
        }

        binding.clearOrSelectAll.apply {
            setOnClickListener {
                text = if (text == "Select all") {
                    viewModel.selectAll()
                    "Clear all"
                } else {
                    viewModel.clearAll()
                    "Select all"
                }
            }
        }

        // binding.listView.itemAnimator = null
        binding.listView.adapter = adapter
    }

    private class CustomizePoseAdapter(
        val selected: (CustomizePose) -> Unit
    ) : ListAdapter<CustomizePose, CustomizePoseAdapter.CustomizePoseViewHolder>(CustomizePose.COMPARATOR) {

        inner class CustomizePoseViewHolder(
            private val binding: ItemCustomizeLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(pose: CustomizePose) {
                binding.apply {
                    poseClassName.text = pose.poseName
                    sanskritName.text = pose.sanskritName
                    poseClassDescription.text = pose.description
                    confidencePercentage.text = "Confidence rate: ${pose.accuracyRate}"
                    poseImage.load(pose.drawableId)
                    checkbox.isChecked = pose.isChecked ?: false
                }

                binding.checkbox.setOnCheckedChangeListener { _, _ ->
                    pose.isChecked = !(pose.isChecked ?: false)
                    println(pose.poseName)
                    selected.invoke(pose)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizePoseViewHolder {
            return CustomizePoseViewHolder(parent.viewBinding(ItemCustomizeLayoutBinding::inflate))
        }

        override fun onBindViewHolder(holder: CustomizePoseViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}