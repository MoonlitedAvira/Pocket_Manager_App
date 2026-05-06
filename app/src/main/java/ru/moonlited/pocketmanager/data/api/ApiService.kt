// data/api/ApiService.kt
package ru.moonlited.pocketmanager.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): TokenResponse

    @POST("/auth/register")
    suspend fun register(@Body request: UserCreateRequest): UserResponse

    @GET("/tasks")
    suspend fun getTasks(): List<TaskResponse>

    @POST("/tasks")
    suspend fun createTask(@Body task: TaskCreateRequest): TaskResponse

    @PUT("/tasks/{task_id}/complete")
    suspend fun completeTask(@Path("task_id") taskId: Int): TaskResponse

    @PUT("/tasks/{task_id}")
    suspend fun updateTask(@Path("task_id") taskId: Int, @Body task: TaskCreateRequest): TaskResponse

    @DELETE("/tasks/{task_id}")
    suspend fun deleteTask(@Path("task_id") taskId: Int)

    @POST("/pomodoro")
    suspend fun savePomodoro(@Body session: PomodoroCreate)

    @POST("/san-test")
    suspend fun saveSanTest(@Body testData: SanTestCreate)
}