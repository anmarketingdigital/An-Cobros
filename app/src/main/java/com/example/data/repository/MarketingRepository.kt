package com.example.data.repository

import com.example.data.database.ClientLeadDao
import com.example.data.database.MeetingDao
import com.example.data.model.ClientLead
import com.example.data.model.Meeting
import kotlinx.coroutines.flow.Flow

class MarketingRepository(
    private val clientLeadDao: ClientLeadDao,
    private val meetingDao: MeetingDao
) {
    val allClientLeads: Flow<List<ClientLead>> = clientLeadDao.getAll()
    val leads: Flow<List<ClientLead>> = clientLeadDao.getLeads()
    val clients: Flow<List<ClientLead>> = clientLeadDao.getClients()
    val upcomingMeetings: Flow<List<Meeting>> = meetingDao.getUpcoming()
    val allMeetings: Flow<List<Meeting>> = meetingDao.getAll()

    suspend fun getClientLeadById(id: Int): ClientLead? {
        return clientLeadDao.getById(id)
    }

    suspend fun insertClientLead(clientLead: ClientLead): Long {
        return clientLeadDao.insert(clientLead)
    }

    suspend fun updateClientLead(clientLead: ClientLead) {
        clientLeadDao.update(clientLead)
    }

    suspend fun deleteClientLead(clientLead: ClientLead) {
        clientLeadDao.delete(clientLead)
    }

    suspend fun updatePaymentStatus(id: Int, isPaid: Boolean) {
        clientLeadDao.updatePaymentStatus(id, isPaid)
    }

    suspend fun insertMeeting(meeting: Meeting): Long {
        return meetingDao.insert(meeting)
    }

    suspend fun updateMeeting(meeting: Meeting) {
        meetingDao.update(meeting)
    }

    suspend fun deleteMeeting(meeting: Meeting) {
        meetingDao.delete(meeting)
    }

    suspend fun updateMeetingCompletedStatus(id: Int, isCompleted: Boolean) {
        meetingDao.updateCompletedStatus(id, isCompleted)
    }
}
