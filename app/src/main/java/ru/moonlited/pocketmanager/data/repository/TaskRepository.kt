package ru.moonlited.pocketmanager.data.repository

import kotlinx.coroutines.flow.Flow
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.DepartmentResponse
import ru.moonlited.pocketmanager.data.api.SyncRequest
import ru.moonlited.pocketmanager.data.api.SyncTaskDto
import ru.moonlited.pocketmanager.data.api.UserResponse
import ru.moonlited.pocketmanager.data.local.dao.TaskDao
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity
import ru.moonlited.pocketmanager.utils.SessionManager
import java.time.Instant

class TaskRepository(
    private val taskDao: TaskDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    fun getTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasksFlow()
    }

    suspend fun createTask(
        title: String,
        description: String?,
        startExecutionAt: String? = null,
        deadline: String? = null,
        assignedUserId: Int? = null,
        departmentId: Int? = null
    ) {
        val curTime = System.currentTimeMillis().toString()
        val myId = sessionManager.getMyId()
        val newTask = TaskEntity(
            userId = myId,
            title = title,
            description = description,
            isCompleted = false,
            createdAt = curTime,
            updatedAt = curTime,
            startExecutionAt = startExecutionAt,
            deadline = deadline,
            assignedUserId = assignedUserId,
            departmentId = departmentId
        )
        taskDao.insertTask(newTask)

        // TODO: Здесь будет логика отправки на сервер и обновления флага isSynced
    }

    private fun getNextUpdatedAt(currentUpdatedAt: String): String {
        val now = java.time.Instant.now()
        val current = try {
            java.time.Instant.parse(currentUpdatedAt)
        } catch (e: Exception) {
            java.time.Instant.MIN
        }
        return if (now.isAfter(current)) {
            now.toString()
        } else {
            current.plusMillis(1).toString()
        }
    }

    suspend fun completeTask(localId: Int) {
        val task = taskDao.getTaskById(localId) ?: return
        val updatedTask = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = getNextUpdatedAt(task.updatedAt),
            isSynced = false
        )
        taskDao.updateTask(updatedTask)
    }

    suspend fun editTask(localId: Int, newTitle: String, newDescription: String?, newStartExecutionAt: String?, newDeadline: String?) {
        val task = taskDao.getTaskById(localId) ?: return
        val updatedTask = task.copy(
            title = newTitle,
            description = newDescription,
            startExecutionAt = newStartExecutionAt,
            deadline = newDeadline,
            updatedAt = getNextUpdatedAt(task.updatedAt),
            isSynced = false
        )
        taskDao.updateTask(updatedTask)
    }

    suspend fun deleteTask(localId: Int) {
        val task = taskDao.getTaskById(localId)
        if (task == null) {
            try {
                apiService.deleteTask(localId)
            } catch (e: Exception) {
                ru.moonlited.pocketmanager.utils.AppLogger.e("Failed to delete delegated task", e)
            }
            return
        }
        val updatedTask = task.copy(
            isDeleted = true,
            updatedAt = getNextUpdatedAt(task.updatedAt),
            isSynced = false
        )
        taskDao.updateTask(updatedTask)
    }

    suspend fun syncTasks() {
        try {
            ru.moonlited.pocketmanager.utils.AppLogger.d("Starting syncTasks...")
            val unsyncedLocalTasks = taskDao.getUnsyncedTasks()
            
            // 1. Post newly created tasks directly to trigger push notifications and get IDs immediately
            val newTasks = unsyncedLocalTasks.filter { it.remoteId == null }
            for (task in newTasks) {
                if (task.isDeleted) {
                    taskDao.removeTask(task.localId)
                    continue
                }
                try {
                    ru.moonlited.pocketmanager.utils.AppLogger.d("Posting new task directly: ${task.title}")
                    val createReq = ru.moonlited.pocketmanager.data.api.TaskCreateRequest(
                        title = task.title,
                        description = task.description,
                        startExecutionAt = task.startExecutionAt,
                        deadline = task.deadline,
                        assignedUserId = task.assignedUserId,
                        departmentId = task.departmentId
                    )
                    val response = apiService.createTask(createReq)
                    val updatedTask = task.copy(remoteId = response.id, isSynced = true)
                    taskDao.updateTask(updatedTask)
                    ru.moonlited.pocketmanager.utils.AppLogger.d("Successfully posted task, remoteId: ${response.id}")
                } catch (e: Exception) {
                    ru.moonlited.pocketmanager.utils.AppLogger.e("Failed to post new task: ${task.title}", e)
                }
            }

            // Fetch the remaining unsynced tasks (updates to existing tasks)
            val updatesToSync = taskDao.getUnsyncedTasks().filter { it.remoteId != null }
            
            val syncDtos = updatesToSync.map { entity ->
                SyncTaskDto(
                    id = entity.remoteId,
                    title = entity.title,
                    description = entity.description,
                    isCompleted = entity.isCompleted,
                    isDeleted = entity.isDeleted,
                    updatedAt = entity.updatedAt,
                    startExecutionAt = entity.startExecutionAt,
                    deadline = entity.deadline,
                    assignedUserId = entity.assignedUserId,
                    departmentId = entity.departmentId
                )
            }

            val lastSync = sessionManager.getLastSyncTime()
            val request = SyncRequest(tasks = syncDtos, lastSyncAt = lastSync)
            
            ru.moonlited.pocketmanager.utils.AppLogger.d("Sending bulk sync request with ${syncDtos.size} updates.")

            val response = apiService.syncData(request)
            ru.moonlited.pocketmanager.utils.AppLogger.d("Received sync response with ${response.tasks.size} tasks updated from server.")

            response.tasks.forEach { serverTask ->
                val existingTask = taskDao.getTaskByRemoteId(serverTask.id)

                if (existingTask != null) {
                    val updatedTask = existingTask.copy(
                        userId = serverTask.userId,
                        title = serverTask.title,
                        description = serverTask.description,
                        isCompleted = serverTask.isCompleted,
                        isDeleted = serverTask.isDeleted,
                        updatedAt = Instant.now().toString(),
                        isSynced = true,
                        startExecutionAt = serverTask.startExecutionAt,
                        deadline = serverTask.deadline,
                        assignedUserId = serverTask.assignedUserId,
                        departmentId = serverTask.departmentId
                    )
                    taskDao.updateTask(updatedTask)
                } else {
                    val newEntity = TaskEntity(
                        remoteId = serverTask.id,
                        userId = serverTask.userId,
                        title = serverTask.title,
                        description = serverTask.description,
                        isCompleted = serverTask.isCompleted,
                        isDeleted = serverTask.isDeleted,
                        createdAt = serverTask.createdAt,
                        updatedAt = Instant.now().toString(),
                        isSynced = true,
                        startExecutionAt = serverTask.startExecutionAt,
                        deadline = serverTask.deadline,
                        assignedUserId = serverTask.assignedUserId,
                        departmentId = serverTask.departmentId
                    )
                    taskDao.insertTask(newEntity)
                }
            }
            sessionManager.saveLastSyncTime(response.currentSyncAt)
            ru.moonlited.pocketmanager.utils.AppLogger.d("syncTasks completed successfully.")
        } catch (e: Exception) {
            ru.moonlited.pocketmanager.utils.AppLogger.e("Exception during syncTasks", e)
            e.printStackTrace()
        }
    }

    suspend fun getDepartments(): List<DepartmentResponse> {
        return try {
            apiService.getDepartments()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsers(): List<UserResponse> {
        return try {
            apiService.getUsers()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDelegatedTasks(): List<ru.moonlited.pocketmanager.data.api.TaskResponse> {
        return try {
            apiService.getDelegatedTasks()
        } catch (e: Exception) {
            emptyList()
        }
    }
}