package dev.forcecodes.guruasana.ui.auth.login

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentLoginBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.utils.binding.ViewBindingFragment
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle
import dev.forcecodes.guruasana.utils.extensions.navigate
import dev.forcecodes.guruasana.utils.setupLoadingState
import dev.forcecodes.guruasana.widget.SnackbarMessage
import dev.forcecodes.guruasana.widget.SnackbarMessageManager
import dev.forcecodes.guruasana.widget.setupSnackbarManager
import javax.inject.Inject

/**
 * [LoginFragment] for authenticating users.
 */
@AndroidEntryPoint
class LoginFragment : ViewBindingFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    @Inject
    lateinit var messageManager: SnackbarMessageManager

    private val viewModel by viewModels<LoginViewModel>()

    override fun viewDidLoad(binding: FragmentLoginBinding?) {
        setupSnackbarManager(messageManager)

        launchWithViewLifecycle {
            viewModel.uiEvent.collect {
                if (it is LoginUiEvent.Create) {
                    navigate(R.id.action_login_fragment_to_sign_up_fragment)
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        setupLoadingState(state.isLoading)

        Logger.d("State: $state")

        if (!state.error.isNullOrEmpty()) {
            val message = SnackbarMessage(message = state.error)
            messageManager.addMessage(message)
        }

        if (state.isAuthenticated) {
            Logger.e("Authenticated...")
        }
    }
}