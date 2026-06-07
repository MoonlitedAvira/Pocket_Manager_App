// data/api/Models.kt
package ru.moonlited.pocketmanager.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String
)


@Serializable
data class UserCreateRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val role: String,
    @SerialName("company_id") val companyId: Int? = null,
    @SerialName("department_id") val departmentId: Int? = null,
    @SerialName("position_id") val positionId: Int? = null,
    @SerialName("company_name") val companyName: String? = null
)

@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    @SerialName("start_execution_at") val startExecutionAt: String? = null,
    val deadline: String? = null,
    @SerialName("assigned_user_id") val assignedUserId: Int? = null,
    @SerialName("department_id") val departmentId: Int? = null
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

@Serializable
data class PomodoroCreate(
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("duration_minutes") val durationMinutes: Int
)

@Serializable
data class SanTestCreate(
    @SerialName("score_s") val scoreS: Float,
    @SerialName("score_a") val scoreA: Float,
    @SerialName("score_n") val scoreN: Float
)

@Serializable
data class SanTestResponse(
    val id: Int,
    val date: String,
    @SerialName("score_s") val scoreS: Float,
    @SerialName("score_a") val scoreA: Float,
    @SerialName("score_n") val scoreN: Float
)
@Serializable
data class SyncTaskDto(
    val id: Int? = null,
    val title: String,
    val description: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("start_execution_at") val startExecutionAt: String? = null,
    val deadline: String? = null,
    @SerialName("assigned_user_id") val assignedUserId: Int? = null,
    @SerialName("department_id") val departmentId: Int? = null
)

@Serializable
data class SyncRequest(
    val tasks: List<SyncTaskDto>,
    @SerialName("last_sync_at") val lastSyncAt: String? = null
)

@Serializable
data class SyncResponse(
    @SerialName("current_sync_at") val currentSyncAt: String,
    val tasks: List<TaskResponse>
)

@Serializable
data class FCMTokenUpdate(
    @SerialName("fcm_token") val fcmToken: String
)

@Serializable
data class DepartmentResponse(
    val id: Int,
    val name: String,
    @SerialName("company_id") val companyId: Int
)

@Serializable
data class CompanyCreateRequest(
    val name: String
)

@Serializable
data class CompanyResponse(
    val id: Int,
    val name: String,
    @SerialName("owner_id") val ownerId: Int
)

@Serializable
data class JoinCompanyRequest(
    val code: String
)

@Serializable
data class MaslachCreate(
    @SerialName("emotional_exhaustion") val emotionalExhaustion: Float,
    val depersonalization: Float,
    @SerialName("personal_accomplishment") val personalAccomplishment: Float
)

@Serializable
data class MaslachResponse(
    val id: Int,
    val date: String,
    @SerialName("emotional_exhaustion") val emotionalExhaustion: Float,
    val depersonalization: Float,
    @SerialName("personal_accomplishment") val personalAccomplishment: Float
)

@Serializable
data class MunsterbergCreate(
    @SerialName("correct_words") val correctWords: Int,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int
)

@Serializable
data class MunsterbergResponse(
    val id: Int,
    val date: String,
    @SerialName("correct_words") val correctWords: Int,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int
)

@Serializable
data class InvitationCreate(
    @SerialName("department_id") val departmentId: Int? = null,
    @SerialName("position_id") val positionId: Int? = null
)

@Serializable
data class InvitationResponse(
    val id: Int,
    val code: String,
    @SerialName("company_id") val companyId: Int,
    @SerialName("department_id") val departmentId: Int? = null,
    @SerialName("position_id") val positionId: Int? = null,
    @SerialName("is_used") val isUsed: Boolean
)

@Serializable
data class AttendanceCreate(
    @SerialName("action_type") val actionType: String
)

@Serializable
data class AttendanceResponse(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    val date: String,
    @SerialName("action_type") val actionType: String
)