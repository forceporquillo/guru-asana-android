package dev.forcecodes.guru.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentSignUpBinding
import dev.forcecodes.guru.ui.NavFragment
import dev.forcecodes.guru.utils.binding.viewBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : NavFragment(R.layout.fragment_sign_up) {

    private val binding by viewBinding(FragmentSignUpBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}