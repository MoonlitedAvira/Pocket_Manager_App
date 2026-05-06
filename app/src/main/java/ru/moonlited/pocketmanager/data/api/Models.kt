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
    val role: String
)

@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null
)

@Serializable
data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("created_at") val createdAt: String
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