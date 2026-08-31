@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.seniorease.app.data.Hospital
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*

@Composable
fun HospitalScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onBookCabClick: () -> Unit
) {
    val hospitals by viewModel.hospitals.collectAsState()

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
            // Header Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏥 HOSPITAL",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentTeal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap an option below to contact or navigate to your hospital.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    textAlign = TextAlign.Center
                )
            }

            // Hospital list block
            if (hospitals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hospitals have been configured yet.\n\nAsk a caregiver or family member to add a hospital in settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(hospitals) { hospital ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(3.dp, AccentTeal),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = hospital.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Text(
                                    text = hospital.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Ambulance Button
                                Button(
                                    onClick = {
                                        viewModel.executeAction(
                                            CustomAction(
                                                title = "${hospital.name} Ambulance",
                                                icon = "🚑",
                                                actionType = "CALL",
                                                payload = hospital.ambulanceNumber
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🚑", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text("CALL AMBULANCE", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Phone Button
                                Button(
                                    onClick = {
                                        viewModel.executeAction(
                                            CustomAction(
                                                title = hospital.name,
                                                icon = "📞",
                                                actionType = "CALL",
                                                payload = hospital.phoneNumber
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📞", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text("CALL HOSPITAL DESK", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Navigate Button
                                Button(
                                    onClick = {
                                        viewModel.executeAction(
                                            CustomAction(
                                                title = hospital.name,
                                                icon = "🗺️",
                                                actionType = "NAVIGATE",
                                                payload = hospital.address
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🗺️", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text("NAVIGATE THERE", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Book a cab Button
                                Button(
                                    onClick = {
                                        viewModel.prefilledCabDestination = hospital.address
                                        onBookCabClick()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🚕", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text("BOOK A CAB TO HOSPITAL", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
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
}
