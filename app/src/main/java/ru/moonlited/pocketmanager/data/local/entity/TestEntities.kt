package ru.moonlited.pocketmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "san_test_results")
data class SanResultEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val date: String,
    val scoreS: Float,
    val scoreA: Float,
    val scoreN: Float,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: String
)

@Entity(tableName = "maslach_results")
data class MaslachResultEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val date: String,
    val emotionalExhaustion: Float,
    val depersonalization: Float,
    val personalAccomplishment: Float,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: String
)

@Entity(tableName = "munsterberg_results")
data class MunsterbergResultEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val date: String,
    val correctWords: Int,
    val timeSpentSeconds: Int,
    val errors: Int,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: String
)
