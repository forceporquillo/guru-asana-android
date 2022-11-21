package dev.forcecodes.guruasana.ui.poses.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.data.db.CustomizePosesDao
import dev.forcecodes.guruasana.model.CustomizePose
import dev.forcecodes.guruasana.ui.poses.RecognizablePosesFactory
import dev.forcecodes.guruasana.utils.extensions.cancelIfActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CustomizePoseViewModel @Inject constructor(
    private val recognizablePosesFactory: RecognizablePosesFactory,
    private val customizePosesDao: CustomizePosesDao
) : ViewModel() {

    private var levelJob: Job? = null

    private val _customizePoses = MutableStateFlow(emptyList<CustomizePose>())
    val customizePose = _customizePoses.asStateFlow()

    fun getPoseCategory(checkedId: Int) {
        val level = when (checkedId) {
            R.id.entry_flow -> 0
            R.id.mid_flow -> 1
            R.id.final_relaxation -> 2
            else -> throw IllegalStateException()
        }

        levelJob?.cancelIfActive()
        levelJob = viewModelScope.launch {
            customizePosesDao.loadPosesByDifficulty(level).collect {
                _customizePoses.value = it
            }
        }
    }

    fun selectAll() {
        //_customizePoses.value = updatePoses(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
               val allChecks = _customizePoses.value.map {
                    it.copy(isChecked = true)
                }.toTypedArray()
                customizePosesDao.updatePoses(*allChecks)
            }
        }
    }

    fun clearAll() {
        // _customizePoses.value = updatePoses(false)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val allChecks = _customizePoses.value.map {
                    it.copy(isChecked = false)
                }.toTypedArray()
                customizePosesDao.updatePoses(*allChecks)
            }
        }
    }

    fun updatePose(pose: CustomizePose) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                customizePosesDao.insertPoses(pose)
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (customizePosesDao.getAllPoses().isEmpty()) {
                    println("Database is empty. Appending some items.")
                    for (i in 0..2) {
                        val poseItems = recognizablePosesFactory.getPoses(i)
                        val poseContent = recognizablePosesFactory.getPoseList(i) ?: emptyList()
                        val data = poseItems.zip(poseContent) { items, content ->
                            CustomizePose(
                                poseName = items.title,
                                sanskritName = items.sinkritName,
                                description = content.description ?: "",
                                accuracyRate = content.accuracyRate ?: "",
                                level = items.level,
                                drawableId = items.drawableId
                            )
                        }.toTypedArray()
                        customizePosesDao.insertPoses(*data)
                    }
                } else {
                    println("Database is not empty.")
                }
            }
        }
        getPoseCategory(R.id.entry_flow)
    }
}

