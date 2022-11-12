package dev.forcecodes.guruasana.ui.auth.signup

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.utils.setupLoadingState
import dev.forcecodes.guruasana.widget.SnackbarMessage
import dev.forcecodes.guruasana.widget.SnackbarMessageManager
import dev.forcecodes.guruasana.widget.setupSnackbarManager
import dev.forcecodes.guruasana.databinding.FragmentSignUpBinding
import dev.forcecodes.guruasana.utils.binding.ViewBindingFragment
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