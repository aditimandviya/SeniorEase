package com.seniorease.app.engine

import com.seniorease.app.data.CustomAction

object VoiceMatcher {
    fun matchCommand(spokenText: String, actions: List<CustomAction>): MatchedResult {
        val query = spokenText.lowercase().trim()

        // 1. Check for Emergency or Help commands
        if (query.contains("emergency") || query.contains("help")) {
            return MatchedResult.Emergency
        }

        // 2. Check for Flashlight control
        if (query.contains("flashlight") || query.contains("light")) {
            val flashlightAction = actions.find { it.actionType.uppercase() == "FLASHLIGHT" }
            return if (flashlightAction != null) {
                MatchedResult.ActionMatch(flashlightAction)
            } else {
                MatchedResult.SystemFlashlight
            }
        }

        // 3. Check for Cab Booking
        if (query.contains("cab") || query.contains("taxi")) {
            return MatchedResult.CabWorkflow
        }

        // 4. Match configured Custom Actions
        val matched = actions.find { action ->
            val titleLower = action.title.lowercase()
            query.contains(titleLower) ||
            (action.actionType.uppercase() == "CALL" && query.contains("call") && query.contains(titleLower.replace("call", "").trim())) ||
            (action.actionType.uppercase() == "OPEN_APP" && query.contains("open") && query.contains(titleLower.replace("open", "").trim()))
        }

        return if (matched != null) {
            MatchedResult.ActionMatch(matched)
        } else {
            MatchedResult.NoMatch(spokenText)
        }
    }
}

sealed class MatchedResult {
    object Emergency : MatchedResult()
    object SystemFlashlight : MatchedResult()
    object CabWorkflow : MatchedResult()
    data class ActionMatch(val action: CustomAction) : MatchedResult()
    data class NoMatch(val rawText: String) : MatchedResult()
}
