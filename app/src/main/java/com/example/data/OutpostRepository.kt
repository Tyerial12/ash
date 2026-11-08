package com.example.data

import com.example.data.local.BuildingEntity
import com.example.data.local.EventLogEntity
import com.example.data.local.OutpostDao
import com.example.data.local.OutpostStateEntity
import com.example.data.local.SkillNodeEntity
import com.example.data.local.WorkerClassEntity
import com.example.model.BuildingType
import com.example.model.SkillTreeCatalog
import com.example.model.WorkerClassType
import kotlinx.coroutines.flow.Flow

class OutpostRepository(private val dao: OutpostDao) {
    val outpostStateFlow: Flow<OutpostStateEntity?> = dao.observeOutpostState()
    val buildingsFlow: Flow<List<BuildingEntity>> = dao.observeBuildings()
    val workersFlow: Flow<List<WorkerClassEntity>> = dao.observeWorkerClasses()
    val skillsFlow: Flow<List<SkillNodeEntity>> = dao.observeSkillNodes()
    val logsFlow: Flow<List<EventLogEntity>> = dao.observeRecentLogs()

    suspend fun ensureSeeded() {
        getInitialSnapshotOrSeed()
    }

    suspend fun getStateSnapshot(): OutpostStateEntity? {
        return dao.getOutpostStateOnce()
    }

    suspend fun getSkillSnapshot(nodeId: String): SkillNodeEntity? {
        return dao.getSkillNodesOnce().firstOrNull { it.nodeId == nodeId }
    }

    suspend fun updateOutpostState(state: OutpostStateEntity) {
        dao.upsertOutpostState(state)
    }

    suspend fun updateBuildingLevel(buildingTypeId: String, level: Int) {
        dao.upsertBuilding(BuildingEntity(buildingTypeId = buildingTypeId, level = level))
    }

    suspend fun updateWorkerClass(worker: WorkerClassEntity) {
        dao.upsertWorkerClasses(listOf(worker))
    }

    suspend fun updateSkillRank(nodeId: String, rank: Int) {
        dao.upsertSkillNode(SkillNodeEntity(nodeId = nodeId, currentRank = rank))
    }

    suspend fun performPrestigeReset(newState: OutpostStateEntity) {
        dao.upsertOutpostState(newState)
        val resetBuildings = BuildingType.entries.map { type ->
            BuildingEntity(buildingTypeId = type.id, level = 0)
        }
        dao.upsertBuildings(resetBuildings)
        val resetWorkers = WorkerClassType.entries.map { type ->
            WorkerClassEntity(
                workerClassId = type.id,
                assignedCount = 0,
                classLevel = 1,
                classXp = 0.0
            )
        }
        dao.upsertWorkerClasses(resetWorkers)
        val existingSkills = dao.getSkillNodesOnce().associateBy { it.nodeId }
        val resetSkills = SkillTreeCatalog.nodes.map { node ->
            val keepRank = if (node.id.startsWith("LEG_")) {
                existingSkills[node.id]?.currentRank ?: 0
            } else {
                0
            }
            SkillNodeEntity(nodeId = node.id, currentRank = keepRank)
        }
        dao.upsertSkillNodes(resetSkills)
    }

    suspend fun resetEntireGame(): OutpostStateEntity {
        val freshState = OutpostStateEntity(
            lastSavedTimestampMillis = System.currentTimeMillis()
        )
        dao.upsertOutpostState(freshState)
        val resetBuildings = BuildingType.entries.map { type ->
            BuildingEntity(buildingTypeId = type.id, level = 0)
        }
        dao.upsertBuildings(resetBuildings)
        val resetWorkers = WorkerClassType.entries.map { type ->
            WorkerClassEntity(
                workerClassId = type.id,
                assignedCount = 0,
                classLevel = 1,
                classXp = 0.0
            )
        }
        dao.upsertWorkerClasses(resetWorkers)
        val resetSkills = SkillTreeCatalog.nodes.map { node ->
            SkillNodeEntity(nodeId = node.id, currentRank = 0)
        }
        dao.upsertSkillNodes(resetSkills)
        dao.insertLog(
            EventLogEntity(
                category = "RESTART",
                message = "Game Restarted! Day 1: You start fresh in an empty broken Vault with your Rusty Pickaxe."
            )
        )
        return freshState
    }

    suspend fun getInitialSnapshotOrSeed(): InitialOutpostSnapshot {
        var state = dao.getOutpostStateOnce()
        var buildings = dao.getBuildingsOnce()
        var workers = dao.getWorkerClassesOnce()
        var skills = dao.getSkillNodesOnce()
        val isFirstLaunch = state == null

        if (state == null) {
            state = OutpostStateEntity(
                lastSavedTimestampMillis = System.currentTimeMillis()
            )
            dao.upsertOutpostState(state)
        }

        if (buildings.isEmpty()) {
            buildings = BuildingType.entries.map { type ->
                BuildingEntity(buildingTypeId = type.id, level = 0)
            }
            dao.upsertBuildings(buildings)
        }

        if (workers.isEmpty()) {
            workers = WorkerClassType.entries.map { type ->
                WorkerClassEntity(
                    workerClassId = type.id,
                    assignedCount = 0, // Zero hired dwellers initially!
                    classLevel = 1,
                    classXp = 0.0
                )
            }
            dao.upsertWorkerClasses(workers)
        }

        if (skills.isEmpty()) {
            skills = SkillTreeCatalog.nodes.map { node ->
                SkillNodeEntity(nodeId = node.id, currentRank = 0)
            }
            dao.upsertSkillNodes(skills)
        }

        if (isFirstLaunch) {
            dao.insertLog(
                EventLogEntity(
                    category = "VAULT",
                    message = "Day 1: You have nothing but a Rusty Pickaxe. Mine stone, fuel & Gold to build a Power Generator and Vault Shelter before the Day 10 Siege!"
                )
            )
        }

        return InitialOutpostSnapshot(
            state = state,
            buildings = buildings,
            workers = workers,
            skills = skills,
            isFirstLaunch = isFirstLaunch
        )
    }

    suspend fun logEvent(category: String, message: String) {
        dao.insertLog(
            EventLogEntity(
                category = category,
                message = message,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }
}

data class InitialOutpostSnapshot(
    val state: OutpostStateEntity,
    val buildings: List<BuildingEntity>,
    val workers: List<WorkerClassEntity>,
    val skills: List<SkillNodeEntity>,
    val isFirstLaunch: Boolean
)
