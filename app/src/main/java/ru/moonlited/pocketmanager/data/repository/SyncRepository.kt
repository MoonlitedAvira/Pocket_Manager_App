package ru.moonlited.pocketmanager.data.repository

import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.SyncAttendanceDto
import ru.moonlited.pocketmanager.data.api.SyncMaslachDto
import ru.moonlited.pocketmanager.data.api.SyncMunsterbergDto
import ru.moonlited.pocketmanager.data.api.SyncRequest
import ru.moonlited.pocketmanager.data.api.SyncSanDto
import ru.moonlited.pocketmanager.data.api.SyncTaskDto
import ru.moonlited.pocketmanager.data.local.AppDatabase
import ru.moonlited.pocketmanager.data.local.entity.AttendanceEntity
import ru.moonlited.pocketmanager.data.local.entity.MaslachResultEntity
import ru.moonlited.pocketmanager.data.local.entity.MunsterbergResultEntity
import ru.moonlited.pocketmanager.data.local.entity.SanResultEntity
import ru.moonlited.pocketmanager.utils.AppLogger
import ru.moonlited.pocketmanager.utils.SessionManager

class SyncRepository(
    private val db: AppDatabase,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun syncAll() {
        try {
            AppLogger.d("Starting full sync...")
            val myId = sessionManager.getMyId()
            if (myId == -1) return

            val unsyncedTasks = db.taskDao().getUnsyncedTasks().map {
                SyncTaskDto(
                    id = it.remoteId,
                    title = it.title,
                    description = it.description,
                    isCompleted = it.isCompleted,
                    isDeleted = it.isDeleted,
                    updatedAt = it.updatedAt,
                    startExecutionAt = it.startExecutionAt,
                    deadline = it.deadline,
                    assignedUserId = it.assignedUserId,
                    departmentId = it.departmentId
                )
            }

            val unsyncedAttendances = db.attendanceDao().getUnsynced().map {
                SyncAttendanceDto(
                    id = it.remoteId,
                    date = it.date,
                    actionType = it.actionType,
                    updatedAt = it.updatedAt,
                    isDeleted = it.isDeleted
                )
            }

            val unsyncedSan = db.sanDao().getUnsynced().map {
                SyncSanDto(
                    id = it.remoteId,
                    date = it.date,
                    scoreS = it.scoreS,
                    scoreA = it.scoreA,
                    scoreN = it.scoreN,
                    updatedAt = it.updatedAt,
                    isDeleted = it.isDeleted
                )
            }

            val unsyncedMaslach = db.maslachDao().getUnsynced().map {
                SyncMaslachDto(
                    id = it.remoteId,
                    date = it.date,
                    emotionalExhaustion = it.emotionalExhaustion,
                    depersonalization = it.depersonalization,
                    personalAccomplishment = it.personalAccomplishment,
                    updatedAt = it.updatedAt,
                    isDeleted = it.isDeleted
                )
            }

            val unsyncedMunsterberg = db.munsterbergDao().getUnsynced().map {
                SyncMunsterbergDto(
                    id = it.remoteId,
                    date = it.date,
                    correctWords = it.correctWords,
                    timeSpentSeconds = it.timeSpentSeconds,
                    errors = it.errors,
                    updatedAt = it.updatedAt,
                    isDeleted = it.isDeleted
                )
            }

            val request = SyncRequest(
                tasks = unsyncedTasks,
                attendances = unsyncedAttendances,
                sanResults = unsyncedSan,
                maslachResults = unsyncedMaslach,
                munsterbergResults = unsyncedMunsterberg,
                lastSyncAt = sessionManager.lastSyncAt
            )

            val response = apiService.syncData(request)

            // Save tasks
            for (taskDto in response.tasks) {
                val existing = db.taskDao().getTaskByRemoteId(taskDto.id)
                if (existing != null) {
                    if (taskDto.isDeleted) {
                        db.taskDao().removeTask(existing.localId)
                    } else {
                        db.taskDao().updateTask(existing.copy(
                            title = taskDto.title,
                            description = taskDto.description,
                            isCompleted = taskDto.isCompleted,
                            isDeleted = false,
                            updatedAt = taskDto.updatedAt,
                            startExecutionAt = taskDto.startExecutionAt,
                            deadline = taskDto.deadline,
                            assignedUserId = taskDto.assignedUserId,
                            departmentId = taskDto.departmentId,
                            isSynced = true
                        ))
                    }
                } else if (!taskDto.isDeleted) {
                    db.taskDao().insertTask(
                        ru.moonlited.pocketmanager.data.local.entity.TaskEntity(
                            remoteId = taskDto.id,
                            userId = taskDto.userId,
                            title = taskDto.title,
                            description = taskDto.description,
                            isCompleted = taskDto.isCompleted,
                            isDeleted = false,
                            createdAt = taskDto.createdAt,
                            updatedAt = taskDto.updatedAt,
                            startExecutionAt = taskDto.startExecutionAt,
                            deadline = taskDto.deadline,
                            assignedUserId = taskDto.assignedUserId,
                            departmentId = taskDto.departmentId,
                            isSynced = true
                        )
                    )
                }
            }

            // Save Attendances
            for (attDto in response.attendances) {
                val existing = db.attendanceDao().getByRemoteId(attDto.id)
                if (existing != null) {
                    if (attDto.isDeleted) {
                        db.attendanceDao().remove(existing.localId)
                    } else {
                        db.attendanceDao().update(existing.copy(
                            date = attDto.date,
                            actionType = attDto.actionType,
                            updatedAt = attDto.updatedAt,
                            isSynced = true
                        ))
                    }
                } else if (!attDto.isDeleted) {
                    db.attendanceDao().insert(AttendanceEntity(
                        remoteId = attDto.id,
                        userId = attDto.userId,
                        date = attDto.date,
                        actionType = attDto.actionType,
                        updatedAt = attDto.updatedAt,
                        isSynced = true
                    ))
                }
            }

            // Save San
            for (sanDto in response.sanResults) {
                val existing = db.sanDao().getByRemoteId(sanDto.id)
                if (existing != null) {
                    if (sanDto.isDeleted) {
                        db.sanDao().remove(existing.localId)
                    } else {
                        db.sanDao().update(existing.copy(
                            date = sanDto.date,
                            scoreS = sanDto.scoreS,
                            scoreA = sanDto.scoreA,
                            scoreN = sanDto.scoreN,
                            updatedAt = sanDto.updatedAt,
                            isSynced = true
                        ))
                    }
                } else if (!sanDto.isDeleted) {
                    db.sanDao().insert(SanResultEntity(
                        remoteId = sanDto.id,
                        userId = myId,
                        date = sanDto.date,
                        scoreS = sanDto.scoreS,
                        scoreA = sanDto.scoreA,
                        scoreN = sanDto.scoreN,
                        updatedAt = sanDto.updatedAt,
                        isSynced = true
                    ))
                }
            }

            // Save Maslach
            for (maslachDto in response.maslachResults) {
                val existing = db.maslachDao().getByRemoteId(maslachDto.id)
                if (existing != null) {
                    if (maslachDto.isDeleted) {
                        db.maslachDao().remove(existing.localId)
                    } else {
                        db.maslachDao().update(existing.copy(
                            date = maslachDto.date,
                            emotionalExhaustion = maslachDto.emotionalExhaustion,
                            depersonalization = maslachDto.depersonalization,
                            personalAccomplishment = maslachDto.personalAccomplishment,
                            updatedAt = maslachDto.updatedAt,
                            isSynced = true
                        ))
                    }
                } else if (!maslachDto.isDeleted) {
                    db.maslachDao().insert(MaslachResultEntity(
                        remoteId = maslachDto.id,
                        userId = myId,
                        date = maslachDto.date,
                        emotionalExhaustion = maslachDto.emotionalExhaustion,
                        depersonalization = maslachDto.depersonalization,
                        personalAccomplishment = maslachDto.personalAccomplishment,
                        updatedAt = maslachDto.updatedAt,
                        isSynced = true
                    ))
                }
            }

            // Save Munsterberg
            for (munsterbergDto in response.munsterbergResults) {
                val existing = db.munsterbergDao().getByRemoteId(munsterbergDto.id)
                if (existing != null) {
                    if (munsterbergDto.isDeleted) {
                        db.munsterbergDao().remove(existing.localId)
                    } else {
                        db.munsterbergDao().update(existing.copy(
                            date = munsterbergDto.date,
                            correctWords = munsterbergDto.correctWords,
                            timeSpentSeconds = munsterbergDto.timeSpentSeconds,
                            errors = munsterbergDto.errors,
                            updatedAt = munsterbergDto.updatedAt,
                            isSynced = true
                        ))
                    }
                } else if (!munsterbergDto.isDeleted) {
                    db.munsterbergDao().insert(MunsterbergResultEntity(
                        remoteId = munsterbergDto.id,
                        userId = myId,
                        date = munsterbergDto.date,
                        correctWords = munsterbergDto.correctWords,
                        timeSpentSeconds = munsterbergDto.timeSpentSeconds,
                        errors = munsterbergDto.errors,
                        updatedAt = munsterbergDto.updatedAt,
                        isSynced = true
                    ))
                }
            }

            // Update session sync date
            sessionManager.lastSyncAt = response.currentSyncAt
            AppLogger.d("Full sync completed successfully. Last sync at: ${response.currentSyncAt}")

        } catch (e: Exception) {
            AppLogger.e("Failed to perform full sync", e)
        }
    }
}
