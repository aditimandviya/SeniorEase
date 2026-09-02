@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorease.app.engine.LocationHelper
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabWorkflowScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val locations by viewModel.savedLocations.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val hospitals by viewModel.hospitals.collectAsState()

    var step by remember { mutableStateOf(1) } // 1: Destination Select, 2: Vehicle Select, 3: Confirmation
    var selectedDestination by remember { mutableStateOf("") }
    var typedDestination by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf("Cab") } // Cab, Auto
        var selectedCabApp by remember { mutableStateOf("Uber") }
    var selectedCabPackage by remember { mutableStateOf("com.ubercab") }

    androidx.activity.compose.BackHandler {
        if (step > 1) {
            step--
        } else {
            onNavigateBack()
        }
    }
 
    // Prepopulate cab app from settings
    LaunchedEffect(appSettings) {
        appSettings?.let {
            selectedCabApp = it.defaultCabApp
            selectedCabPackage = it.defaultCabAppPackage
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.prefilledCabDestination.isNotBlank()) {
            selectedDestination = viewModel.prefilledCabDestination
            step = 2
            viewModel.prefilledCabDestination = ""
        }
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
                    text = "🚕 BOOK A CAB",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                val desc = when (step) {
                    1 -> "Where would you like to go?"
                    2 -> "Choose vehicle type."
                    else -> "Please confirm your ride request."
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    textAlign = TextAlign.Center
                )
            }

            // Step Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    1 -> {
                        // Destination Selection
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Home Address Suggestion
                            userProfile?.homeAddress?.let { homeAddr ->
                                if (homeAddr.isNotBlank()) {
                                    item {
                                        Card(
                                            onClick = {
                                                selectedDestination = homeAddr
                                                step = 2
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(2.dp, AccentBlue),
                                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🏠", fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
                                                Column {
                                                    Text("GO HOME", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                    Text(homeAddr, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Hospital Address Suggestion
                            val firstHospital = hospitals.firstOrNull()
                            firstHospital?.let { hosp ->
                                if (hosp.address.isNotBlank()) {
                                    item {
                                        Card(
                                            onClick = {
                                                selectedDestination = hosp.address
                                                step = 2
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(2.dp, AccentTeal),
                                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🏥", fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
                                                Column {
                                                    Text("HOSPITAL", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, fontWeight = FontWeight.Bold)
                                                    Text(hosp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = PrimaryText)
                                                    Text(hosp.address, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Saved Locations list
                            items(locations) { loc ->
                                Card(
                                    onClick = {
                                        selectedDestination = loc.address
                                        step = 2
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(2.dp, AccentBlue),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📍", fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
                                        Column {
                                            Text(loc.label.uppercase(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            Text(loc.address, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                        }
                                    }
                                }
                            }

                            // Raw Input TextField
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    border = BorderStroke(2.dp, BorderMedium),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Type another destination:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = typedDestination,
                                            onValueChange = { typedDestination = it },
                                            textStyle = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = BorderMedium
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                if (typedDestination.isNotBlank()) {
                                                    selectedDestination = typedDestination
                                                    step = 2
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            modifier = Modifier.fillMaxWidth().height(60.dp),
                                            enabled = typedDestination.isNotBlank()
                                        ) {
                                            Text("GO TO TYPED ADDRESS", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }

                                        2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Vehicle type selection and preferred app selection
                            Text("Select Ride Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Vehicle choices
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                listOf("Cab", "Auto").forEach { type ->
                                    val isSelected = selectedVehicleType == type
                                    Card(
                                        onClick = { selectedVehicleType = type },
                                        modifier = Modifier.weight(1f).height(100.dp),
                                        border = BorderStroke(3.dp, if (isSelected) AccentBlue else BorderMedium),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) AccentBlue.copy(alpha = 0.1f) else CardBackground)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(if (type == "Cab") "🚕" else "🛺", fontSize = 32.sp)
                                            Text(type, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Select Cab App Provider", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            val appChoices = listOf(
                                Pair("Uber", "com.ubercab"),
                                Pair("Ola", "com.olacabs.customer"),
                                Pair("Rapido", "com.rapido.passenger"),
                                Pair("Namma Yatri", "in.juspay.nammayatri"),
                                Pair("Google Maps", "com.google.android.apps.maps")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                appChoices.forEach { (appName, appPkg) ->
                                    val isSelected = selectedCabPackage == appPkg
                                    Card(
                                        onClick = {
                                            selectedCabApp = appName
                                            selectedCabPackage = appPkg
                                        },
                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                        border = BorderStroke(3.dp, if (isSelected) AccentTeal else BorderMedium),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) AccentTeal.copy(alpha = 0.1f) else CardBackground)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                            Text(
                                                text = appName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    3 -> {
                        // Final Confirmation (Requirement 13)
                        Card(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            border = BorderStroke(3.dp, AccentTeal),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚕 CONFIRM YOUR RIDE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AccentTeal)
                                Spacer(modifier = Modifier.height(20.dp))

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("📍", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                    Column {
                                        Text("PICKUP", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, fontWeight = FontWeight.Bold)
                                        Text("Current GPS Location", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Divider(color = BorderLight, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏁", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                    Column {
                                        Text("DESTINATION", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, fontWeight = FontWeight.Bold)
                                        Text(selectedDestination, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Divider(color = BorderLight, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("📱", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                    Column {
                                        Text("PROVIDER & VEHICLE", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, fontWeight = FontWeight.Bold)
                                        Text("$selectedCabApp ($selectedVehicleType)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    viewModel.launchCabWorkflow(selectedDestination, selectedCabApp, selectedCabPackage)
                                    onNavigateBack()
                                },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                            ) {
                                Text("CONFIRM & BOOK", style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(3.dp, EmergencyRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed)
                            ) {
                                Text("CANCEL / CHANGE", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }

            // Pinned Bottom Navigation Bar: GO BACK & NEXT side-by-side
            if (step < 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (step > 1) {
                                step--
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, BorderMedium)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = SecondaryText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GO BACK", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                    }

                    Button(
                        onClick = {
                            if (step == 1) {
                                if (typedDestination.isNotBlank()) {
                                    selectedDestination = typedDestination
                                }
                                if (selectedDestination.isNotBlank()) {
                                    step = 2
                                }
                            } else if (step == 2) {
                                step = 3
                            }
                        },
                        enabled = (step == 1 && (selectedDestination.isNotBlank() || typedDestination.isNotBlank())) || (step == 2),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("NEXT ➡️", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
