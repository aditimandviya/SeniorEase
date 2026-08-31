package com.seniorease.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts ORDER BY priority ASC")
    fun getEmergencyContactsFlow(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts ORDER BY priority ASC")
    suspend fun getEmergencyContacts(): List<EmergencyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContact): Long

    @Update
    suspend fun updateEmergencyContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteEmergencyContact(contact: EmergencyContact)

    @Query("DELETE FROM emergency_contacts")
    suspend fun clearEmergencyContacts()

    // Unified Places
    @Query("SELECT * FROM places ORDER BY id DESC")
    fun getPlacesFlow(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places ORDER BY id DESC")
    suspend fun getPlaces(): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity): Long

    @Update
    suspend fun updatePlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM places")
    suspend fun clearPlaces()

    // Custom Actions
    @Query("SELECT * FROM custom_actions ORDER BY orderIndex ASC")
    fun getCustomActionsFlow(): Flow<List<CustomAction>>

    @Query("SELECT * FROM custom_actions ORDER BY orderIndex ASC")
    suspend fun getCustomActions(): List<CustomAction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAction(action: CustomAction): Long

    @Update
    suspend fun updateCustomAction(action: CustomAction)

    @Delete
    suspend fun deleteCustomAction(action: CustomAction)

    @Query("DELETE FROM custom_actions")
    suspend fun clearCustomActions()

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppSettings(settings: AppSettings)

    // Documents
    @Query("SELECT * FROM documents ORDER BY dateAdded DESC")
    fun getDocumentsFlow(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity): Long

    @Delete
    suspend fun deleteDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents")
    suspend fun clearDocuments()

    // Notes
    @Query("SELECT * FROM notes ORDER BY dateCreated DESC")
    fun getNotesFlow(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clearNotes()
}
