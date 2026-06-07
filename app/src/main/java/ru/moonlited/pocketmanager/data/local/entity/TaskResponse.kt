package ru.moonlited.pocketmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val title: String,
    val description: String?,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false,
    val startExecutionAt: String? = null,
    val deadline: String? = null,
    val assignedUserId: Int? = null,
    val departmentId: Int? = null
)

@Serializable
data class TaskResponse(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("start_execution_at") val startExecutionAt: String? = null,
    val deadline: String? = null,
    @SerialName("assigned_user_id") val assignedUserId: Int? = null,
    @SerialName("department_id") val departmentId: Int? = null
)