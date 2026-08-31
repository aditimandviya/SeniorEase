@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*

@Composable
fun WelcomeSetupWizard(
    viewModel: MainViewModel,
    onSetupComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var isCaregiverMode by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = currentStep > 1) {
        currentStep--
    }

    // Form inputs state
    var name by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    var homeAddress by remember { mutableStateOf("") }

    // Contact setup state
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactRel by remember { mutableStateOf("") }
    val tempContacts = remember { mutableStateListOf<Triple<String, String, String>>() } // Name, Rel, Phone

    // Hospital setup state
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var hospitalPhone by remember { mutableStateOf("") }
    var hospitalAmbulance by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager
    
    val cabAppsList = listOf(
        Pair("Uber", "com.ubercab"),
        Pair("Ola", "com.olacabs.customer"),
        Pair("Rapido", "com.rapido.passenger"),
        Pair("Namma Yatri", "in.juspay.nammayatri"),
        Pair("Google Maps", "com.google.android.apps.maps")
    )

    val getInstalledCabApps = remember {
        cabAppsList.map { (name, pkg) ->
            val isInstalled = try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
            Triple(name, pkg, isInstalled)
        }
    }

    var selectedCabAppSetup by remember {
        mutableStateOf(
            getInstalledCabApps.firstOrNull { it.third }?.first ?: "Google Maps"
        )
    }
    var selectedCabPackageSetup by remember {
        mutableStateOf(
            getInstalledCabApps.firstOrNull { it.third }?.second ?: "com.google.android.apps.maps"
        )
    }


    // Suggested actions checklist
    val predefinedSuggestions = remember {
        mutableStateListOf(
            Triple("EMERGENCY DIRECT DIAL", "🚨", true),
            Triple("FLASHLIGHT", "🔦", true),
            Triple("BOOK A CAB", "🚕", true),
            Triple("PHONE SETTINGS", "⚙️", true),
            Triple("OPEN WHATSAPP", "💬", true),
            Triple("OPEN YOUTUBE", "📺", true)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Indicator
            if (currentStep > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $currentStep of 5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    LinearProgressIndicator(
                        progress = currentStep / 5f,
                        modifier = Modifier
                            .width(150.dp)
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = BorderLight
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Step Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    0 -> {
                        // Splash Welcome
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Welcome to SeniorEase",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Let's set up your phone so important things are easy to access.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        // Selection buttons
                        Button(
                            onClick = {
                                isCaregiverMode = false
                                currentStep = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("I am setting this up for myself", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                isCaregiverMode = true
                                currentStep = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(3.dp, AccentTeal),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal)
                        ) {
                            Text("I am setting this up for someone else", style = MaterialTheme.typography.titleLarge)
                        }
                    }

                    1 -> {
                        // Profile Info
                        Text(
                            text = if (isCaregiverMode) "Senior User Profile" else "About Yourself",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("What is your name?", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderMedium
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = homeAddress,
                            onValueChange = { homeAddress = it },
                            label = { Text("What is your home address? (Optional)", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderMedium
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Language Toggle Selection
                        Text(
                            text = "Preferred Language",
                            style = MaterialTheme.typography.titleLarge,
                            color = PrimaryText,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("English", "Spanish", "Hindi").forEach { lang ->
                                val isSelected = language == lang
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { language = lang },
                                    label = { Text(lang, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentBlue,
                                        selectedLabelColor = CardBackground
                                    )
                                )
                            }
                        }
                    }

                    2 -> {
                        // Emergency Contacts
                        Text(
                            text = "Emergency Contacts",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add family members or friends who can help in an emergency.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Contact Name (e.g. Daughter)", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone Number", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactRel,
                            onValueChange = { contactRel = it },
                            label = { Text("Relationship (e.g., Son, Neighbor)", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                                    tempContacts.add(Triple(contactName, contactRel, contactPhone))
                                    contactName = ""
                                    contactPhone = ""
                                    contactRel = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD CONTACT", style = MaterialTheme.typography.bodyLarge)
                        }

                        if (tempContacts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Added Contacts:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(8.dp))
                            tempContacts.forEachIndexed { index, contact ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    border = BorderStroke(2.dp, BorderLight),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${index + 1}. ${contact.first} (${contact.second})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            Text(contact.third, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                        }
                                        IconButton(onClick = { tempContacts.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Hospital Setup
                        Text(
                            text = "Hospital Setup",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Configure preferred hospital info for easy navigation or calling.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = hospitalName,
                            onValueChange = { hospitalName = it },
                            label = { Text("Hospital Name (e.g. City Hospital)", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = hospitalAddress,
                            onValueChange = { hospitalAddress = it },
                            label = { Text("Hospital Address", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = hospitalPhone,
                            onValueChange = { hospitalPhone = it },
                            label = { Text("Hospital Helpline Phone", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = hospitalAmbulance,
                            onValueChange = { hospitalAmbulance = it },
                            label = { Text("Ambulance Phone (Optional)", style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    4 -> {
                        // Action suggestions list
                        Text(
                            text = "Actions Quick Access",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select actions you want available immediately on your home screen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        predefinedSuggestions.forEachIndexed { idx, item ->
                            val (title, icon, isChecked) = item
                            Card(
                                onClick = {
                                    predefinedSuggestions[idx] = Triple(title, icon, !isChecked)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                border = BorderStroke(2.dp, if (isChecked) AccentBlue else BorderLight),
                                colors = CardDefaults.cardColors(containerColor = CardBackground)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(icon, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(end = 16.dp))
                                        Text(
                                            title,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                predefinedSuggestions[idx] = Triple(title, icon, checked ?: false)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                        )
                                    }

                                    if (title == "BOOK A CAB" && isChecked) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                                                .background(ScreenBackground, RoundedCornerShape(12.dp))
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Which app should we open to book the cab?",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                                                                        getInstalledCabApps.forEach { (cabName, cabPkg, isInstalled) ->
                                                val statusLabel = if (isInstalled) "(Detected)" else "(Not Detected)"
                                                val labelColor = if (isInstalled) AccentTeal else SecondaryText
 
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                     verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCabAppSetup == cabName,
                                                        onClick = {
                                                            selectedCabAppSetup = cabName
                                                            selectedCabPackageSetup = cabPkg
                                                        }
                                                    )
                                                    Text(
                                                        text = "$cabName ",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryText
                                                    )
                                                    Text(
                                                        text = statusLabel,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = labelColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // Confirmation / Ready screen
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "You are all set!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SeniorEase will now display simple large action cards. You can change these preferences at any time through Caregiver settings.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Navigation Panel
            if (currentStep > 0) {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, BorderMedium)
                        ) {
                            Text("BACK", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep == 5) {
                                                                 viewModel.saveProfile(
                                    name = name.ifBlank { "User" },
                                    language = language,
                                    homeAddr = homeAddress.ifBlank { null }
                                )
                                viewModel.updateDefaultCabApp(selectedCabAppSetup, selectedCabPackageSetup)
                                // Save Emergency Contacts
                                tempContacts.forEachIndexed { index, contact ->
                                    viewModel.addEmergencyContact(
                                        name = contact.first,
                                        rel = contact.second,
                                        phone = contact.third,
                                        priority = index + 1
                                    )
                                }
                                // Save Hospital
                                if (hospitalName.isNotBlank()) {
                                    viewModel.addHospital(
                                        name = hospitalName,
                                        address = hospitalAddress,
                                        phone = hospitalPhone,
                                        ambulance = hospitalAmbulance,
                                        emergency = hospitalPhone
                                    )
                                }
                                                                 // Save selected predefined actions
                                 viewModel.clearCustomActions() // Clear previous custom actions first
                                 predefinedSuggestions.forEach { actionTuple ->
                                     val (title, icon, isChecked) = actionTuple
                                     if (isChecked) {
                                         val type = when (title) {
                                             "FLASHLIGHT" -> "FLASHLIGHT"
                                             "BOOK A CAB" -> "CAB_WORKFLOW"
                                             "PHONE SETTINGS" -> "OPEN_SETTINGS"
                                             "OPEN WHATSAPP" -> "OPEN_APP"
                                             "OPEN YOUTUBE" -> "OPEN_APP"
                                             else -> "CUSTOM_WORKFLOW"
                                         }
                                         val payload = when (title) {
                                             "OPEN WHATSAPP" -> "com.whatsapp"
                                             "OPEN YOUTUBE" -> "com.google.android.youtube"
                                             "PHONE SETTINGS" -> "SYSTEM"
                                             else -> ""
                                         }
                                         if (title != "EMERGENCY DIRECT DIAL") {
                                             viewModel.addCustomAction(title, icon, type, payload)
                                         }
                                     }
                                 }

                                viewModel.setFirstRunCompleted(true)
                                onSetupComplete()
                            } else {
                                currentStep++
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text(
                            text = if (currentStep == 5) "FINISH" else "NEXT",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
