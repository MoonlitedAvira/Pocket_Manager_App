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
    @SerialName("score_n") val scoreN: Float,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
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
data class SyncAttendanceDto(
    val id: Int? = null,
    val date: String,
    @SerialName("action_type") val actionType: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class SyncSanDto(
    val id: Int? = null,
    val date: String,
    @SerialName("score_s") val scoreS: Float,
    @SerialName("score_a") val scoreA: Float,
    @SerialName("score_n") val scoreN: Float,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class SyncMaslachDto(
    val id: Int? = null,
    val date: String,
    @SerialName("emotional_exhaustion") val emotionalExhaustion: Float,
    val depersonalization: Float,
    @SerialName("personal_accomplishment") val personalAccomplishment: Float,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class SyncMunsterbergDto(
    val id: Int? = null,
    val date: String,
    @SerialName("correct_words") val correctWords: Int,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int,
    val errors: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class SyncRequest(
    val tasks: List<SyncTaskDto> = emptyList(),
    val attendances: List<SyncAttendanceDto> = emptyList(),
    @SerialName("san_results") val sanResults: List<SyncSanDto> = emptyList(),
    @SerialName("maslach_results") val maslachResults: List<SyncMaslachDto> = emptyList(),
    @SerialName("munsterberg_results") val munsterbergResults: List<SyncMunsterbergDto> = emptyList(),
    @SerialName("last_sync_at") val lastSyncAt: String? = null
)

@Serializable
data class SyncResponse(
    @SerialName("current_sync_at") val currentSyncAt: String,
    val tasks: List<TaskResponse> = emptyList(),
    val attendances: List<AttendanceResponse> = emptyList(),
    @SerialName("san_results") val sanResults: List<SanTestResponse> = emptyList(),
    @SerialName("maslach_results") val maslachResults: List<MaslachResponse> = emptyList(),
    @SerialName("munsterberg_results") val munsterbergResults: List<MunsterbergResponse> = emptyList()
)

@Serializable
data class FCMTokenUpdate(
    @SerialName("fcm_token") val fcmToken: String
)

@Serializable
data class PositionResponse(
    val id: Int,
    @SerialName("department_id") val departmentId: Int,
    val name: String,
    @SerialName("hierarchy_level") val hierarchyLevel: Int,
    val permissions: String? = null,
    @SerialName("schedule_type") val scheduleType: String = "none",
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("schedule_start") val scheduleStart: String? = null,
    @SerialName("schedule_end") val scheduleEnd: String? = null,
    @SerialName("schedule_norm_minutes") val scheduleNormMinutes: Int? = null
)

@Serializable
data class PositionCreateRequest(
    val name: String,
    @SerialName("department_id") val departmentId: Int,
    @SerialName("hierarchy_level") val hierarchyLevel: Int,
    val permissions: String? = null,
    @SerialName("schedule_type") val scheduleType: String? = "none",
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("schedule_start") val scheduleStart: String? = null,
    @SerialName("schedule_end") val scheduleEnd: String? = null,
    @SerialName("schedule_norm_minutes") val scheduleNormMinutes: Int? = null
)

@Serializable
data class PositionUpdateRequest(
    val name: String? = null,
    @SerialName("hierarchy_level") val hierarchyLevel: Int? = null,
    val permissions: String? = null,
    @SerialName("schedule_type") val scheduleType: String? = null,
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("schedule_start") val scheduleStart: String? = null,
    @SerialName("schedule_end") val scheduleEnd: String? = null,
    @SerialName("schedule_norm_minutes") val scheduleNormMinutes: Int? = null
)

@Serializable
data class DepartmentCreateRequest(
    val name: String,
    @SerialName("company_id") val companyId: Int
)

@Serializable
data class DepartmentResponse(
    val id: Int,
    val name: String,
    @SerialName("company_id") val companyId: Int,
    val positions: List<PositionResponse> = emptyList()
)

@Serializable
data class WorkerUpdateRequest(
    @SerialName("department_id") val departmentId: Int? = null,
    @SerialName("position_id") val positionId: Int? = null,
    val role: String? = null
)

@Serializable
data class WorkerStatsResponse(
    @SerialName("user_id") val userId: Int,
    @SerialName("san_results") val sanResults: List<SanTestResponse> = emptyList(),
    @SerialName("maslach_results") val maslachResults: List<MaslachResponse> = emptyList(),
    @SerialName("munsterberg_results") val munsterbergResults: List<MunsterbergResponse> = emptyList()
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
    @SerialName("personal_accomplishment") val personalAccomplishment: Float,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class MunsterbergCreate(
    @SerialName("correct_words") val correctWords: Int,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int,
    val errors: Int = 0
)

@Serializable
data class MunsterbergResponse(
    val id: Int,
    val date: String,
    @SerialName("correct_words") val correctWords: Int,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int,
    val errors: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
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
    @SerialName("action_type") val actionType: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)