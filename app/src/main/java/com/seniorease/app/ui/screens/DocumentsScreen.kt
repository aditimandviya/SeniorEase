@file:OptIn(ExperimentalMaterial3Api::class)
package com.seniorease.app.ui.screens

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.seniorease.app.data.DocumentEntity
import com.seniorease.app.data.NoteEntity
import com.seniorease.app.ui.MainViewModel
import com.seniorease.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DocumentsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val notes by viewModel.notes.collectAsState()

    androidx.activity.compose.BackHandler {
        onNavigateBack()
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Documents, 1: Notes
    var showDocTitleDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var newDocTitle by remember { mutableStateOf("") }

    var showNoteDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    var viewingImageDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var viewingNote by remember { mutableStateOf<NoteEntity?>(null) }

    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            showDocTitleDialog = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(60.dp)
                        .background(CardBackground, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = PrimaryText,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "📁 DOCUMENTS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentBlue
                )
            }

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TabButton(
                    text = "📄 UPLOADS",
                    isSelected = activeTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 0 }
                )
                TabButton(
                    text = "✍️ NOTES",
                    isSelected = activeTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 1 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == 0) {
                    // Documents Upload Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { fileLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UPLOAD FILE OR PHOTO", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (documents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No uploaded documents.\nTap above to upload IDs, prescriptions or reports.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(documents) { doc ->
                                    DocumentCard(
                                        doc = doc,
                                        onView = {
                                            if (doc.fileType.startsWith("IMAGE") || doc.fileUri.endsWith(".jpg") || doc.fileUri.endsWith(".png") || doc.fileUri.endsWith(".jpeg")) {
                                                viewingImageDoc = doc
                                            } else {
                                                // Launch generic system viewer for PDF or others via FileProvider content:// URI
                                                try {
                                                    val file = java.io.File(doc.fileUri)
                                                    val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        "com.seniorease.app.fileprovider",
                                                        file
                                                    )
                                                    val mime = context.contentResolver.getType(contentUri) ?: "application/pdf"
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                        setDataAndType(contentUri, mime)
                                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    viewModel.showToast("No application installed to open this file type.")
                                                }
                                            }
                                        },
                                        onDelete = { viewModel.deleteDocument(doc) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Notes tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { showNoteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CREATE INSTRUCTION NOTE", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (notes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No notes created.\nCreate emergency directions, prescriptions list, or medical logs.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(notes) { note ->
                                    NoteCard(
                                        note = note,
                                        onView = { viewingNote = note },
                                        onDelete = { viewModel.deleteNote(note) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Document Title Dialog
    if (showDocTitleDialog) {
        AlertDialog(
            onDismissRequest = { showDocTitleDialog = false },
            title = { Text("Set Document Title", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newDocTitle,
                    onValueChange = { newDocTitle = it },
                    label = { Text("e.g. Aadhaar Card, Prescription") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedFileUri
                        if (newDocTitle.isNotBlank() && uri != null) {
                            val savedFile = saveFileToInternalStorage(context, uri)
                            if (savedFile != null) {
                                val mimeType = context.contentResolver.getType(uri) ?: "IMAGE"
                                val type = if (mimeType.startsWith("image")) "IMAGE" else "PDF"
                                viewModel.addDocument(newDocTitle, savedFile.absolutePath, type)
                                newDocTitle = ""
                                selectedFileUri = null
                                showDocTitleDialog = false
                            } else {
                                viewModel.showToast("Failed to copy file to app storage.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocTitleDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Create Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Write Note / Instructions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Title (e.g. Medical List)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Write your notes here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank() && newNoteContent.isNotBlank()) {
                            viewModel.addNote(newNoteTitle, newNoteContent)
                            newNoteTitle = ""
                            newNoteContent = ""
                            showNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("SAVE NOTE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Full Screen Image Viewer Dialog
    if (viewingImageDoc != null) {
        Dialog(onDismissRequest = { viewingImageDoc = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                border = BorderStroke(2.dp, BorderMedium)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewingImageDoc?.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewingImageDoc = null }) {
                            Text("CLOSE", fontWeight = FontWeight.Bold, color = AccentBlue)
                        }
                    }

                                        Spacer(modifier = Modifier.height(16.dp))

                    val bitmap = remember(viewingImageDoc?.fileUri) {
                        viewingImageDoc?.fileUri?.let { path ->
                            try {
                                BitmapFactory.decodeFile(path)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Document image preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        )
                    } else {
                        Text("Could not load image preview.", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Accessible Note Reader Dialog
    if (viewingNote != null) {
        Dialog(onDismissRequest = { viewingNote = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                border = BorderStroke(3.dp, AccentBlue)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewingNote?.title ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewingNote = null }) {
                            Text("CLOSE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        }
                    }

                    Divider(color = BorderMedium, modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = viewingNote?.content ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, lineHeight = 30.sp),
                        color = PrimaryText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentBlue else CardBackground
        ),
        border = BorderStroke(2.dp, if (isSelected) AccentBlue else BorderMedium),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else PrimaryText
        )
    }
}

@Composable
fun DocumentCard(
    doc: DocumentEntity,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(doc.dateAdded) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(doc.dateAdded))
    }

    Card(
        onClick = onView,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, BorderMedium),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (doc.fileType == "IMAGE") "🖼️" else "📄",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = EmergencyRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(note.dateCreated) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(note.dateCreated))
    }

    Card(
        onClick = onView,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, BorderMedium),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "📝",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$dateString - ${note.content}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = EmergencyRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Helper function to safely copy files to internal storage to ensure offline access
private fun saveFileToInternalStorage(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val mimeType = context.contentResolver.getType(uri)
        val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "bin"
        val file = File(context.filesDir, "doc_${System.currentTimeMillis()}.$extension")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        null
    }
}
