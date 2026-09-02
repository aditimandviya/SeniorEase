@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorease.app.data.CustomAction
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToEmergency: () -> Unit,
    onNavigateToCab: () -> Unit,
    onNavigateToHospital: () -> Unit,
    onNavigateToCaregiver: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToCalls: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val actions by viewModel.customActions.collectAsState()

    var showPasscodeDialog by remember { mutableStateOf(false) }
    var passcodeText by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }

    // Time-based greeting helper
    val greeting = remember {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "GOOD MORNING"
            in 12..16 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
    val username = userProfile?.name?.uppercase() ?: "USER"

    // Speech Recognizer setup
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var voiceStateText by remember { mutableStateOf("Listening... Speak now.") }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Test Mode Alert Header
                if (appSettings?.testModeEnabled == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentAmber)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️ TEST MODE ACTIVE (Simulated Operations)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Greeting & Settings Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = PrimaryText
                        )
                    }
                    // Lock button for caregiver mode
                    IconButton(
                        onClick = { showPasscodeDialog = true },
                        modifier = Modifier
                            .size(60.dp)
                            .background(BorderLight, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Caregiver Settings",
                            tint = AccentBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Speech activation microphone button
            FloatingActionButton(
                onClick = {
                    showVoiceOverlay = true
                    voiceStateText = "Listening... Speak now."
                    speechRecognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {
                            voiceStateText = "Listening..."
                        }
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            voiceStateText = "Processing speech..."
                        }
                        override fun onError(error: Int) {
                            voiceStateText = "Sorry, couldn't hear you. Please tap and try again."
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val spoken = matches[0]
                                viewModel.handleVoiceCommand(spoken)
                                showVoiceOverlay = false
                            } else {
                                voiceStateText = "No command recognized."
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    speechRecognizer.startListening(speechIntent)
                },
                containerColor = AccentBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input Command",
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            val cardOrderKeys = remember(appSettings?.cardOrderJson, actions) {
                val rawOrder = (appSettings?.cardOrderJson ?: "EMERGENCY,CALLS,HOSPITAL,CAB,DOCUMENTS")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toMutableList()

                actions.forEach { customAction ->
                    val customKey = "CUSTOM_${customAction.id}"
                    if (!rawOrder.contains(customKey) && !rawOrder.contains(customAction.title)) {
                        rawOrder.add(customKey)
                    }
                }
                rawOrder.distinct()
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cardOrderKeys) { key ->
                    when {
                        key == "EMERGENCY" -> {
                            Card(
                                onClick = onNavigateToEmergency,
                                colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(88.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "🚨",
                                        fontSize = 28.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = "EMERGENCY",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        key == "CALLS" -> {
                            Card(
                                onClick = onNavigateToCalls,
                                colors = CardDefaults.cardColors(containerColor = AccentGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📞", fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                                    Text(
                                        text = "PHONE CALLS",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        key == "HOSPITAL" -> {
                            Card(
                                onClick = onNavigateToHospital,
                                colors = CardDefaults.cardColors(containerColor = AccentTeal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏥", fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                                    Text(
                                        text = "GO TO HOSPITAL",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        key == "CAB" -> {
                            Card(
                                onClick = onNavigateToCab,
                                colors = CardDefaults.cardColors(containerColor = AccentBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🚕", fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                                    Text(
                                        text = "BOOK A CAB",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        key == "DOCUMENTS" -> {
                            Card(
                                onClick = onNavigateToDocuments,
                                colors = CardDefaults.cardColors(containerColor = AccentPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📁", fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                                    Text(
                                        text = "MY DOCUMENTS & NOTES",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        else -> {
                            // Find custom action matching key or custom ID
                            val customAction = actions.find { "CUSTOM_${it.id}" == key || it.title == key }
                            if (customAction != null) {
                                val isFlashlight = customAction.actionType == "FLASHLIGHT"
                                val isFlashlightOn = isFlashlight && viewModel.isFlashlightActive
                                val borderAccent = if (isFlashlightOn) AccentAmber else BorderMedium

                                Card(
                                    onClick = { viewModel.executeAction(customAction) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(2.dp, borderAccent),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(customAction.icon, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text(
                                            text = customAction.title.uppercase(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Empty space at bottom to prevent FAB covering item
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // Caregiver Mode Passcode Prompt Dialog
    if (showPasscodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasscodeDialog = false
                passcodeText = ""
                passcodeError = false
            },
            title = {
                Text(
                    "Caregiver Mode Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Please enter the caregiver passcode (Default: 1234).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = passcodeText,
                        onValueChange = {
                            passcodeText = it
                            passcodeError = false
                        },
                        textStyle = MaterialTheme.typography.headlineMedium,
                        singleLine = true,
                        isError = passcodeError,
                        modifier = Modifier.width(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderMedium
                        )
                    )
                    if (passcodeError) {
                        Text(
                            "Incorrect passcode",
                            color = EmergencyRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val configured = appSettings?.caregiverPasscode ?: "1234"
                        if (passcodeText == configured) {
                            showPasscodeDialog = false
                            passcodeText = ""
                            onNavigateToCaregiver()
                        } else {
                            passcodeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("UNLOCK", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPasscodeDialog = false
                        passcodeText = ""
                        passcodeError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(top = 8.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
                }
            }
        )
    }

    // Voice Overlay Panel
    if (showVoiceOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Voice Command",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = voiceStateText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            speechRecognizer.stopListening()
                            showVoiceOverlay = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text("CANCEL", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
