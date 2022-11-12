package dev.forcecodes.guruasana.ui

import androidx.fragment.app.Fragment
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.utils.binding.viewBinding
import dev.forcecodes.guruasana.databinding.FragmentSignUpBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TrackerFragment : Fragment(R.layout.fragment_sign_up) {

    private val binding by viewBinding(FragmentSignUpBinding::bind)

}