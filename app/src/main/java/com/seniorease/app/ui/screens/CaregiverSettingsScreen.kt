@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.seniorease.app.data.SavedLocation
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.emergencyContacts.collectAsState()
    val hospitals by viewModel.hospitals.collectAsState()
    val locations by viewModel.savedLocations.collectAsState()
    val actions by viewModel.customActions.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Custom Actions, 1: Contacts & Hospitals, 2: System Settings
    var showActionBuilder by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        onNavigateBack()
    }

    // Backup/Restore States
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var backupStatusText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caregiver Panel",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("DONE")
                }
            }

            // Tab selection
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Actions", "Contacts").forEachIndexed { index, title ->
                        val isSelected = activeTab == index
                        Button(
                            onClick = { activeTab = index },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentBlue else BorderLight,
                                contentColor = if (isSelected) Color.White else PrimaryText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Hospitals", "Backup & Tools").forEachIndexed { index, title ->
                        val actualIndex = index + 2
                        val isSelected = activeTab == actualIndex
                        Button(
                            onClick = { activeTab = actualIndex },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentBlue else BorderLight,
                                contentColor = if (isSelected) Color.White else PrimaryText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Content Panel
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    0 -> CustomActionsTab(
                        actions = actions,
                        onDeleteAction = { viewModel.deleteCustomAction(it) },
                        onAddActionClick = { showActionBuilder = true }
                    )
                    1 -> ContactsTab(
                        contacts = contacts,
                        onAddContact = { name, rel, phone -> viewModel.addEmergencyContact(name, rel, phone, contacts.size + 1) },
                        onDeleteContact = { viewModel.deleteEmergencyContact(it) }
                    )
                    2 -> HospitalsTab(
                        hospitals = hospitals,
                        onAddHospital = { name, addr, phone, amb -> viewModel.addHospital(name, addr, phone, amb, phone) },
                        onDeleteHospital = { viewModel.deleteHospital(it) },
                        onUpdateHospital = { viewModel.updateHospital(it) }
                    )
                    3 -> SystemSettingsTab(
                        defaultCabApp = settings?.defaultCabApp ?: "Uber",
                        defaultCabAppPackage = settings?.defaultCabAppPackage ?: "com.ubercab",
                        onUpdateCabApp = { app, pkg -> viewModel.updateDefaultCabApp(app, pkg) },
                        isTestMode = settings?.testModeEnabled ?: false,
                        onToggleTestMode = { viewModel.toggleTestMode() },
                        onBackupClick = {
                            // Compile database tables to JSON format
                            try {
                                val root = JSONObject()
                                root.put("profile_name", userProfile?.name ?: "")
                                root.put("profile_lang", userProfile?.preferredLanguage ?: "English")
                                root.put("profile_home", userProfile?.homeAddress ?: "")

                                val contactArray = JSONArray()
                                contacts.forEach {
                                    val obj = JSONObject()
                                    obj.put("name", it.name)
                                    obj.put("relationship", it.relationship)
                                    obj.put("phone", it.phoneNumber)
                                    contactArray.put(obj)
                                }
                                root.put("contacts", contactArray)

                                val hospArray = JSONArray()
                                hospitals.forEach {
                                    val obj = JSONObject()
                                    obj.put("name", it.name)
                                    obj.put("address", it.address)
                                    obj.put("phone", it.phoneNumber)
                                    obj.put("ambulance", it.ambulanceNumber)
                                    hospArray.put(obj)
                                }
                                root.put("hospitals", hospArray)

                                val actionArray = JSONArray()
                                actions.forEach {
                                    val obj = JSONObject()
                                    obj.put("title", it.title)
                                    obj.put("icon", it.icon)
                                    obj.put("actionType", it.actionType)
                                    obj.put("payload", it.payload)
                                    obj.put("payloadExtra", it.payloadExtra ?: "")
                                    actionArray.put(obj)
                                }
                                root.put("actions", actionArray)

                                backupJsonText = root.toString(2)
                                backupStatusText = "Copy JSON below to backup config"
                            } catch (e: Exception) {
                                backupStatusText = "Error compiling backup: ${e.message}"
                            }
                            showBackupRestoreDialog = true
                        },
                        onClearData = {
                            viewModel.clearAllData()
                            onNavigateBack()
                        },
                        locations = locations,
                        onAddLocation = { label, addr -> viewModel.addSavedLocation(label, addr) },
                        onDeleteLocation = { viewModel.deleteSavedLocation(it) }
                    )
                }
            }
        }
    }

    // Custom Action Builder Dialog (Requirement 6)
    if (showActionBuilder) {
        var actionTitle by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("📞") }
        var selectedType by remember { mutableStateOf("CALL") } // CALL, OPEN_APP, NAVIGATE, OPEN_SETTINGS, FLASHLIGHT
        var actionPayload by remember { mutableStateOf("") }

        val iconsList = listOf("📞", "💬", "🏠", "🏥", "👨‍⚕️", "🚕", "🔦", "⚙️", "📺", "🗺️")
        val actionTypes = listOf("CALL", "OPEN_APP", "NAVIGATE", "OPEN_SETTINGS", "FLASHLIGHT")

        AlertDialog(
            onDismissRequest = { showActionBuilder = false },
            title = {
                Text("Custom Action Builder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Action name
                    Text("What should the button say?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = actionTitle,
                        onValueChange = { actionTitle = it },
                        placeholder = { Text("e.g. Call Son") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon library
                    Text("Choose an Icon", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsList.take(5).forEach { icon ->
                            FilterChip(
                                selected = selectedIcon == icon,
                                onClick = { selectedIcon = icon },
                                label = { Text(icon, fontSize = 24.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsList.drop(5).forEach { icon ->
                            FilterChip(
                                selected = selectedIcon == icon,
                                onClick = { selectedIcon = icon },
                                label = { Text(icon, fontSize = 24.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Action type
                    Text("What should this action do?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        actionTypes.forEach { type ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                Text(
                                    text = when (type) {
                                        "CALL" -> "Call a phone number"
                                        "OPEN_APP" -> "Open another app"
                                        "NAVIGATE" -> "Navigate to address"
                                        "OPEN_SETTINGS" -> "Open settings page"
                                        else -> "Toggle Flashlight"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Config/Payload input
                    if (selectedType != "FLASHLIGHT") {
                        val label = when (selectedType) {
                            "CALL" -> "Phone Number"
                            "OPEN_APP" -> "App Package Name (e.g. com.whatsapp)"
                            "NAVIGATE" -> "Target Destination Address"
                            else -> "Setting Type (WIFI, BLUETOOTH, SOUND, DISPLAY)"
                        }
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = actionPayload,
                            onValueChange = { actionPayload = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionTitle.isNotBlank()) {
                            viewModel.addCustomAction(
                                title = actionTitle,
                                icon = selectedIcon,
                                type = selectedType,
                                payload = actionPayload
                            )
                            showActionBuilder = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = actionTitle.isNotBlank()
                ) {
                    Text("SAVE ACTION", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showActionBuilder = false },
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            title = {
                Text("Backup & Restore Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(backupStatusText, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonText,
                        onValueChange = { backupJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Attempt to parse and restore DB from input JSON
                        try {
                            val root = JSONObject(backupJsonText)
                            val profileName = root.optString("profile_name", "User")
                            val profileLang = root.optString("profile_lang", "English")
                            val profileHome = root.optString("profile_home", "")
                            
                            viewModel.saveProfile(profileName, profileLang, profileHome)

                            // Load contacts
                            val contactsArray = root.optJSONArray("contacts")
                            if (contactsArray != null) {
                                for (i in 0 until contactsArray.length()) {
                                    val item = contactsArray.getJSONObject(i)
                                    viewModel.addEmergencyContact(
                                        item.getString("name"),
                                        item.getString("relationship"),
                                        item.getString("phone"),
                                        i + 1
                                    )
                                }
                            }

                            // Load hospitals
                            val hospArray = root.optJSONArray("hospitals")
                            if (hospArray != null) {
                                for (i in 0 until hospArray.length()) {
                                    val item = hospArray.getJSONObject(i)
                                    viewModel.addHospital(
                                        item.getString("name"),
                                        item.getString("address"),
                                        item.getString("phone"),
                                        item.getString("ambulance"),
                                        item.getString("phone")
                                    )
                                }
                            }

                            // Load actions
                            val actionsArray = root.optJSONArray("actions")
                            if (actionsArray != null) {
                                for (i in 0 until actionsArray.length()) {
                                    val item = actionsArray.getJSONObject(i)
                                    viewModel.addCustomAction(
                                        item.getString("title"),
                                        item.getString("icon"),
                                        item.getString("actionType"),
                                        item.getString("payload"),
                                        item.optString("payloadExtra", null)
                                    )
                                }
                            }

                            backupStatusText = "Restore Success! Configuration loaded."
                            viewModel.onShowToast("Database restore successful!")
                        } catch (e: Exception) {
                            backupStatusText = "Restore failed: invalid JSON syntax or tables mismatch."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("IMPORT / RESTORE CONFIG", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showBackupRestoreDialog = false },
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp)
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                }
            }
        )
    }
}

@Composable
fun CustomActionsTab(
    actions: List<CustomAction>,
    onDeleteAction: (CustomAction) -> Unit,
    onAddActionClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddActionClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("CREATE CUSTOM ACTION CARD", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (actions.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No custom action cards configured yet.", style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(actions) { action ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, BorderLight),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(action.icon, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                Column {
                                    Text(action.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("${action.actionType}: ${action.payload}", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                }
                            }
                            IconButton(onClick = { onDeleteAction(action) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsTab(
    contacts: List<EmergencyContact>,
    onAddContact: (String, String, String) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit
) {
    var cName by remember { mutableStateOf("") }
    var cPhone by remember { mutableStateOf("") }
    var cRel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Emergency Family Contacts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            border = BorderStroke(2.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cPhone, onValueChange = { cPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cRel, onValueChange = { cRel = it }, label = { Text("Relationship") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (cName.isNotBlank() && cPhone.isNotBlank()) {
                            onAddContact(cName, cRel, cPhone)
                            cName = ""
                            cPhone = ""
                            cRel = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("ADD EMERGENCY CONTACT")
                }
            }
        }
        contacts.forEach { contact ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${contact.name} (${contact.relationship})", fontWeight = FontWeight.Bold)
                        Text(contact.phoneNumber)
                    }
                    IconButton(onClick = { onDeleteContact(contact) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed)
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalsTab(
    hospitals: List<Hospital>,
    onAddHospital: (String, String, String, String) -> Unit,
    onDeleteHospital: (Hospital) -> Unit,
    onUpdateHospital: (Hospital) -> Unit
) {
    var hName by remember { mutableStateOf("") }
    var hAddr by remember { mutableStateOf("") }
    var hPhone by remember { mutableStateOf("") }
    var hAmb by remember { mutableStateOf("") }
    var editingHospital by remember { mutableStateOf<Hospital?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Hospital Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            border = BorderStroke(2.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = hName, onValueChange = { hName = it }, label = { Text("Hospital Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hAddr, onValueChange = { hAddr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hPhone, onValueChange = { hPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hAmb, onValueChange = { hAmb = it }, label = { Text("Ambulance Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (hName.isNotBlank()) {
                            onAddHospital(hName, hAddr, hPhone, hAmb)
                            hName = ""
                            hAddr = ""
                            hPhone = ""
                            hAmb = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("ADD HOSPITAL")
                }
            }
        }
        hospitals.forEach { hospital ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hospital.name, fontWeight = FontWeight.Bold)
                        Text(hospital.address)
                        Text("Ph: ${hospital.phoneNumber} | Amb: ${hospital.ambulanceNumber}")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            editingHospital = hospital
                            showEditDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Hospital", tint = AccentTeal)
                        }
                        IconButton(onClick = { onDeleteHospital(hospital) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Hospital", tint = EmergencyRed)
                        }
                    }
                }
            }
        }
    }

    // Edit Hospital Dialog
    if (showEditDialog && editingHospital != null) {
        val currentHosp = editingHospital!!
        var eName by remember { mutableStateOf(currentHosp.name) }
        var eAddr by remember { mutableStateOf(currentHosp.address) }
        var ePhone by remember { mutableStateOf(currentHosp.phoneNumber) }
        var eAmb by remember { mutableStateOf(currentHosp.ambulanceNumber) }

        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingHospital = null
            },
            title = {
                Text("Edit Hospital Details", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Hospital Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eAddr, onValueChange = { eAddr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ePhone, onValueChange = { ePhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eAmb, onValueChange = { eAmb = it }, label = { Text("Ambulance Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eName.isNotBlank()) {
                            onUpdateHospital(
                                currentHosp.copy(
                                    name = eName,
                                    address = eAddr,
                                    phoneNumber = ePhone,
                                    ambulanceNumber = eAmb,
                                    emergencyNumber = ePhone
                                )
                            )
                            showEditDialog = false
                            editingHospital = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("SAVE CHANGES")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingHospital = null
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun SystemSettingsTab(
    defaultCabApp: String,
    defaultCabAppPackage: String,
    onUpdateCabApp: (String, String) -> Unit,
    isTestMode: Boolean,
    onToggleTestMode: () -> Unit,
    onBackupClick: () -> Unit,
    onClearData: () -> Unit,
    locations: List<SavedLocation>,
    onAddLocation: (String, String) -> Unit,
    onDeleteLocation: (SavedLocation) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager

    var lLabel by remember { mutableStateOf("") }
    var lAddr by remember { mutableStateOf("") }

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Saved Places management section
        Text("Saved Places", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            border = BorderStroke(2.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = lLabel, onValueChange = { lLabel = it }, label = { Text("Label (e.g. Doctor, Mall)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lAddr, onValueChange = { lAddr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (lLabel.isNotBlank() && lAddr.isNotBlank()) {
                            onAddLocation(lLabel, lAddr)
                            lLabel = ""
                            lAddr = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("ADD PLACE")
                }
            }
        }
        locations.forEach { loc ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(loc.label.uppercase(), fontWeight = FontWeight.Bold)
                        Text(loc.address)
                    }
                    IconButton(onClick = { onDeleteLocation(loc) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed)
                    }
                }
            }
        }

        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

        Text("Test Suite and Debugging", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)

        // Preferred Cab selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(2.dp, AccentTeal),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Preferred Travel Application", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Select the app that will open when tapping 'BOOK A CAB'.", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                Spacer(modifier = Modifier.height(16.dp))

                getInstalledCabApps.forEach { (name, pkg, isInstalled) ->
                    val status = if (isInstalled) "(Detected)" else "(Not Detected)"
                    val statusColor = if (isInstalled) AccentTeal else SecondaryText

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = defaultCabAppPackage == pkg,
                            onClick = { onUpdateCabApp(name, pkg) }
                        )
                        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                    }
                }
            }
        }

        // Test mode switcher (Requirement 28)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(2.dp, AccentAmber),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Enable Test Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Simulates SMS, phone calls, and navigation without dialing.", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                }
                Switch(
                    checked = isTestMode,
                    onCheckedChange = { onToggleTestMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentAmber, checkedTrackColor = AccentAmber.copy(alpha = 0.5f))
                )
            }
        }

        // Backup configuration (Requirement 19)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(2.dp, BorderMedium),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Backup & Restore Data", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Export database JSON to save settings or import it back.", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                }
                Button(
                    onClick = onBackupClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("OPEN")
                }
            }
        }

        // Danger zone clear data (Requirement 24)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(3.dp, EmergencyRed),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Danger Zone", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = EmergencyRed)
                Text("Delete all personal addresses, emergency numbers, and custom cards.", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClearData,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("DELETE ALL APP CONFIG", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
