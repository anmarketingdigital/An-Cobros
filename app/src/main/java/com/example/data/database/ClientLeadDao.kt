package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientLead
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientLeadDao {
    @Query("SELECT * FROM client_leads ORDER BY name ASC")
    fun getAll(): Flow<List<ClientLead>>

    @Query("SELECT * FROM client_leads WHERE isLead = 1 ORDER BY createdAt DESC")
    fun getLeads(): Flow<List<ClientLead>>

    @Query("SELECT * FROM client_leads WHERE isLead = 0 ORDER BY name ASC")
    fun getClients(): Flow<List<ClientLead>>

    @Query("SELECT * FROM client_leads WHERE id = :id")
    suspend fun getById(id: Int): ClientLead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clientLead: ClientLead): Long

    @Update
    suspend fun update(clientLead: ClientLead)

    @Delete
    suspend fun delete(clientLead: ClientLead)

    @Query("UPDATE client_leads SET isPaidThisMonth = :isPaid WHERE id = :id")
    suspend fun updatePaymentStatus(id: Int, isPaid: Boolean)
}
