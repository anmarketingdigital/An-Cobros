package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ClientLead
import com.example.data.model.Meeting
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketingViewModel
import com.example.ui.viewmodel.ReportState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MarketingViewModel) {
    var currentTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val tabs = listOf(
        TabItem("Resumen", Icons.Default.Dashboard, "dashboard"),
        TabItem("Clientes", Icons.Default.People, "clients"),
        TabItem("Cobranza", Icons.Default.AttachMoney, "billing"),
        TabItem("Reuniones", Icons.Default.Event, "meetings"),
        TabItem("Reportes IA", Icons.Default.Assessment, "reports")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_tab_${tab.tag}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(viewModel, onNavigateToTab = { currentTab = it })
                1 -> ClientsScreen(viewModel)
                2 -> BillingScreen(viewModel)
                3 -> MeetingsScreen(viewModel)
                4 -> ReportScreen(viewModel)
            }
        }
    }
}

data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tag: String)

// --- HELPER FUNCTION TO OPEN WHATSAPP ---
fun sendWhatsAppMessage(context: Context, phone: String, message: String) {
    try {
        // Clean phone number (keep only digits)
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        // Ensure country code is present (e.g., if starting with 10 digits without + we can assume Mexican standard if needed, but it's best to let user type country code)
        val formattedPhone = if (cleanPhone.length == 10) "52$cleanPhone" else cleanPhone // default to Mexico code if 10 digits
        
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp. Asegúrate de tener la app instalada.", Toast.LENGTH_LONG).show()
    }
}

