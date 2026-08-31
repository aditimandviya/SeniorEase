@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorease.app.data.CustomAction
import com.seniorease.app.data.EmergencyContact
import com.seniorease.app.data.Hospital
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*

@Composable
fun EmergencyScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.emergencyContacts.collectAsState()
    val hospitals by viewModel.hospitals.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var showFamilySelect by remember { mutableStateOf(false) }
    var showHospitalSelect by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        onNavigateBack()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚨 EMERGENCY",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = EmergencyRed,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap what you need. We will help you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    textAlign = TextAlign.Center
                )
            }

            // Central Actions Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. CALL AMBULANCE
                Button(
                    onClick = {
                        // Find ambulance phone from configured hospitals or default 102/108/911
                        val phone = hospitals.firstOrNull { it.ambulanceNumber.isNotBlank() }?.ambulanceNumber ?: "108"
                        viewModel.executeAction(
                            CustomAction(title = "Ambulance", icon = "🚑", actionType = "CALL", payload = phone)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚑", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Text("CALL AMBULANCE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. CALL HOSPITAL
                Button(
                    onClick = {
                        if (hospitals.isEmpty()) {
                            // If no hospitals configured, notify user or dial standard helpline
                            viewModel.executeAction(
                                CustomAction(title = "General Help", icon = "🏥", actionType = "CALL", payload = "108")
                            )
                        } else if (hospitals.size == 1) {
                            val hosp = hospitals.first()
                            viewModel.executeAction(
                                CustomAction(title = hosp.name, icon = "🏥", actionType = "CALL", payload = hosp.phoneNumber)
                            )
                        } else {
                            showHospitalSelect = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏥", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Text("CALL HOSPITAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 3. CALL FAMILY
                Button(
                    onClick = {
                        if (contacts.isEmpty()) {
                            viewModel.onShowToast("No emergency contacts configured yet.")
                        } else if (contacts.size == 1) {
                            val contact = contacts.first()
                            viewModel.executeAction(
                                CustomAction(title = contact.name, icon = "📞", actionType = "CALL", payload = contact.phoneNumber)
                            )
                        } else {
                            showFamilySelect = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👨‍👩‍👧", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Text("CALL FAMILY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 4. SEND MY LOCATION
                Button(
                    onClick = {
                        viewModel.triggerEmergencyWorkflow()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Text("SEND MY LOCATION", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Big Back Button
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(3.dp, BorderMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "GO BACK",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryText
                )
            }
        }
    }

    // Modal Sheet or dialog for calling specific Family member
    if (showFamilySelect) {
        AlertDialog(
            onDismissRequest = { showFamilySelect = false },
            title = {
                Text(
                    "Call Emergency Contact",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(contacts) { contact ->
                        Card(
                            onClick = {
                                showFamilySelect = false
                                viewModel.executeAction(
                                    CustomAction(
                                        title = contact.name,
                                        icon = "📞",
                                        actionType = "CALL",
                                        payload = contact.phoneNumber
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = BorderStroke(2.dp, AccentBlue),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${contact.name} (${contact.relationship})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = contact.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { showFamilySelect = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

    // Modal Sheet or dialog for calling specific Hospital
    if (showHospitalSelect) {
        AlertDialog(
            onDismissRequest = { showHospitalSelect = false },
            title = {
                Text(
                    "Select Hospital Call",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(hospitals) { hosp ->
                        Card(
                            onClick = {
                                showHospitalSelect = false
                                viewModel.executeAction(
                                    CustomAction(
                                        title = hosp.name,
                                        icon = "🏥",
                                        actionType = "CALL",
                                        payload = hosp.phoneNumber
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = BorderStroke(2.dp, AccentTeal),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = hosp.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Helpline: ${hosp.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { showHospitalSelect = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}
