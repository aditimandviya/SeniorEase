package com.seniorease.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val preferredLanguage: String = "English",
    val photoUri: String? = null,
    val homeAddress: String? = null,
    val workAddress: String? = null,
    val otherSavedPlaces: String? = null
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val priority: Int
)

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // HOME, HOSPITAL, DOCTOR, OTHER
    val address: String,
    val phoneNumber: String? = null,
    val notes: String? = null,
    val isEmergency: Boolean = false
)

@Entity(tableName = "custom_actions")
data class CustomAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String, // Store icon name or raw emoji (e.g., "📞", "🚕")
    val actionType: String, // CALL, OPEN_APP, OPEN_MAP, NAVIGATE, SEND_MESSAGE, OPEN_SETTINGS, FLASHLIGHT, EMERGENCY, CUSTOM_WORKFLOW
    val payload: String, // target parameter (phone number, website url, package name, address, etc.)
    val payloadExtra: String? = null,
    val enabled: Boolean = true,
    val pinned: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val caregiverPasscode: String = "1234",
    val testModeEnabled: Boolean = false,
    val defaultCabApp: String = "Uber",
    val defaultCabAppPackage: String = "com.ubercab",
    val firstRunCompleted: Boolean = false,
    val cardOrderJson: String = "EMERGENCY,CALLS,HOSPITAL,CAB,DOCUMENTS"
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileUri: String,
    val fileType: String, // "IMAGE", "PDF", "OTHER"
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis()
)

data class Hospital(
    val id: Long = 0,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val ambulanceNumber: String,
    val emergencyNumber: String = ""
)

data class SavedLocation(
    val id: Long = 0,
    val label: String,
    val address: String
)
