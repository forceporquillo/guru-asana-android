package dev.forcecodes.guruasana.ui.poses.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guruasana.data.db.CustomizePosesDao
import dev.forcecodes.guruasana.model.Poses
import dev.forcecodes.guruasana.ui.poses.RecognizablePosesFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRoutinesViewModel @Inject constructor(
    private val recognizablePosesFactory: RecognizablePosesFactory,
    private val customizePosesDao: CustomizePosesDao
) : ViewModel() {

    private val _myRoutinePoses = MutableStateFlow<List<Poses>?>(null)
    val myRoutinePoses = _myRoutinePoses.asStateFlow()

    val poses: List<Poses>?
        get() = myRoutinePoses.value

    init {
        viewModelScope.launch {
            customizePosesDao.getSelectedPoses().collect {
                val items = it.flatMap { customize ->
                    val poses = recognizablePosesFactory.getPoses(customize.level ?: 0)
                    poses.filter { pose -> pose.title == customize.poseName }
                }
                _myRoutinePoses.value = items.toList()
            }
        }
    }
}