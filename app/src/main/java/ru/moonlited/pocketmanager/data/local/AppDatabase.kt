package ru.moonlited.pocketmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.moonlited.pocketmanager.data.local.dao.TaskDao
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}