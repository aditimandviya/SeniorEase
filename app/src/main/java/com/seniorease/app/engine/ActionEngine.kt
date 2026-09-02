package com.seniorease.app.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.Settings
import com.seniorease.app.data.CustomAction

class ActionEngine(private val context: Context) {

    interface ActionListener {
        fun onShowToast(message: String)
        fun onAppNotInstalled(appName: String, packageName: String)
        fun onNavigateFailed(address: String)
        fun onFlashlightStateChanged(isOn: Boolean)
        fun onMockExecution(actionType: String, message: String)
    }

    private var listener: ActionListener? = null

    fun setListener(listener: ActionListener) {
        this.listener = listener
    }

    private var isFlashlightOn = false

    fun toggleFlashlight() {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                isFlashlightOn = !isFlashlightOn
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
                listener?.onFlashlightStateChanged(isFlashlightOn)
                if (isFlashlightOn) {
                    listener?.onShowToast("Flashlight turned ON")
                } else {
                    listener?.onShowToast("Flashlight turned OFF")
                }
            } else {
                listener?.onShowToast("Flashlight not available on this device")
            }
        } catch (e: Exception) {
            listener?.onShowToast("Couldn't toggle flashlight")
        }
    }

    fun execute(action: CustomAction, isTestMode: Boolean) {
        if (isTestMode) {
            val mockMsg = when (action.actionType) {
                "CALL" -> "Mock call to: ${action.title} (${action.payload})"
                "OPEN_APP" -> "Mock opening app: ${action.title} (${action.payload})"
                "NAVIGATE" -> "Mock navigation to: ${action.payload}"
                "OPEN_SETTINGS" -> "Mock opening settings: ${action.payload}"
                "FLASHLIGHT" -> "Mock toggling flashlight"
                "SEND_MESSAGE" -> "Mock sending message to: ${action.payload} -> ${action.payloadExtra}"
                "OPEN_MAP" -> "Mock opening map: ${action.payload}"
                else -> "Mock action execution for: ${action.title}"
            }
            listener?.onMockExecution(action.actionType, mockMsg)
            return
        }

        when (action.actionType.uppercase()) {
            "CALL" -> {
                makeCall(action.payload)
            }
            "OPEN_APP" -> {
                openApp(action.title, action.payload)
            }
            "NAVIGATE" -> {
                navigate(action.payload)
            }
            "OPEN_SETTINGS" -> {
                openSettings(action.payload)
            }
            "FLASHLIGHT" -> {
                toggleFlashlight()
            }
            "OPEN_MAP" -> {
                openMap(action.payload)
            }
            "SEND_MESSAGE" -> {
                sendMessage(action.payload, action.payloadExtra ?: "")
            }
            else -> {
                listener?.onShowToast("Unknown action type: ${action.actionType}")
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent)
            } else {
                // Fall back to ACTION_DIAL which does not require permission
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } catch (e: Exception) {
            listener?.onShowToast("We couldn't make the call. Opening dialer...")
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (ex: Exception) {
                listener?.onShowToast("Could not open dialer.")
            }
        }
    }

    private fun openApp(appName: String, packageName: String) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                listener?.onAppNotInstalled(appName, packageName)
            }
        } catch (e: Exception) {
            listener?.onAppNotInstalled(appName, packageName)
        }
    }

    private fun navigate(address: String) {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Try opening map generally without specifying package
                val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(genericIntent)
            }
        } catch (e: Exception) {
            listener?.onNavigateFailed(address)
        }
    }

    private fun openMap(address: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            listener?.onNavigateFailed(address)
        }
    }

    private fun openSettings(settingType: String) {
        val type = settingType.uppercase()
        if (type in listOf("SIM", "SIM_MANAGER", "MOBILE_DATA", "NETWORK", "CONNECTIONS")) {
            val intents = listOf(
                Intent("android.settings.SIM_CARD_SETTINGS"),
                Intent().setClassName("com.android.settings", "com.android.settings.Settings\$SimSettingsActivity"),
                Intent().setClassName("com.android.settings", "com.android.settings.Settings\$MobileNetworkListActivity"),
                Intent().setClassName("com.android.settings", "com.android.settings.network.telephony.MobileNetworkActivity"),
                Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
                Intent("android.settings.MOBILE_DATA_SETTINGS"),
                Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
                Intent().setClassName("com.android.settings", "com.android.settings.Settings\$MobileNetworkActivity"),
                Intent("android.settings.NETWORK_PROVIDER_SETTINGS"),
                Intent(Settings.ACTION_WIRELESS_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
            for (intent in intents) {
                try {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    // Try next intent in chain
                }
            }
            listener?.onShowToast("We couldn't open system network settings.")
            return
        }

        val action = when (type) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "SOUND", "VOLUME" -> Settings.ACTION_SOUND_SETTINGS
            "DISPLAY" -> Settings.ACTION_DISPLAY_SETTINGS
            "ACCESSIBILITY" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "BATTERY" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "AIRPLANE" -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                listener?.onShowToast("We couldn't open system settings.")
            }
        }
    }

    private fun sendMessage(phoneNumber: String, messageText: String) {
        try {
            val uri = Uri.parse("smsto:$phoneNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            listener?.onShowToast("We couldn't open messaging.")
        }
    }
}
