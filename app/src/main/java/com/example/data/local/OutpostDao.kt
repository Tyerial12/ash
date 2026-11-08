package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutpostDao {
    @Query("SELECT * FROM outpost_state WHERE id = 1")
    fun observeOutpostState(): Flow<OutpostStateEntity?>

    @Query("SELECT * FROM outpost_state WHERE id = 1")
    suspend fun getOutpostStateOnce(): OutpostStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutpostState(state: OutpostStateEntity)

    @Query("SELECT * FROM buildings")
    fun observeBuildings(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings")
    suspend fun getBuildingsOnce(): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuildings(buildings: List<BuildingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuilding(building: BuildingEntity)

    @Query("SELECT * FROM worker_classes")
    fun observeWorkerClasses(): Flow<List<WorkerClassEntity>>

    @Query("SELECT * FROM worker_classes")
    suspend fun getWorkerClassesOnce(): List<WorkerClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkerClasses(workerClasses: List<WorkerClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkerClass(workerClass: WorkerClassEntity)

    @Query("SELECT * FROM skill_nodes")
    fun observeSkillNodes(): Flow<List<SkillNodeEntity>>

    @Query("SELECT * FROM skill_nodes")
    suspend fun getSkillNodesOnce(): List<SkillNodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkillNodes(nodes: List<SkillNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkillNode(node: SkillNodeEntity)

    @Query("SELECT * FROM event_logs ORDER BY timestampMillis DESC LIMIT 35")
    fun observeRecentLogs(): Flow<List<EventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EventLogEntity)

    @Query("DELETE FROM event_logs")
    suspend fun clearLogs()
}
