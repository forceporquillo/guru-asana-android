package dev.forcecodes.guruasana.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.databinding.FragmentSelectRoutinesBinding
import dev.forcecodes.guruasana.databinding.ItemRoutineLayoutBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SelectRoutinesFragment : Fragment(R.layout.fragment_select_routines) {

    private val binding by viewBinding(FragmentSelectRoutinesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.routineList.adapter = RoutinesAdapter()
    }

    internal class RoutinesAdapter : RecyclerView.Adapter<RoutinesAdapter.RoutineViewHolder>() {

        internal data class Routine(
            val title: String,
            val difficulty: String,
            val sets: String,
            @ColorRes val colorId: Int,
            @DrawableRes val drawable: Int
        ) {
            companion object {
                fun create(): List<Routine> {
                    return arrayListOf(
                        Routine(
                            "Rhythm Focused",
                            "Beginner Level",
                            "4 Sets",
                            R.color.light_blue,
                            R.drawable.yoga_basic,
                        ),
                        Routine(
                            "Vinyasa",
                            "Intermediate Level",
                            "4 Sets",
                            R.color.light_peach,
                            R.drawable.yoga_intermediate
                        ),
                        Routine(
                            "Acro Yogis",
                            "Advanced Level",
                            "4 Sets",
                            R.color.light_gray,
                            R.drawable.yoga_professional
                        ),
                        Routine(
                            "Customize Routines",
                            "All Levels",
                            "4 Sets",
                            R.color.light_leaf,
                            R.drawable.yoga_custom
                        )
                    )
                }
            }
        }

        private val routines = Routine.create()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
            val binding = parent.viewBinding(ItemRoutineLayoutBinding::inflate)
            return RoutineViewHolder(binding)
        }

        class RoutineViewHolder(
            private val binding: ItemRoutineLayoutBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(routine: Routine) {
                binding.apply {
                    drawable.setImageResource(routine.drawable)
                    setRoutine(routine)
                    routineRoot.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            routine.colorId
                        )
                    )
                }
            }
        }

        override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
            holder.bind(routines[position])
        }

        override fun getItemCount(): Int {
            return routines.size
        }
    }
}