package dev.forcecodes.guruasana.ui.dashboard

import androidx.lifecycle.ViewModel
import dev.forcecodes.guruasana.R
import kotlinx.coroutines.flow.MutableStateFlow

class DashboardViewModel : ViewModel() {

    val actionState = MutableStateFlow(R.id.dashboard)

    fun setViewToShow(first: Int) {
        actionState.value =
            if (first == R.id.dashboard_chip)
                R.id.dashboard else R.id.history
    }
}