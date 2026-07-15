package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Meeting
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY dateMillis ASC, timeString ASC")
    fun getAll(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE isCompleted = 0 ORDER BY dateMillis ASC, timeString ASC")
    fun getUpcoming(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY timeString ASC")
    fun getMeetingsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Meeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: Meeting): Long

    @Update
    suspend fun update(meeting: Meeting)

    @Delete
    suspend fun delete(meeting: Meeting)

    @Query("UPDATE meetings SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletedStatus(id: Int, isCompleted: Boolean)
}
