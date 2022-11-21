package dev.forcecodes.guruasana.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.forcecodes.guruasana.model.CustomizePose
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomizePosesDao {

    @Query("SELECT * FROM CustomizePose")
    fun getAllPoses(): List<CustomizePose>

    @Query("SELECT * FROM CustomizePose WHERE isChecked = 1")
    fun getSelectedPoses(): Flow<List<CustomizePose>>

    @Query("SELECT * FROM CustomizePose WHERE level=:level ORDER BY poseName ASC")
    fun loadPosesByDifficulty(level: Int): Flow<List<CustomizePose>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoses(vararg poses: CustomizePose)

    @Update
    suspend fun updatePoses(vararg pose: CustomizePose)
}