// --- SCREEN 1: DASHBOARD ---
@Composable
fun DashboardScreen(viewModel: MarketingViewModel, onNavigateToTab: (Int) -> Unit) {
    val clients by viewModel.clientsList.collectAsStateWithLifecycle()
    val leads by viewModel.leadsList.collectAsStateWithLifecycle()
    val meetings by viewModel.upcomingMeetings.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val pendingCollect by viewModel.pendingCollectCount.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Logo / Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_an_marketing_banner),
                    contentDescription = "AN Marketing Digital Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "AN Marketing Digital",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gestión de Campañas y Clientes de Alto Nivel",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Metrics Grid Row
        item {
            Text(
                text = "Métricas Clave",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Ingresos Mensuales",
                    value = "$${String.format("%.2f", totalRevenue)} USD",
                    icon = Icons.Default.TrendingUp,
                    accentColor = MarketingSecondary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Por Cobrar",
                    value = "$pendingCollect Clientes",
                    icon = Icons.Default.Warning,
                    accentColor = MarketingTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Clientes Activos",
                    value = "${clients.size}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = MarketingPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Leads en Seguimiento",
                    value = "${leads.size}",
                    icon = Icons.Default.FilterList,
                    accentColor = Color(0xFF6366F1), // Indigo
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Meetings Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximas Reuniones (${meetings.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTab(3) }
                )
            }
        }

        if (meetings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = "Sin reuniones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tienes reuniones programadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(meetings.take(3)) { meeting ->
                MeetingItemCard(meeting = meeting, onToggleComplete = {
                    viewModel.toggleMeetingCompleted(meeting.id, meeting.isCompleted)
                })
            }
        }

        // Quick Shortcuts
        item {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab(1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("shortcut_add_client"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Lead/Cliente", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }

                Button(
                    onClick = { onNavigateToTab(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("shortcut_collect"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = MarketingSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cobrar por WhatsApp", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- SCREEN 2: CLIENTS & LEADS ---
@Composable
fun ClientsScreen(viewModel: MarketingViewModel) {
    val clients by viewModel.clientsList.collectAsStateWithLifecycle()
    val leads by viewModel.leadsList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Clients, 1: Leads
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedClientForDetail by remember { mutableStateOf<ClientLead?>(null) }
    var clientToEdit by remember { mutableStateOf<ClientLead?>(null) }
    var showConvertDialog by remember { mutableStateOf<ClientLead?>(null) }

    val filteredList = if (selectedTab == 0) {
        clients.filter { it.name.contains(searchQuery, ignoreCase = true) || it.company.contains(searchQuery, ignoreCase = true) }
    } else {
        leads.filter { it.name.contains(searchQuery, ignoreCase = true) || it.company.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title & Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) "Clientes Activos" else "Prospectos / Leads",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("add_client_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o empresa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Buttons
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Clientes (${clients.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Leads (${leads.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (selectedTab == 0) Icons.Default.Business else Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron resultados" else if (selectedTab == 0) "No tienes clientes activos todavía" else "No tienes leads registrados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { clientLead ->
                        ClientLeadItemCard(
                            clientLead = clientLead,
                            onClick = { selectedClientForDetail = clientLead },
                            onEdit = { clientToEdit = clientLead },
                            onDelete = { viewModel.deleteClientLead(clientLead) },
                            onConvert = { showConvertDialog = clientLead }
                        )
                    }
                }
            }
        }

        // --- DIALOGS & OVERLAYS ---
        if (showAddDialog) {
            AddEditClientLeadDialog(
                isEdit = false,
                onDismiss = { showAddDialog = false },
                onSave = { name, company, phone, email, service, fee, day, isLeadState, leadStatus, notes ->
                    viewModel.addClientLead(name, company, phone, email, service, fee, day, isLeadState, leadStatus, notes)
                    showAddDialog = false
                }
            )
        }

        if (clientToEdit != null) {
            AddEditClientLeadDialog(
                isEdit = true,
                existingClientLead = clientToEdit,
                onDismiss = { clientToEdit = null },
                onSave = { name, company, phone, email, service, fee, day, isLeadState, leadStatus, notes ->
                    val updated = clientToEdit!!.copy(
                        name = name,
                        company = company,
                        phone = phone,
                        email = email,
                        service = service,
                        monthlyFee = fee,
                        billingDay = day,
                        isLead = isLeadState,
                        leadStatus = leadStatus,
                        notes = notes
                    )
                    viewModel.updateClientLead(updated)
                    clientToEdit = null
                }
            )
        }

        if (showConvertDialog != null) {
            ConvertLeadDialog(
                lead = showConvertDialog!!,
                onDismiss = { showConvertDialog = null },
                onConfirm = { fee, day ->
                    viewModel.convertLeadToClient(showConvertDialog!!, fee, day)
                    showConvertDialog = null
                    Toast.makeText(context, "¡Lead convertido en Cliente Activo!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (selectedClientForDetail != null) {
            ClientDetailDialog(
                clientLead = selectedClientForDetail!!,
                onDismiss = { selectedClientForDetail = null }
            )
        }
    }
}

@Composable
fun ClientLeadItemCard(
    clientLead: ClientLead,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConvert: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("client_lead_item_${clientLead.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clientLead.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = clientLead.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Badge for Status
                val statusColor = when {
                    !clientLead.isLead -> MarketingSecondary
                    clientLead.leadStatus == "Nuevo" -> MarketingTertiary
                    clientLead.leadStatus == "En Contacto" -> MarketingPrimary
                    clientLead.leadStatus == "Propuesta" -> Color(0xFF6366F1)
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (!clientLead.isLead) "Cliente" else clientLead.leadStatus,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Servicio: ${clientLead.service}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    if (!clientLead.isLead) {
                        Text(
                            text = "Cobro: $${clientLead.monthlyFee} USD / Día ${clientLead.billingDay}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MarketingPrimary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (clientLead.isLead) {
                        IconButton(
                            onClick = onConvert,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MarketingSecondary.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Convertir", tint = MarketingSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: BILLING AUTOMATION (WHATSAPP REMINDERS) ---
@Composable
fun BillingScreen(viewModel: MarketingViewModel) {
    val clients by viewModel.clientsList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var toneSelected by remember { mutableStateOf("friendly") } // "friendly", "formal", "urgent"
    var clientForReminder by remember { mutableStateOf<ClientLead?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Cobranza Automatizada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gestiona cobros y envía recordatorios profesionales por WhatsApp con un solo toque.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (clients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Primero agrega un Cliente Activo con cobro programado para habilitar la cobranza.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clients) { client ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = client.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Servicio: ${client.service}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (client.isPaidThisMonth) MarketingSecondary.copy(alpha = 0.15f)
                                            else MarketingTertiary.copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (client.isPaidThisMonth) "Pagado" else "Pendiente",
                                        color = if (client.isPaidThisMonth) MarketingSecondary else MarketingTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Monto: $${client.monthlyFee} USD",
                                        fontWeight = FontWeight.Bold,
                                        color = MarketingPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Cobrar el día: ${client.billingDay} de cada mes",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Toggle payment status
                                    Button(
                                        onClick = { viewModel.togglePaymentStatus(client.id, client.isPaidThisMonth) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (client.isPaidThisMonth) MaterialTheme.colorScheme.surfaceVariant else MarketingSecondary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(
                                            if (client.isPaidThisMonth) Icons.Default.Close else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (client.isPaidThisMonth) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (client.isPaidThisMonth) "Marcar Pendiente" else "Marcar Pagado",
                                            fontSize = 11.sp,
                                            color = if (client.isPaidThisMonth) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // WhatsApp Cobra
                                    if (!client.isPaidThisMonth) {
                                        Button(
                                            onClick = { clientForReminder = client },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cobrar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reminder Message Dialog / Selection
        if (clientForReminder != null) {
            Dialog(onDismissRequest = { clientForReminder = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Mensaje para ${clientForReminder!!.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Selecciona el tono del recordatorio:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ToneButton("Amistoso 😊", toneSelected == "friendly", modifier = Modifier.weight(1f)) { toneSelected = "friendly" }
                            ToneButton("Formal 💼", toneSelected == "formal", modifier = Modifier.weight(1f)) { toneSelected = "formal" }
                            ToneButton("Urgente ⚠️", toneSelected == "urgent", modifier = Modifier.weight(1f)) { toneSelected = "urgent" }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Preview Box
                        val messagePreview = viewModel.generateWhatsAppBillingMessage(clientForReminder!!, toneSelected)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = messagePreview,
                                        fontSize = 12.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { clientForReminder = null }) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    sendWhatsAppMessage(context, clientForReminder!!.phone, messagePreview)
                                    clientForReminder = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enviar por WA", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToneButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- SCREEN 4: MEETING Reminders ---
@Composable
fun MeetingsScreen(viewModel: MarketingViewModel) {
    val meetings by viewModel.allMeetings.collectAsStateWithLifecycle()
    val clientsAndLeads by viewModel.allClientLeads.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Agenda de Reuniones",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No dejes pasar ninguna cita clave de AN Marketing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("add_meeting_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agendar", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (meetings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tienes reuniones registradas.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending Meetings
                val pending = meetings.filter { !it.isCompleted }
                val completed = meetings.filter { it.isCompleted }

                if (pending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Reuniones Pendientes (${pending.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(pending) { meeting ->
                        MeetingItemCard(meeting = meeting, onToggleComplete = {
                            viewModel.toggleMeetingCompleted(meeting.id, meeting.isCompleted)
                        }, onDelete = {
                            viewModel.deleteMeeting(meeting)
                        })
                    }
                }

                if (completed.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Historial / Completadas (${completed.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MarketingSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(completed) { meeting ->
                        MeetingItemCard(meeting = meeting, onToggleComplete = {
                            viewModel.toggleMeetingCompleted(meeting.id, meeting.isCompleted)
                        }, onDelete = {
                            viewModel.deleteMeeting(meeting)
                        })
                    }
                }
            }
        }

        if (showAddDialog) {
            AddMeetingDialog(
                clientLeads = clientsAndLeads,
                onDismiss = { showAddDialog = false },
                onSave = { clientLeadId, clientName, title, desc, dateMillis, time ->
                    viewModel.addMeeting(clientLeadId, clientName, title, desc, dateMillis, time)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MeetingItemCard(
    meeting: Meeting,
    onToggleComplete: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val formatter = remember { SimpleDateFormat("dd 'de' MMM, yyyy", Locale("es", "MX")) }
    val dateStr = formatter.format(Date(meeting.dateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (meeting.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = meeting.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(checkedColor = MarketingSecondary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (meeting.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if (meeting.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cliente: ${meeting.clientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (meeting.description.isNotBlank()) {
                    Text(
                        text = meeting.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Text(text = "$dateStr a las ${meeting.timeString} hs", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// --- SCREEN 5: AI PERFORMANCE REPORTS ---
@Composable
fun ReportScreen(viewModel: MarketingViewModel) {
    val clients by viewModel.clientsList.collectAsStateWithLifecycle()
    val reportState by viewModel.reportState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedClientIndex by remember { mutableStateOf(0) }
    var impressions by remember { mutableStateOf("50000") }
    var clicks by remember { mutableStateOf("1200") }
    var conversions by remember { mutableStateOf("45") }
    var spent by remember { mutableStateOf("350.0") }
    var additionalNotes by remember { mutableStateOf("") }

    val currentClient = clients.getOrNull(selectedClientIndex)

    LaunchedEffect(currentClient) {
        viewModel.resetReportState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reportes con IA",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Genera reportes de rendimiento profesionales automatizados con Gemini para tus clientes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (clients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Primero agrega un Cliente Activo para generar reportes de marketing.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Client Selector Dropdown
                item {
                    Text("Selecciona el Cliente:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    var expandedDropdown by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { expandedDropdown = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentClient?.name ?: "Seleccionar Cliente", fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            clients.forEachIndexed { index, client ->
                                DropdownMenuItem(
                                    text = { Text("${client.name} (${client.company})") },
                                    onClick = {
                                        selectedClientIndex = index
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Metrics Entry Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ingresa las métricas mensuales del periodo:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = impressions,
                                    onValueChange = { impressions = it },
                                    label = { Text("Impresiones") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = clicks,
                                    onValueChange = { clicks = it },
                                    label = { Text("Clics") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = conversions,
                                    onValueChange = { conversions = it },
                                    label = { Text("Conversiones") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = spent,
                                    onValueChange = { spent = it },
                                    label = { Text("Inversión ($ USD)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = additionalNotes,
                                onValueChange = { additionalNotes = it },
                                label = { Text("Notas o logros clave adicionales...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (currentClient != null) {
                                        viewModel.generatePerformanceReport(
                                            clientLead = currentClient,
                                            impressions = impressions.toIntOrNull() ?: 0,
                                            clicks = clicks.toIntOrNull() ?: 0,
                                            conversions = conversions.toIntOrNull() ?: 0,
                                            spent = spent.toDoubleOrNull() ?: 0.0,
                                            additionalNotes = additionalNotes
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_report_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar Reporte con Inteligencia Artificial", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // AI Output Panel
                item {
                    when (val state = reportState) {
                        is ReportState.Idle -> {
                            if (currentClient != null && currentClient.lastReportContent.isNotBlank()) {
                                Text("Último Reporte Guardado:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                ReportDisplayCard(
                                    clientName = currentClient.name,
                                    reportText = currentClient.lastReportContent,
                                    context = context
                                )
                            }
                        }
                        is ReportState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Gemini está analizando las métricas y redactando...", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        is ReportState.Success -> {
                            Text("¡Reporte de Rendimiento Generado!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MarketingSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            ReportDisplayCard(
                                clientName = currentClient!!.name,
                                reportText = state.reportText,
                                context = context
                            )
                        }
                        is ReportState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text("Error al generar reporte: ${state.error}", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDisplayCard(clientName: String, reportText: String, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reporte Listo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Reporte de Marketing", reportText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartir Reporte con $clientName")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reportText,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- SUB-DIALOG: ADD/EDIT CLIENT OR LEAD ---
@Composable
fun AddEditClientLeadDialog(
    isEdit: Boolean,
    existingClientLead: ClientLead? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        company: String,
        phone: String,
        email: String,
        service: String,
        fee: Double,
        day: Int,
        isLead: Boolean,
        leadStatus: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(existingClientLead?.name ?: "") }
    var company by remember { mutableStateOf(existingClientLead?.company ?: "") }
    var phone by remember { mutableStateOf(existingClientLead?.phone ?: "") }
    var email by remember { mutableStateOf(existingClientLead?.email ?: "") }
    var service by remember { mutableStateOf(existingClientLead?.service ?: "Gestión Redes Sociales") }
    var monthlyFee by remember { mutableStateOf(existingClientLead?.monthlyFee?.toString() ?: "350") }
    var billingDay by remember { mutableStateOf(existingClientLead?.billingDay?.toString() ?: "15") }
    var isLead by remember { mutableStateOf(existingClientLead?.isLead ?: true) }
    var leadStatus by remember { mutableStateOf(existingClientLead?.leadStatus ?: "Nuevo") }
    var notes by remember { mutableStateOf(existingClientLead?.notes ?: "") }

    val servicesList = listOf(
        "Gestión Redes Sociales",
        "Google Ads & SEM",
        "SEO Orgánico",
        "Diseño Web & Embudo",
        "Email Marketing",
        "Branding Completo"
    )

    val leadStatuses = listOf("Nuevo", "En Contacto", "Propuesta", "No Interesado")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (isEdit) "Editar Lead / Cliente" else "Registrar Nuevo Lead / Cliente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Type Toggle: Lead vs Active Client
                item {
                    Text("Tipo de registro:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isLead = true },
                            modifier = Modifier.weight(1f).testTag("select_lead_type"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Prospecto / Lead", color = if (isLead) Color.Black else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { isLead = false },
                            modifier = Modifier.weight(1f).testTag("select_client_type"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLead) MarketingSecondary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cliente Activo", color = if (!isLead) Color.Black else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }
                }

                // Input Fields
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Completo Contacto") },
                        modifier = Modifier.fillMaxWidth().testTag("input_client_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Empresa o Marca") },
                        modifier = Modifier.fillMaxWidth().testTag("input_client_company"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp (Ej: +525512345678)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_client_phone"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth().testTag("input_client_email"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }

                // Service Dropdown
                item {
                    var expandedService by remember { mutableStateOf(false) }
                    Text("Servicio Contratado:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable { expandedService = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(service, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedService,
                            onDismissRequest = { expandedService = false }
                        ) {
                            servicesList.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        service = s
                                        expandedService = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Conditional fields if Client
                if (!isLead) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = monthlyFee,
                                onValueChange = { monthlyFee = it },
                                label = { Text("Iguala Mensual ($ USD)") },
                                modifier = Modifier.weight(1f).testTag("input_client_fee"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = billingDay,
                                onValueChange = { billingDay = it },
                                label = { Text("Día de Cobro") },
                                modifier = Modifier.weight(1f).testTag("input_client_day"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                } else {
                    // Lead status selector
                    item {
                        var expandedStatus by remember { mutableStateOf(false) }
                        Text("Estado del Lead:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedStatus = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(leadStatus, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                leadStatuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            leadStatus = status
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas y comentarios clave...") },
                        modifier = Modifier.fillMaxWidth().testTag("input_client_notes"),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Action Row
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && company.isNotBlank()) {
                                    onSave(
                                        name,
                                        company,
                                        phone,
                                        email,
                                        service,
                                        monthlyFee.toDoubleOrNull() ?: 0.0,
                                        billingDay.toIntOrNull() ?: 1,
                                        isLead,
                                        leadStatus,
                                        notes
                                    )
                                }
                            },
                            enabled = name.isNotBlank() && company.isNotBlank(),
                            modifier = Modifier.testTag("save_client_button")
                        ) {
                            Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-DIALOG: CONVERT LEAD TO CLIENT ---
@Composable
fun ConvertLeadDialog(
    lead: ClientLead,
    onDismiss: () -> Unit,
    onConfirm: (monthlyFee: Double, billingDay: Int) -> Unit
) {
    var fee by remember { mutableStateOf("350") }
    var day by remember { mutableStateOf("5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Convertir Lead en Cliente Activo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vas a formalizar la pauta de ${lead.name} (${lead.company}). Ingresa los datos de cobranza mensual:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fee,
                    onValueChange = { fee = it },
                    label = { Text("Cobro Mensual ($ USD)") },
                    modifier = Modifier.fillMaxWidth().testTag("convert_fee_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Día de Cobro (1-28)") },
                    modifier = Modifier.fillMaxWidth().testTag("convert_day_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                fee.toDoubleOrNull() ?: 350.0,
                                day.toIntOrNull() ?: 5
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MarketingSecondary)
                    ) {
                        Text("Confirmar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SUB-DIALOG: CLIENT DETAILS PANEL ---
@Composable
fun ClientDetailDialog(
    clientLead: ClientLead,
    onDismiss: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ficha del Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }

                item {
                    Text(text = clientLead.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = clientLead.company, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

                item {
                    DetailRow(icon = Icons.Default.Phone, label = "WhatsApp", value = clientLead.phone)
                }

                item {
                    DetailRow(icon = Icons.Default.Email, label = "Correo", value = clientLead.email)
                }

                item {
                    DetailRow(icon = Icons.Default.Campaign, label = "Servicio", value = clientLead.service)
                }

                item {
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Estructura Financiera",
                        value = if (clientLead.isLead) "Lead (${clientLead.leadStatus})"
                                else "$${clientLead.monthlyFee} USD / Día ${clientLead.billingDay}"
                    )
                }

                if (clientLead.notes.isNotBlank()) {
                    item {
                        Column {
                            Text("Notas:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(clientLead.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (clientLead.lastReportContent.isNotBlank()) {
                    item {
                        Column {
                            val repDate = formatter.format(Date(clientLead.lastReportDate))
                            Text("Último Reporte de Rendimiento ($repDate):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                LazyColumn {
                                    item {
                                        Text(
                                            clientLead.lastReportContent,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- SUB-DIALOG: ADD MEETING ---
@Composable
fun AddMeetingDialog(
    clientLeads: List<ClientLead>,
    onDismiss: () -> Unit,
    onSave: (clientLeadId: Int?, clientName: String, title: String, desc: String, dateMillis: Long, time: String) -> Unit
) {
    var selectedClientIndex by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") } // dd/MM/yyyy
    var timeString by remember { mutableStateOf("10:00") } // HH:mm

    val currentSelectedClient = clientLeads.getOrNull(selectedClientIndex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = "Agendar Nueva Reunión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                // Client Selector Dropdown
                if (clientLeads.isNotEmpty()) {
                    item {
                        var expandedDropdown by remember { mutableStateOf(false) }
                        Text("Relacionar con Cliente o Lead:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { expandedDropdown = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currentSelectedClient?.name ?: "Seleccionar", fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                clientLeads.forEachIndexed { idx, client ->
                                    DropdownMenuItem(
                                        text = { Text("${client.name} (${client.company})") },
                                        onClick = {
                                            selectedClientIndex = idx
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Cita") },
                        placeholder = { Text("Ej: Presentación de Propuesta / Kick-off") },
                        modifier = Modifier.fillMaxWidth().testTag("meeting_title_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Objetivo / Temario") },
                        modifier = Modifier.fillMaxWidth().testTag("meeting_desc_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateString,
                            onValueChange = { dateString = it },
                            label = { Text("Fecha (dd/mm/aaaa)") },
                            placeholder = { Text("Ej: 15/07/2026") },
                            modifier = Modifier.weight(1f).testTag("meeting_date_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = timeString,
                            onValueChange = { timeString = it },
                            label = { Text("Hora (24h)") },
                            placeholder = { Text("Ej: 14:30") },
                            modifier = Modifier.weight(1f).testTag("meeting_time_input"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val parsedDate = try {
                                    parser.parse(dateString)?.time ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    System.currentTimeMillis()
                                }

                                onSave(
                                    currentSelectedClient?.id,
                                    currentSelectedClient?.name ?: "Cita General",
                                    title,
                                    description,
                                    parsedDate,
                                    timeString
                                )
                            },
                            enabled = title.isNotBlank() && dateString.isNotBlank(),
                            modifier = Modifier.testTag("save_meeting_button")
                        ) {
                            Text("Agendar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
