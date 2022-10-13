package dev.forcecodes.guru.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentLoginBinding
import dev.forcecodes.guru.ui.NavFragment
import dev.forcecodes.guru.utils.binding.viewBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class LoginFragment : NavFragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Login Button Clicked!", Toast.LENGTH_SHORT).show()
        }
        binding.createAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_login_fragment_to_sign_up_fragment)
        }
    }
}