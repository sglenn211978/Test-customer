package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM session_records ORDER BY timestampMs DESC")
    fun getAllSessions(): Flow<List<SessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(record: SessionRecord): Long

    @Query("SELECT * FROM note_mastery")
    fun getAllNoteMastery(): Flow<List<NoteMastery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMastery(mastery: NoteMastery)

    @Query("SELECT * FROM note_mastery WHERE noteKey = :key")
    suspend fun getMasteryForKey(key: String): NoteMastery?

    @Query("DELETE FROM session_records")
    suspend fun clearHistory()
}
