package ru.moonlited.pocketmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.moonlited.pocketmanager.data.local.entity.AttendanceEntity
import ru.moonlited.pocketmanager.data.local.entity.SanResultEntity
import ru.moonlited.pocketmanager.data.local.entity.MaslachResultEntity
import ru.moonlited.pocketmanager.data.local.entity.MunsterbergResultEntity

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY date DESC")
    fun getAllFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE isSynced = 0")
    suspend fun getUnsynced(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttendanceEntity): Long

    @Update
    suspend fun update(entity: AttendanceEntity)

    @Query("SELECT * FROM attendances WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): AttendanceEntity?
    
    @Query("DELETE FROM attendances WHERE localId = :id")
    suspend fun remove(id: Int)
}

@Dao
interface SanDao {
    @Query("SELECT * FROM san_test_results ORDER BY date DESC")
    fun getAllFlow(): Flow<List<SanResultEntity>>

    @Query("SELECT * FROM san_test_results WHERE isSynced = 0")
    suspend fun getUnsynced(): List<SanResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SanResultEntity): Long

    @Update
    suspend fun update(entity: SanResultEntity)

    @Query("SELECT * FROM san_test_results WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): SanResultEntity?
    
    @Query("DELETE FROM san_test_results WHERE localId = :id")
    suspend fun remove(id: Int)
}

@Dao
interface MaslachDao {
    @Query("SELECT * FROM maslach_results ORDER BY date DESC")
    fun getAllFlow(): Flow<List<MaslachResultEntity>>

    @Query("SELECT * FROM maslach_results WHERE isSynced = 0")
    suspend fun getUnsynced(): List<MaslachResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MaslachResultEntity): Long

    @Update
    suspend fun update(entity: MaslachResultEntity)

    @Query("SELECT * FROM maslach_results WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): MaslachResultEntity?
    
    @Query("DELETE FROM maslach_results WHERE localId = :id")
    suspend fun remove(id: Int)
}

@Dao
interface MunsterbergDao {
    @Query("SELECT * FROM munsterberg_results ORDER BY date DESC")
    fun getAllFlow(): Flow<List<MunsterbergResultEntity>>

    @Query("SELECT * FROM munsterberg_results WHERE isSynced = 0")
    suspend fun getUnsynced(): List<MunsterbergResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MunsterbergResultEntity): Long

    @Update
    suspend fun update(entity: MunsterbergResultEntity)

    @Query("SELECT * FROM munsterberg_results WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): MunsterbergResultEntity?
    
    @Query("DELETE FROM munsterberg_results WHERE localId = :id")
    suspend fun remove(id: Int)
}
