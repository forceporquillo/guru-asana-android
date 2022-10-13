package dev.forcecodes.guru.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentAuthChooserBinding
import dev.forcecodes.guru.ui.NavFragment

class AuthChooserFragment : NavFragment(R.layout.fragment_auth_chooser) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthChooserBinding.bind(view)

        binding.guestLogin.setOnClickListener {
            findNavController().navigate(R.id.action_auth_chooser_fragment_to_guest_login_dialog_fragment)
        }

        binding.emailSignInBtn.setOnClickListener {
            findNavController().navigate()
            findNavController().navigate(R.id.action_auth_chooser_fragment_to_loginFragment)
        }
    }
}
