package dev.forcecodes.guruasana.ui.dashboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.YogaPosesFactory
import dev.forcecodes.guruasana.databinding.OtherPoseLayoutBinding
import dev.forcecodes.guruasana.databinding.YogaCategoryItemBinding
import dev.forcecodes.guruasana.databinding.YogaCategoryLayoutBinding
import dev.forcecodes.guruasana.model.PoseUiModel
import dev.forcecodes.guruasana.utils.binding.executeAfter
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.utils.extensions.navigate
import dev.forcecodes.guruasana.utils.extensions.onNavigateWhenInvoked

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var viewModel: DashboardViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOtherPoseView(view)
        initYogaCategoryView(view)
    }

    // Entry point for included OtherPoseLayoutBinding
    private fun initOtherPoseView(view: View) {
        val binding = OtherPoseLayoutBinding.bind(view)
        binding.poseCardContainer.executeAfter {
            pose = PoseUiModel(
                "Become fluent with Yoga",
                "Learn different yoga poses and proper practices",
                R.drawable.other_poses_thumbnail,
                multiPoses = false
            )
            onNavigateWhenInvoked("pose")
        }
    }

    // Entry point for included YogaCategoryLayoutBinding
    private fun initYogaCategoryView(view: View) {
        val categoryBinding = YogaCategoryLayoutBinding.bind(view)
        categoryBinding.listView.adapter = YogaCategoryAdapter { category ->
            println(category)
            navigate("category")
        }
        YogaPosesFactory.singleton(requireContext())
    }

    internal class YogaCategoryAdapter(
        private val listener: (Category) -> Unit
    ) : RecyclerView.Adapter<YogaCategoryAdapter.YogaCategoryViewHolder>() {

        private val categories = Category.create()

        internal data class Category(
            val title: String,
            @ColorRes val colorId: Int,
            @DrawableRes val drawable: Int
        ) {
            companion object {
                fun create() = listOf(
                    Category("Entry flow", R.color.light_blue, R.drawable.yoga_basic),
                    Category("Mid flow", R.color.light_peach, R.drawable.yoga_intermediate),
                    Category("Final relaxation", R.color.light_leaf, R.drawable.yoga_professional)
                )
            }
        }

        inner class YogaCategoryViewHolder(
            private val binding: YogaCategoryItemBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    listener.invoke(categories[adapterPosition])
                }
            }

            fun bind(category: Category) {
                binding.apply {
                    title.text = category.title
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            category.colorId
                        )
                    )
                    thumbnail.setImageResource(category.drawable)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YogaCategoryViewHolder {
            val binding = parent.viewBinding(YogaCategoryItemBinding::inflate)
            return YogaCategoryViewHolder(binding)
        }

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: YogaCategoryViewHolder, position: Int) {
            holder.bind(categories[position])
        }
    }
}