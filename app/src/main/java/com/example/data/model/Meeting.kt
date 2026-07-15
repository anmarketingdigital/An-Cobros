package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientLeadId: Int? = null,
    val clientName: String,
    val title: String,
    val description: String = "",
    val dateMillis: Long,
    val timeString: String, // e.g. "14:30"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
