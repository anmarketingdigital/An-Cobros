package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_leads")
data class ClientLead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val company: String,
    val phone: String,
    val email: String,
    val service: String,
    val monthlyFee: Double,
    val billingDay: Int,
    val isLead: Boolean = false,
    val leadStatus: String = "Nuevo", // "Nuevo", "En Contacto", "Propuesta", "No Interesado", "Convertido"
    val notes: String = "",
    val isPaidThisMonth: Boolean = false,
    val lastReportDate: Long = 0L,
    val lastReportContent: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
