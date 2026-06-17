package ru.moonlited.pocketmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.moonlited.pocketmanager.data.local.dao.TaskDao
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity
import ru.moonlited.pocketmanager.data.local.entity.AttendanceEntity
import ru.moonlited.pocketmanager.data.local.entity.SanResultEntity
import ru.moonlited.pocketmanager.data.local.entity.MaslachResultEntity
import ru.moonlited.pocketmanager.data.local.entity.MunsterbergResultEntity

@Database(
    entities = [
        TaskEntity::class,
        AttendanceEntity::class,
        SanResultEntity::class,
        MaslachResultEntity::class,
        MunsterbergResultEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun attendanceDao(): ru.moonlited.pocketmanager.data.local.dao.AttendanceDao
    abstract fun sanDao(): ru.moonlited.pocketmanager.data.local.dao.SanDao
    abstract fun maslachDao(): ru.moonlited.pocketmanager.data.local.dao.MaslachDao
    abstract fun munsterbergDao(): ru.moonlited.pocketmanager.data.local.dao.MunsterbergDao
}