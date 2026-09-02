package com.seniorease.app.ui

import android.app.Application
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seniorease.app.data.*
import com.seniorease.app.engine.ActionEngine
import com.seniorease.app.engine.WorkflowEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application), 
    ActionEngine.ActionListener, WorkflowEngine.WorkflowListener {

    private val database = AppDatabase.getDatabase(application)
    private val appDao = database.appDao()

    val actionEngine = ActionEngine(application).apply { setListener(this@MainViewModel) }
    val workflowEngine = WorkflowEngine(application).apply { setListener(this@MainViewModel) }

    // UI Toast and Mock Logging States
    var toastMessage by mutableStateOf<String?>(null)
    var lastMockLog by mutableStateOf<String?>(null)
    var appNotInstalledState by mutableStateOf<Pair<String, String>?>(null) // appName to packageName
    var showConfirmationAction by mutableStateOf<CustomAction?>(null)
    var activeCabDestination by mutableStateOf<String?>(null)

    // Speech Voice Input State
    var isVoiceListening by mutableStateOf(false)
    var voiceTextResult by mutableStateOf<String?>(null)

    // Flashlight hardware state tracked
    var isFlashlightActive by mutableStateOf(false)
    var prefilledCabDestination by mutableStateOf("")

    // Flow definitions from DB
    val userProfile: StateFlow<UserProfile?> = appDao.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettings?> = appDao.getAppSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customActions: StateFlow<List<CustomAction>> = appDao.getCustomActionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContact>> = appDao.getEmergencyContactsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places: StateFlow<List<PlaceEntity>> = appDao.getPlacesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hospitals: StateFlow<List<Hospital>> = places.map { list ->
        list.filter { it.type == "HOSPITAL" }.map {
            Hospital(
                id = it.id,
                name = it.name,
                address = it.address,
                phoneNumber = it.phoneNumber ?: "",
                ambulanceNumber = it.notes?.replace("Ambulance: ", "") ?: ""
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedLocations: StateFlow<List<SavedLocation>> = places.map { list ->
        list.filter { it.type != "HOSPITAL" }.map {
            SavedLocation(
                id = it.id,
                label = it.name,
                address = it.address
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = appDao.getDocumentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = appDao.getNotesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize Default App Settings if none exist
        viewModelScope.launch(Dispatchers.IO) {
            if (appDao.getAppSettings() == null) {
                appDao.saveAppSettings(AppSettings(firstRunCompleted = false))
            }
        }
    }

    fun makePhoneCall(phoneNumber: String) {
        actionEngine.execute(
            CustomAction(title = "Phone Call", icon = "📞", actionType = "CALL", payload = phoneNumber),
            appSettings.value?.testModeEnabled ?: false
        )
    }

    fun openNetworkSettings() {
        actionEngine.execute(
            CustomAction(title = "Network & SIM", icon = "🌐", actionType = "OPEN_SETTINGS", payload = "SIM_MANAGER"),
            appSettings.value?.testModeEnabled ?: false
        )
    }

    // --- DB Modification Methods ---

    fun saveProfile(name: String, language: String, homeAddr: String?, photoUri: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.saveUserProfile(
                UserProfile(name = name, preferredLanguage = language, homeAddress = homeAddr, photoUri = photoUri)
            )
            // Automatically add "GO HOME" custom action if home address exists
            if (!homeAddr.isNullOrBlank()) {
                val existing = appDao.getCustomActions().find { it.actionType == "NAVIGATE" && it.title.uppercase().contains("HOME") }
                if (existing == null) {
                    appDao.insertCustomAction(
                        CustomAction(
                            title = "GO HOME",
                            icon = "🏠",
                            actionType = "NAVIGATE",
                            payload = homeAddr,
                            orderIndex = 2
                        )
                    )
                }
            }
        }
    }

    fun setFirstRunCompleted(completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = appDao.getAppSettings() ?: AppSettings()
            appDao.saveAppSettings(settings.copy(firstRunCompleted = completed))
        }
    }

    fun toggleTestMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = appDao.getAppSettings() ?: AppSettings()
            val newSettings = settings.copy(testModeEnabled = !settings.testModeEnabled)
            appDao.saveAppSettings(newSettings)
            viewModelScope.launch(Dispatchers.Main) {
                val mode = if (newSettings.testModeEnabled) "ENABLED (Mocking operations)" else "DISABLED (Real Intents)"
                showToast("Test Mode: $mode")
            }
        }
    }

    fun updateCaregiverPasscode(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = appDao.getAppSettings() ?: AppSettings()
            appDao.saveAppSettings(settings.copy(caregiverPasscode = code))
        }
    }

    fun updateDefaultCabApp(app: String, packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = appDao.getAppSettings() ?: AppSettings()
            appDao.saveAppSettings(settings.copy(defaultCabApp = app, defaultCabAppPackage = packageName))
        }
    }

    fun updateCardOrder(newOrderKeys: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = appDao.getAppSettings() ?: AppSettings()
            appDao.saveAppSettings(settings.copy(cardOrderJson = newOrderKeys.joinToString(",")))
        }
    }

    fun addEmergencyContact(name: String, rel: String, phone: String, priority: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.insertEmergencyContact(
                EmergencyContact(name = name, relationship = rel, phoneNumber = phone, priority = priority)
            )
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deleteEmergencyContact(contact)
        }
    }

    fun addHospital(name: String, address: String, phone: String, ambulance: String, emergency: String) {
        addPlace(
            name = name,
            type = "HOSPITAL",
            address = address,
            phone = phone,
            notes = "Ambulance: $ambulance",
            isEmergency = true
        )
    }

    fun deleteHospital(hospital: Hospital) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deletePlace(
                PlaceEntity(
                    id = hospital.id,
                    name = hospital.name,
                    type = "HOSPITAL",
                    address = hospital.address
                )
            )
        }
    }

    fun updateHospital(hospital: Hospital) {
        updatePlace(
            PlaceEntity(
                id = hospital.id,
                name = hospital.name,
                type = "HOSPITAL",
                address = hospital.address,
                phoneNumber = hospital.phoneNumber,
                notes = "Ambulance: ${hospital.ambulanceNumber}",
                isEmergency = true
            )
        )
    }

    fun addSavedLocation(label: String, address: String) {
        addPlace(
            name = label,
            type = "OTHER",
            address = address,
            isEmergency = false
        )
    }

    fun deleteSavedLocation(loc: SavedLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deletePlace(
                PlaceEntity(
                    id = loc.id,
                    name = loc.label,
                    type = "OTHER",
                    address = loc.address
                )
            )
        }
    }

    fun addPlace(name: String, type: String, address: String, phone: String? = null, notes: String? = null, isEmergency: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.insertPlace(
                PlaceEntity(
                    name = name,
                    type = type,
                    address = address,
                    phoneNumber = phone,
                    notes = notes,
                    isEmergency = isEmergency
                )
            )
            // Add Hospital/Custom Action if it's a Hospital or Home or Doctor
            if (type == "HOSPITAL" || type == "HOME" || type == "DOCTOR") {
                val actionTitle = when (type) {
                    "HOME" -> "🏠 GO HOME"
                    "HOSPITAL" -> "🏥 GO TO $name"
                    else -> "🩺 GO TO $name"
                }
                val icon = when (type) {
                    "HOME" -> "🏠"
                    "HOSPITAL" -> "🏥"
                    else -> "🩺"
                }
                val existing = appDao.getCustomActions().find { it.actionType == "NAVIGATE" && it.title == actionTitle }
                if (existing == null) {
                    appDao.insertCustomAction(
                        CustomAction(
                            title = actionTitle,
                            icon = icon,
                            actionType = "NAVIGATE",
                            payload = address,
                            orderIndex = 3
                        )
                    )
                }
            }
        }
    }

    fun updatePlace(place: PlaceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.updatePlace(place)
            // Proactively update any custom actions that navigate to this place
            val actions = appDao.getCustomActions()
            actions.forEach { action ->
                if (action.actionType == "NAVIGATE" && (action.title.contains(place.name, ignoreCase = true) || (place.type == "HOME" && action.title.contains("HOME", ignoreCase = true)))) {
                    appDao.updateCustomAction(action.copy(payload = place.address))
                }
            }
        }
    }

    fun deletePlace(place: PlaceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deletePlace(place)
        }
    }

    fun addCustomAction(title: String, icon: String, type: String, payload: String, payloadExtra: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = appDao.getCustomActions().size
            appDao.insertCustomAction(
                CustomAction(
                    title = title,
                    icon = icon,
                    actionType = type,
                    payload = payload,
                    payloadExtra = payloadExtra,
                    orderIndex = count
                )
            )
        }
    }

    fun deleteCustomAction(action: CustomAction) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deleteCustomAction(action)
        }
    }

    fun updateCustomAction(action: CustomAction) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.updateCustomAction(action)
        }
    }

    fun updateEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.insertEmergencyContact(contact)
        }
    }

    fun clearCustomActions() {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.clearCustomActions()
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.clearUserProfile()
            appDao.clearEmergencyContacts()
            appDao.clearPlaces()
            appDao.clearCustomActions()
            appDao.clearDocuments()
            appDao.clearNotes()
            appDao.saveAppSettings(AppSettings(firstRunCompleted = false))
        }
    }

    fun addDocument(title: String, fileUri: String, fileType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.insertDocument(DocumentEntity(title = title, fileUri = fileUri, fileType = fileType))
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deleteDocument(doc)
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.insertNote(NoteEntity(title = title, content = content))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deleteNote(note)
        }
    }

    fun populateDefaultActions() {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.clearCustomActions()
            
            val defaults = listOf(
                CustomAction(title = "FLASHLIGHT", icon = "🔦", actionType = "FLASHLIGHT", payload = "", orderIndex = 0),
                CustomAction(title = "PHONE SETTINGS", icon = "⚙️", actionType = "OPEN_SETTINGS", payload = "SYSTEM", orderIndex = 1),
                CustomAction(title = "CALL DAUGHTER", icon = "📞", actionType = "CALL", payload = "+919876543210", orderIndex = 2),
                CustomAction(title = "OPEN WHATSAPP", icon = "💬", actionType = "OPEN_APP", payload = "com.whatsapp", orderIndex = 3),
                CustomAction(title = "OPEN YOUTUBE", icon = "📺", actionType = "OPEN_APP", payload = "com.google.android.youtube", orderIndex = 4)
            )
            for (action in defaults) {
                appDao.insertCustomAction(action)
            }
        }
    }

    // --- Action Engine Execution Layer ---

    fun executeAction(action: CustomAction) {
        val testMode = appSettings.value?.testModeEnabled ?: false
        actionEngine.execute(action, testMode)
    }

    fun launchCabWorkflow(destinationAddress: String, cabApp: String, cabPackage: String) {
        val settings = appSettings.value ?: AppSettings()
        workflowEngine.launchCabApp(
            destinationAddress = destinationAddress,
            cabApp = cabApp,
            cabPackage = cabPackage,
            vehicleType = "Cab",
            isTestMode = settings.testModeEnabled
        )
    }

    fun triggerEmergencyWorkflow() {
        val settings = appSettings.value ?: AppSettings()
        val contacts = emergencyContacts.value
        workflowEngine.triggerEmergencyLocationShare(contacts, settings.testModeEnabled)
    }

    // --- Speech Voice Matcher ---

    fun handleVoiceCommand(spokenText: String) {
        voiceTextResult = spokenText
        when (val result = com.seniorease.app.engine.VoiceMatcher.matchCommand(spokenText, customActions.value)) {
            is com.seniorease.app.engine.MatchedResult.Emergency -> {
                showToast("Emergency matched!")
                triggerEmergencyWorkflow()
            }
            is com.seniorease.app.engine.MatchedResult.SystemFlashlight -> {
                actionEngine.toggleFlashlight()
            }
            is com.seniorease.app.engine.MatchedResult.CabWorkflow -> {
                val settings = appSettings.value ?: AppSettings()
                val dest = places.value.firstOrNull { it.type == "HOME" || it.type == "OTHER" }?.address ?: userProfile.value?.homeAddress
                if (dest != null) {
                    showToast("Booking cab to $dest")
                    launchCabWorkflow(dest, settings.defaultCabApp, settings.defaultCabAppPackage)
                } else {
                    showToast("No destination configured for cab booking.")
                }
            }
            is com.seniorease.app.engine.MatchedResult.ActionMatch -> {
                val action = result.action
                showToast("Voice matched: ${action.title}")
                executeAction(action)
            }
            is com.seniorease.app.engine.MatchedResult.NoMatch -> {
                showToast("No action found matching: \"$spokenText\"")
            }
        }
    }

    // --- Action & Workflow Engine Listeners ---

    override fun onShowToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            toastMessage = message
        }
    }

    override fun onAppNotInstalled(appName: String, packageName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            appNotInstalledState = appName to packageName
        }
    }

    override fun onNavigateFailed(address: String) {
        viewModelScope.launch(Dispatchers.Main) {
            toastMessage = "We couldn't open Maps. Make sure a map application is installed."
        }
    }

    override fun onFlashlightStateChanged(isOn: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            isFlashlightActive = isOn
        }
    }

    override fun onMockExecution(actionType: String, message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            lastMockLog = message
            toastMessage = "[TEST MODE] $message"
        }
    }

    override fun onLocationFetched(location: Location?, mapUrl: String) {
        viewModelScope.launch(Dispatchers.Main) {
            toastMessage = if (location != null) {
                "Location found! Sharing..."
            } else {
                "GPS weak. Sharing coordinates wrapper link..."
            }
        }
    }

    override fun onCabLaunchSuccess(appName: String, intentUri: String) {
        viewModelScope.launch(Dispatchers.Main) {
            toastMessage = "Opened $appName for booking"
        }
    }

    override fun onCabLaunchFailed(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            toastMessage = message
        }
    }

    override fun onMockWorkflowTriggered(workflowName: String, data: String) {
        viewModelScope.launch(Dispatchers.Main) {
            lastMockLog = "WORKFLOW $workflowName: $data"
            toastMessage = "[TEST MODE] Workflow $workflowName triggered: $data"
        }
    }

    fun showToast(message: String) {
        onShowToast(message)
    }

    // Helper helper toast dismissal
    fun clearToast() {
        toastMessage = null
    }

    fun clearMockLog() {
        lastMockLog = null
    }
}
