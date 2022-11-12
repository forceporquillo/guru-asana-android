package dev.forcecodes.guruasana.ui.auth.login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentAuthChooserBinding
import dev.forcecodes.guruasana.ui.NavFragment

class AuthChooserFragment : NavFragment(R.layout.fragment_auth_chooser) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthChooserBinding.bind(view)

        binding.guestLogin.setOnClickListener {
            findNavController().navigate(R.id.action_auth_chooser_fragment_to_guest_login_dialog_fragment)
        }

        binding.emailSignInBtn.setOnClickListener {
            findNavController().navigate(R.id.action_auth_chooser_fragment_to_loginFragment)
        }
    }
}
