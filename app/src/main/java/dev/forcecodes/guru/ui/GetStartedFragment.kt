package dev.forcecodes.guru.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentGetStartedBinding
import dev.forcecodes.guru.utils.binding.viewBinding

class GetStartedFragment : Fragment(R.layout.fragment_get_started) {

    private val binding by viewBinding(FragmentGetStartedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.getStartedBtn.setOnClickListener {
            findNavController().navigate(R.id.action_get_started_trampoline_fragment_to_auth_chooser_fragment)
        }
    }
}