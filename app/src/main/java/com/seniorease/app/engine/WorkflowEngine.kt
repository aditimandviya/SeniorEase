package com.seniorease.app.engine

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import com.seniorease.app.data.EmergencyContact

class WorkflowEngine(private val context: Context) {

    interface WorkflowListener {
        fun onLocationFetched(location: Location?, mapUrl: String)
        fun onCabLaunchSuccess(appName: String, intentUri: String)
        fun onCabLaunchFailed(message: String)
        fun onShowToast(message: String)
        fun onMockWorkflowTriggered(workflowName: String, data: String)
    }

    private var listener: WorkflowListener? = null
    private val locationHelper = LocationHelper(context)

    fun setListener(listener: WorkflowListener) {
        this.listener = listener
    }

    fun launchCabApp(
        destinationAddress: String,
        cabApp: String,
        cabPackage: String,
        vehicleType: String,
        isTestMode: Boolean
    ) {
        if (isTestMode) {
            listener?.onMockWorkflowTriggered(
                "BOOK_CAB",
                "App: $cabApp ($cabPackage), Dest: $destinationAddress, Vehicle: $vehicleType"
            )
            return
        }

        locationHelper.getCurrentLocation { location ->
            val pm = context.packageManager
            var resolvedIntent: Intent? = null
            var selectedAppName = cabApp

            // Copy destination to clipboard to help seniors paste it
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("destination", destinationAddress)
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                // Ignore clipboard failure
            }

            when (cabPackage) {
                "com.ubercab" -> {
                    val uberUri = "uber://?action=setPickup&pickup=my_location&dropoff[address]=${Uri.encode(destinationAddress)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uberUri)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(pm) != null) {
                        resolvedIntent = intent
                    } else {
                        val webUberUri = "https://m.uber.com/ul/?action=setPickup&pickup=my_location&dropoff[formatted_address]=${Uri.encode(destinationAddress)}"
                        resolvedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUberUri)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        selectedAppName = "Uber Web"
                    }
                }
                "com.olacabs.customer" -> {
                    val olaUri = "olacabs://app/launch?dropoff_address=${Uri.encode(destinationAddress)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(olaUri)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(pm) != null) {
                        resolvedIntent = intent
                    } else {
                        selectedAppName = "Ola (Fallback Search)"
                        val searchUri = "https://www.google.com/search?q=book+ola+cab+to+${Uri.encode(destinationAddress)}"
                        resolvedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUri)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                }
                "com.google.android.apps.maps" -> {
                    val mapUri = "geo:0,0?q=${Uri.encode(destinationAddress)}"
                    resolvedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUri)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                else -> {
                    val intent = pm.getLaunchIntentForPackage(cabPackage)
                    if (intent != null) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        resolvedIntent = intent
                        listener?.onShowToast("Opening $cabApp. Destination address copied to clipboard! Paste it inside the search bar.")
                    } else {
                        selectedAppName = "$cabApp (Search Fallback)"
                        val searchUri = "https://www.google.com/search?q=book+$cabApp+cab+to+${Uri.encode(destinationAddress)}"
                        resolvedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUri)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                }
            }

            try {
                if (resolvedIntent != null) {
                    context.startActivity(resolvedIntent)
                    listener?.onCabLaunchSuccess(selectedAppName, resolvedIntent.dataString ?: "")
                } else {
                    listener?.onCabLaunchFailed("Could not resolve any application to book a cab.")
                }
            } catch (e: Exception) {
                listener?.onCabLaunchFailed("Error launching cab app: ${e.message}")
            }
        }
    }

    fun triggerEmergencyLocationShare(contacts: List<EmergencyContact>, isTestMode: Boolean) {
        if (isTestMode) {
            listener?.onMockWorkflowTriggered(
                "EMERGENCY_LOCATION",
                "Contacts: ${contacts.joinToString { it.name + " (" + it.phoneNumber + ")" }}"
            )
            return
        }

        locationHelper.getCurrentLocation { location ->
            val locationUrl = if (location != null) {
                "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "Unknown location (GPS signal weak)"
            }
            
            val message = "I may need help. My current location is: $locationUrl"
            listener?.onLocationFetched(location, locationUrl)

            if (contacts.isEmpty()) {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Location").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (e: Exception) {
                    listener?.onShowToast("Unable to open sharing tools.")
                }
            } else {
                val primaryContact = contacts.first()
                try {
                    val uri = Uri.parse("smsto:${primaryContact.phoneNumber}")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                        putExtra("sms_body", message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(smsIntent)
                    listener?.onShowToast("Opening SMS to send location to ${primaryContact.name}")
                } catch (e: Exception) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Location").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
        }
    }
}
