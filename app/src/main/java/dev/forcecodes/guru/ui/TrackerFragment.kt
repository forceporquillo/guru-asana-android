package dev.forcecodes.guru.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentSignUpBinding
import dev.forcecodes.guru.utils.binding.viewBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TrackerFragment : Fragment(R.layout.fragment_sign_up) {

  private val binding by viewBinding(FragmentSignUpBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

//    binding.buttonSecond.setOnClickListener {
//      findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//    }
  }
}