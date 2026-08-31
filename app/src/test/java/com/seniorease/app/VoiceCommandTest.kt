package com.seniorease.app

import com.seniorease.app.data.CustomAction
import com.seniorease.app.engine.MatchedResult
import com.seniorease.app.engine.VoiceMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandTest {

    private val sampleActions = listOf(
        CustomAction(id = 1, title = "Call Daughter", icon = "📞", actionType = "CALL", payload = "+919999999999"),
        CustomAction(id = 2, title = "Open WhatsApp", icon = "💬", actionType = "OPEN_APP", payload = "com.whatsapp"),
        CustomAction(id = 3, title = "My Doctor", icon = "👨‍⚕️", actionType = "CALL", payload = "+1234567890"),
        CustomAction(id = 4, title = "Flashlight", icon = "🔦", actionType = "FLASHLIGHT", payload = "")
    )

    @Test
    fun testEmergencyMatch() {
        val r1 = VoiceMatcher.matchCommand("I need help urgently", sampleActions)
        val r2 = VoiceMatcher.matchCommand("This is an emergency situation", sampleActions)
        
        assertTrue(r1 is MatchedResult.Emergency)
        assertTrue(r2 is MatchedResult.Emergency)
    }

    @Test
    fun testCabMatch() {
        val r1 = VoiceMatcher.matchCommand("book a cab", sampleActions)
        val r2 = VoiceMatcher.matchCommand("call a taxi", sampleActions)

        assertTrue(r1 is MatchedResult.CabWorkflow)
        assertTrue(r2 is MatchedResult.CabWorkflow)
    }

    @Test
    fun testFlashlightMatch() {
        val r1 = VoiceMatcher.matchCommand("turn on flashlight", sampleActions)
        val r2 = VoiceMatcher.matchCommand("need some light", sampleActions)

        // Since Flashlight action exists in actions list, it should match the action card
        assertTrue(r1 is MatchedResult.ActionMatch)
        assertEquals("Flashlight", (r1 as MatchedResult.ActionMatch).action.title)
        assertTrue(r2 is MatchedResult.ActionMatch)
    }

    @Test
    fun testCustomActionsMatch() {
        val r1 = VoiceMatcher.matchCommand("please call daughter", sampleActions)
        val r2 = VoiceMatcher.matchCommand("open whatsapp now", sampleActions)
        val r3 = VoiceMatcher.matchCommand("call my doctor", sampleActions)

        assertTrue(r1 is MatchedResult.ActionMatch)
        assertEquals("Call Daughter", (r1 as MatchedResult.ActionMatch).action.title)

        assertTrue(r2 is MatchedResult.ActionMatch)
        assertEquals("Open WhatsApp", (r2 as MatchedResult.ActionMatch).action.title)

        assertTrue(r3 is MatchedResult.ActionMatch)
        assertEquals("My Doctor", (r3 as MatchedResult.ActionMatch).action.title)
    }

    @Test
    fun testNoMatch() {
        val r = VoiceMatcher.matchCommand("what is the weather today", sampleActions)
        assertTrue(r is MatchedResult.NoMatch)
        assertEquals("what is the weather today", (r as MatchedResult.NoMatch).rawText)
    }
}
