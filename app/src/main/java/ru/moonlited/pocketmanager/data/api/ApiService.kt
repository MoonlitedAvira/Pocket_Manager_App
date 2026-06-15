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

    @GET("/tasks/delegated")
    suspend fun getDelegatedTasks(): List<TaskResponse>

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

    @GET("/san-test")
    suspend fun getSanResults(): List<SanTestResponse>

    @POST("/sync")
    suspend fun syncData(@Body request: SyncRequest): SyncResponse

    @PUT("/users/fcm-token")
    suspend fun updateFcmToken(@Body tokenData: FCMTokenUpdate)

    @GET("/departments")
    suspend fun getDepartments(): List<DepartmentResponse>

    @GET("/users")
    suspend fun getUsers(): List<UserResponse>

    @POST("/companies")
    suspend fun createCompany(@Body request: CompanyCreateRequest): CompanyResponse

    @POST("/companies/join")
    suspend fun joinCompany(@Body request: JoinCompanyRequest)

    @POST("/users/recover")
    suspend fun recoverAccount(@Body request: UserCreateRequest): TokenResponse

    @DELETE("/users/me")
    suspend fun deleteAccount()


    @GET("/users/me")
    suspend fun getMe(): UserResponse

    @POST("/users/attendance")
    suspend fun checkIn(@Body request: AttendanceCreate): AttendanceResponse

    @GET("/users/attendance")
    suspend fun getAttendance(): List<AttendanceResponse>

    @POST("/maslach-test")
    suspend fun saveMaslachTest(@Body testData: MaslachCreate): MaslachResponse

    @GET("/maslach-test")
    suspend fun getMaslachResults(): List<MaslachResponse>

    @POST("/munsterberg-test")
    suspend fun saveMunsterbergTest(@Body testData: MunsterbergCreate): MunsterbergResponse

    @GET("/munsterberg-test")
    suspend fun getMunsterbergResults(): List<MunsterbergResponse>

    @POST("/companies/invitations")
    suspend fun createInvitation(@Body request: InvitationCreate): InvitationResponse

    @DELETE("/companies/invitations/{code}")
    suspend fun deleteInvitation(@Path("code") code: String)

    @POST("/departments")
    suspend fun createDepartment(@Body request: DepartmentCreateRequest): DepartmentResponse

    @POST("/positions")
    suspend fun createPosition(@Body request: PositionCreateRequest): PositionResponse

    @PUT("/positions/{pos_id}")
    suspend fun updatePosition(@Path("pos_id") posId: Int, @Body request: PositionUpdateRequest): PositionResponse

    @PUT("/users/{user_id}")
    suspend fun updateUser(@Path("user_id") userId: Int, @Body request: WorkerUpdateRequest): UserResponse

    @DELETE("/users/{user_id}")
    suspend fun deleteUserFromCompany(@Path("user_id") userId: Int)

    @GET("/users/{user_id}/stats")
    suspend fun getUserStats(@Path("user_id") userId: Int): WorkerStatsResponse
}