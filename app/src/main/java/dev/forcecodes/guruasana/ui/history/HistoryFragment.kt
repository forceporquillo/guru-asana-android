package dev.forcecodes.guruasana.ui.history

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentHistoryBinding
import dev.forcecodes.guruasana.databinding.ItemHistoryLayoutBinding
import dev.forcecodes.guruasana.model.PostProcessMetrics
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle
import dev.forcecodes.guruasana.utils.extensions.setupToolbarNavigateUp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val binding by viewBinding(FragmentHistoryBinding::bind)

    private val viewModel by viewModels<HistoryViewModel>()

    private val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm:ss")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarNavigateUp(binding.toolbar)

        val adapter = HistoryAdapter()
        binding.listView.adapter = adapter

        launchWithViewLifecycle {
            viewModel.processMetrics.collect {
                val message = if (it.isNotEmpty()) {
                    val size = it.map { it.poseName }.distinct().size
                    "You have already acquired $size out of 11 unique yoga poses as of ${sdf.format(Date())}(GMT+8)"
                } else {
                    getString(R.string.message_no_post_detected)
                }
                binding.containerMessage.text = message
                adapter.submitList(it)
            }
        }
    }

    inner class HistoryAdapter :
        ListAdapter<PostProcessMetrics, HistoryAdapter.HistoryViewHolder>(PostProcessMetrics.COMPARATOR) {

        inner class HistoryViewHolder(
            private val binding: ItemHistoryLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(metrics: PostProcessMetrics) {
                binding.apply {
                    poseClassName.text = metrics.poseName

                    val category = when (metrics.category) {
                        "beginner" -> "Entry flow"
                        "intermediate" -> "Mid flow"
                        "advanced" -> "Final relaxation"
                        else -> "N/A"
                    }

                    poseLevel.text = "Category: $category"
                    confidencePercentage.text = "${metrics.confidence}% Confidence"

                    val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(metrics.timestamp),
                            ZoneId.systemDefault()
                        ).toString()
                    } else {
                        sdf.format(Date(metrics.timestamp))
                    }
                    timestamp.text = date
                }
            }
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            return HistoryViewHolder(parent.viewBinding(ItemHistoryLayoutBinding::inflate))
        }
    }
}