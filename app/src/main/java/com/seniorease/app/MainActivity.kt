package com.seniorease.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.screens.*
import com.seniorease.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var pendingPermissionCallback: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            pendingPermissionCallback?.invoke()
        } else {
            Toast.makeText(this, "Permissions are required to run this action.", Toast.LENGTH_LONG).show()
        }
        pendingPermissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorEaseTheme {
                val settingsState by viewModel.appSettings.collectAsState()
                val userProfileState by viewModel.userProfile.collectAsState()
                var currentScreen by rememberSaveable { mutableStateOf("home") }

                var permissionExplanationText by remember { mutableStateOf<String?>(null) }
                var permissionExplanationTitle by remember { mutableStateOf("") }
                var permissionsToRequest by remember { mutableStateOf<Array<String>>(emptyArray()) }

                val firstRunCompleted = (settingsState?.firstRunCompleted == true) || (userProfileState != null && userProfileState?.name?.isNotBlank() == true)
                val activeScreen = if (!firstRunCompleted) "wizard" else currentScreen

                val toastMsg = viewModel.toastMessage
                LaunchedEffect(toastMsg) {
                    if (toastMsg != null) {
                        Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_LONG).show()
                        viewModel.clearToast()
                    }
                }

                val appNotInstalled = viewModel.appNotInstalledState
                if (appNotInstalled != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.appNotInstalledState = null },
                        title = {
                            Text(
                                text = "${appNotInstalled.first} Not Installed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = "We couldn't find ${appNotInstalled.first} on this phone. Caregiver can verify app installations.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SecondaryText
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.appNotInstalledState = null },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Text("OK", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    )
                }

                val checkAndRunAction: (Array<String>, String, String, () -> Unit) -> Unit = { permissions, title, explanation, action ->
                    val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
                    if (missing.isEmpty()) {
                        action()
                    } else {
                        permissionsToRequest = missing.toTypedArray()
                        permissionExplanationTitle = title
                        permissionExplanationText = explanation
                        pendingPermissionCallback = action
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (activeScreen) {
                        "wizard" -> {
                            WelcomeSetupWizard(
                                viewModel = viewModel,
                                onSetupComplete = { currentScreen = "home" }
                            )
                        }
                        "home" -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToEmergency = {
                                    checkAndRunAction(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.CALL_PHONE,
                                            Manifest.permission.SEND_SMS
                                        ),
                                        "🚨 Emergency Services Permission",
                                        "Location, calling, and text permissions are required to share GPS details with your family in emergencies.",
                                        { currentScreen = "emergency" }
                                    )
                                },
                                onNavigateToCab = {
                                    checkAndRunAction(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        "📍 Location Permission",
                                        "Location access is required so we can set your current location as the cab pickup address.",
                                        { currentScreen = "cab" }
                                    )
                                },
                                onNavigateToHospital = {
                                    currentScreen = "hospital"
                                },
                                onNavigateToCaregiver = {
                                    currentScreen = "caregiver"
                                },
                                 onNavigateToDocuments = {
                                    currentScreen = "documents"
                                },
                                onNavigateToCalls = {
                                    currentScreen = "calls"
                                }
                            )
                        }
                        "calls" -> {
                            com.seniorease.app.ui.screens.PhoneCallsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                        "emergency" -> {
                            EmergencyScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                        "cab" -> {
                            CabWorkflowScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                        "hospital" -> {
                            HospitalScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" },
                                onBookCabClick = {
                                    checkAndRunAction(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        "📍 Location Permission",
                                        "Location access is required so we can set your current location as the cab pickup address.",
                                        { currentScreen = "cab" }
                                    )
                                }
                            )
                        }
                        "caregiver" -> {
                            CaregiverSettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                        "documents" -> {
                            DocumentsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                    }
                }

                if (permissionExplanationText != null) {
                    AlertDialog(
                        onDismissRequest = {
                            permissionExplanationText = null
                            pendingPermissionCallback = null
                        },
                        title = {
                            Text(
                                text = permissionExplanationTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Text(
                                text = permissionExplanationText!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SecondaryText,
                                textAlign = TextAlign.Center
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(permissionsToRequest)
                                    permissionExplanationText = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Text("ALLOW ACCESS", style = MaterialTheme.typography.labelLarge)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    permissionExplanationText = null
                                    pendingPermissionCallback = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(top = 8.dp),
                                border = BorderStroke(2.dp, BorderMedium)
                            ) {
                                Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                            }
                        }
                    )
                }
            }
        }
    }
}
