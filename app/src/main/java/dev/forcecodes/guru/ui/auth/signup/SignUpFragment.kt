package dev.forcecodes.guru.ui.auth.signup

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.FragmentSignUpBinding
import dev.forcecodes.guru.utils.binding.ViewBindingFragment
import dev.forcecodes.guru.utils.setupLoadingState
import dev.forcecodes.guru.widget.SnackbarMessage
import dev.forcecodes.guru.widget.SnackbarMessageManager
import dev.forcecodes.guru.widget.setupSnackbarManager
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : ViewBindingFragment<FragmentSignUpBinding>(R.layout.fragment_sign_up) {

    @Inject
    lateinit var messageManager: SnackbarMessageManager

    private val viewModel: SignUpViewModel by viewModels()

    override fun viewDidLoad(binding: FragmentSignUpBinding?) {
        setupSnackbarManager(messageManager)
    }

    override fun invalidate() = withState(viewModel) { state ->
        setupLoadingState(state.isLoading)
        if (state.isSuccess) {
            val modal = SignUpStateModalSheet()
            modal.show(childFragmentManager, SignUpStateModalSheet.TAG)
        }

        if (!state.error.isNullOrEmpty()) {
            val message = SnackbarMessage(message = state.error)
            messageManager.addMessage(message)
        }
    }
}