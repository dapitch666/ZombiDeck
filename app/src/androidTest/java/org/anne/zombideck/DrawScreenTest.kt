package org.anne.zombideck

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DrawScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()

        // Go to the draw screen
        composeTestRule.onNodeWithStringId(R.string.shuffle_and_start)
            .assertExists()
            .performClick()
    }

    @Test
    fun drawScreenLoadingTest() {
        composeTestRule.onRoot().printToLog("drawScreenLoadingTest")
        // Check that the buttons are disabled
        composeTestRule.onNodeWithStringId(R.string.see_abomination)
            .assertExists()
            .assertIsNotEnabled()
        composeTestRule.onNodeWithStringId(R.string.previous_card)
            .assertExists()
            .assertIsNotEnabled()

        // No card was drawn yet, so the card back should be visible
        composeTestRule.onNodeWithContentDescription("Card back")
            .assertExists()

        // Draw first card
        composeTestRule.onNodeWithStringId(R.string.draw_a_card)
            .assertExists()
            .assertIsEnabled()
            .performClick()

        // Card back should be replaced by the card front
        composeTestRule.onNodeWithContentDescription("Card back")
            .assertDoesNotExist()
            
        val walker = composeTestRule.activity.getString(R.string.walker).uppercase()
        val runner = composeTestRule.activity.getString(R.string.runner).uppercase()
        val fatty = composeTestRule.activity.getString(R.string.fatty).uppercase()
        val abomination = composeTestRule.activity.getString(R.string.abomination).uppercase()
        val trejo = composeTestRule.activity.getString(R.string.trejo).uppercase()

        composeTestRule.onNode(
            hasText(walker, substring = true)
                    or hasText(runner, substring = true)
                    or hasText(fatty, substring = true)
                    or hasText(abomination)
                    or hasText(trejo, substring = true)
        ).assertExists()
    }

    @Test
    fun testPreviousCardButton() {
        // Draw first card
        composeTestRule.onNodeWithStringId(R.string.draw_a_card)
            .assertExists()
            .assertIsEnabled()
            .performClick()

        // The previous card button should be disabled
        composeTestRule.onNodeWithStringId(R.string.previous_card)
            .assertExists()
            .assertIsNotEnabled()

        // Draw second card
        composeTestRule.onNodeWithStringId(R.string.draw_a_card)
            .performClick()

        // The previous card button should be enabled. Click on it.
        composeTestRule.onNodeWithStringId(R.string.previous_card)
            .assertExists()
            .assertIsEnabled()
            .performClick()

        // The previous card button should be disabled again
        composeTestRule.onNodeWithStringId(R.string.previous_card)
            .assertIsNotEnabled()
    }

    @Test
    fun testSoundButton() {
        composeTestRule.onNodeWithDescriptionStringId(R.string.toggle_sound_on_off)
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testDangerLevel() {
        val blue = composeTestRule.activity.getString(R.string.blue)
        val yellow = composeTestRule.activity.getString(R.string.yellow)
        val orange = composeTestRule.activity.getString(R.string.orange)
        val red = composeTestRule.activity.getString(R.string.red)
        
        composeTestRule.onNodeWithStringId(R.string.danger_level, blue)
            .assertExists()

        composeTestRule.onNodeWithDescriptionStringId(R.string.decrease_danger_level)
            .assertExists()
            .assertIsNotEnabled()

        composeTestRule.onNodeWithDescriptionStringId(R.string.increase_danger_level)
            .assertExists()
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithStringId(R.string.danger_level, yellow)
            .assertExists()

        composeTestRule.onNodeWithDescriptionStringId(R.string.increase_danger_level)
            .performClick()

        composeTestRule.onNodeWithStringId(R.string.danger_level, orange)
            .assertExists()

        composeTestRule.onNodeWithDescriptionStringId(R.string.increase_danger_level)
            .performClick()

        composeTestRule.onNodeWithStringId(R.string.danger_level, red)
            .assertExists()

        composeTestRule.onNodeWithDescriptionStringId(R.string.increase_danger_level)
            .assertIsNotEnabled()

        composeTestRule.onNodeWithDescriptionStringId(R.string.decrease_danger_level)
            .assertExists()
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithStringId(R.string.danger_level, orange)
            .assertExists()
    }
}