package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientLead
import com.example.data.model.Meeting
import com.example.data.network.GeminiClient
import com.example.data.repository.MarketingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketingViewModel(private val repository: MarketingRepository) : ViewModel() {

    val clientsList: StateFlow<List<ClientLead>> = repository.clients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leadsList: StateFlow<List<ClientLead>> = repository.leads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClientLeads: StateFlow<List<ClientLead>> = repository.allClientLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingMeetings: StateFlow<List<Meeting>> = repository.upcomingMeetings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeetings: StateFlow<List<Meeting>> = repository.allMeetings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for generating reports
    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    // Combined metric states
    val totalRevenue: StateFlow<Double> = clientsList
        .combine(MutableStateFlow(0.0)) { list, _ ->
            list.sumOf { it.monthlyFee }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingCollectCount: StateFlow<Int> = clientsList
        .combine(MutableStateFlow(0)) { list, _ ->
            list.count { !it.isPaidThisMonth }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Actions
    fun addClientLead(
        name: String,
        company: String,
        phone: String,
        email: String,
        service: String,
        monthlyFee: Double,
        billingDay: Int,
        isLead: Boolean,
        leadStatus: String = "Nuevo",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val clientLead = ClientLead(
                name = name,
                company = company,
                phone = phone,
                email = email,
                service = service,
                monthlyFee = monthlyFee,
                billingDay = billingDay,
                isLead = isLead,
                leadStatus = if (isLead) leadStatus else "Convertido",
                notes = notes
            )
            repository.insertClientLead(clientLead)
        }
    }

    fun updateClientLead(clientLead: ClientLead) {
        viewModelScope.launch {
            repository.updateClientLead(clientLead)
        }
    }

    fun deleteClientLead(clientLead: ClientLead) {
        viewModelScope.launch {
            repository.deleteClientLead(clientLead)
        }
    }

    fun convertLeadToClient(lead: ClientLead, monthlyFee: Double, billingDay: Int) {
        viewModelScope.launch {
            val updated = lead.copy(
                isLead = false,
                leadStatus = "Convertido",
                monthlyFee = monthlyFee,
                billingDay = billingDay
            )
            repository.updateClientLead(updated)
        }
    }

    fun togglePaymentStatus(clientId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updatePaymentStatus(clientId, !currentStatus)
        }
    }

    fun addMeeting(
        clientLeadId: Int?,
        clientName: String,
        title: String,
        description: String,
        dateMillis: Long,
        timeString: String
    ) {
        viewModelScope.launch {
            val meeting = Meeting(
                clientLeadId = clientLeadId,
                clientName = clientName,
                title = title,
                description = description,
                dateMillis = dateMillis,
                timeString = timeString
            )
            repository.insertMeeting(meeting)
        }
    }

    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }

    fun toggleMeetingCompleted(meetingId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateMeetingCompletedStatus(meetingId, !currentStatus)
        }
    }

    fun generatePerformanceReport(
        clientLead: ClientLead,
        impressions: Int,
        clicks: Int,
        conversions: Int,
        spent: Double,
        additionalNotes: String
    ) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            try {
                val report = GeminiClient.generatePerformanceReport(
                    clientName = clientLead.name,
                    service = clientLead.service,
                    impressions = impressions,
                    clicks = clicks,
                    conversions = conversions,
                    spent = spent,
                    additionalNotes = additionalNotes
                )
                
                // Update client lead in DB with the generated report content
                val updated = clientLead.copy(
                    lastReportDate = System.currentTimeMillis(),
                    lastReportContent = report
                )
                repository.updateClientLead(updated)
                
                _reportState.value = ReportState.Success(report)
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun resetReportState() {
        _reportState.value = ReportState.Idle
    }

    fun generateWhatsAppBillingMessage(client: ClientLead, tone: String): String {
        return GeminiClient.generateBillingReminder(
            clientName = client.name,
            service = client.service,
            fee = client.monthlyFee,
            billingDay = client.billingDay,
            tone = tone
        )
    }

    // Factory to instantiate the ViewModel with constructor injection
    class Factory(private val repository: MarketingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MarketingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MarketingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val reportText: String) : ReportState()
    data class Error(val error: String) : ReportState()
}
