// data/api/Models.kt
package ru.moonlited.pocketmanager.data.api

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String
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
    val is_completed: Boolean,
    val created_at: String
)

@Serializable
data class PomodoroCreate(
    val start_time: String,
    val end_time: String,
    val duration_minutes: Int
)

@Serializable
data class SanTestCreate(
    val score_s: Float,
    val score_a: Float,
    val score_n: Float
)