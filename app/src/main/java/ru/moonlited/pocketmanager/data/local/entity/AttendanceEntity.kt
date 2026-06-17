package ru.moonlited.pocketmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val date: String,
    val actionType: String, // e.g., "check_in", "check_out"
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: String
)
