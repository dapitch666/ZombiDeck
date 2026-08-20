package org.anne.zombideck

import androidx.activity.compose.setContent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.anne.zombideck.data.Expansion
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsScreenTest {
    companion object {
        private const val EASY_TAG  = "easy"
        private const val HARD_TAG  = "hard"
        private const val EXTRA_TAG = "extra"
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()

        // Clear all shared preferences so every test starts from a known baseline.
        PreferenceManager
            .getDefaultSharedPreferences(composeTestRule.activity)
            .edit()
            .clear()
            .commit()

        composeTestRule.activity.setContent {
            ZombiDeckApp(
                navController        = rememberNavController(),
                playAbominationSound = { },
                playShooterSound     = { },
            )
        }

        // Go to the settings screen
        composeTestRule.onNodeWithStringId(R.string.configure_the_deck)
            .assertExists()
            .performClick()
    }

    @Test
    fun testDialogOpensOnAllSwitchesOff() {
        // All three difficulty switches are on by default — turn them off one by one.
        composeTestRule.onNodeWithTag(EASY_TAG).assertIsOn().performClick().assertIsOff()
        composeTestRule.onNodeWithTag(HARD_TAG).assertIsOn().performClick().assertIsOff()
        composeTestRule.onNodeWithTag(EXTRA_TAG).assertIsOn().performClick().assertIsOff()

        // All off → dialog must appear.
        composeTestRule.onNodeWithStringId(R.string.preferences_alert)
            .assertExists()
            .assertIsDisplayed()

        // Dismissing re-enables easy.
        composeTestRule.onNodeWithStringId(android.R.string.ok)
            .assertExists()
            .performClick()

        composeTestRule.onNodeWithTag(EASY_TAG).assertIsOn()
        composeTestRule.onNodeWithStringId(R.string.preferences_alert).assertDoesNotExist()

        // Restore hard and extra so we leave no dirty state.
        composeTestRule.onNodeWithTag(HARD_TAG).assertIsOff().performClick().assertIsOn()
        composeTestRule.onNodeWithTag(EXTRA_TAG).assertIsOff().performClick().assertIsOn()
    }

    @Test
    fun testCardLabelsUpdateWhenFortHendrixToggles() {
        val base = Expansion.BASE
        val fort = Expansion.FORT_HENDRIX

        // Baseline: Fort Hendrix is OFF (guaranteed by @Before clearing prefs).
        composeTestRule.onNodeWithStringId(R.string.cards_range, base.easyRange!!.first,  base.easyRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, base.hardRange!!.first,  base.hardRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, base.extraRange!!.first, base.extraRange.last).assertIsDisplayed()

        // Enable Fort Hendrix → labels switch to Fort Hendrix ranges.
        composeTestRule.onNodeWithTag(fort.prefKey).performClick()

        composeTestRule.onNodeWithStringId(R.string.cards_range, fort.easyRange!!.first,  fort.easyRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, fort.hardRange!!.first,  fort.hardRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, fort.extraRange!!.first, fort.extraRange.last).assertIsDisplayed()

        // Disable Fort Hendrix → labels revert to BASE ranges.
        composeTestRule.onNodeWithTag(fort.prefKey).performClick()

        composeTestRule.onNodeWithStringId(R.string.cards_range, base.easyRange.first,  base.easyRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, base.hardRange.first,  base.hardRange.last).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.cards_range, base.extraRange.first, base.extraRange.last).assertIsDisplayed()
    }

    @Test
    fun testDannyTrejoToggles() {
        // Baseline: Danny Trejo is OFF (guaranteed by @Before clearing prefs).
        composeTestRule.onNodeWithTag(Expansion.DANNY_TREJO.prefKey)
            .assertIsDisplayed()
            .assertIsOff()
            .performClick()
            .assertIsOn()
            .performClick()
            .assertIsOff()
    }

    @Test
    fun testAllSixSwitchesExistAndDefaultStateIsCorrect() {
        val switchMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

        // Exactly 6 switches: 3 expansions + 3 difficulty.
        composeTestRule.onAllNodes(switchMatcher).assertCountEquals(6)

        // Default state: all 3 difficulty switches ON, all 3 expansion switches OFF.
        composeTestRule.onNodeWithTag(EASY_TAG).assertIsOn()
        composeTestRule.onNodeWithTag(HARD_TAG).assertIsOn()
        composeTestRule.onNodeWithTag(EXTRA_TAG).assertIsOn()
        composeTestRule.onNodeWithTag(Expansion.FORT_HENDRIX.prefKey).assertIsOff()
        composeTestRule.onNodeWithTag(Expansion.DANNY_TREJO.prefKey).assertIsOff()
        composeTestRule.onNodeWithTag(Expansion.URBAN_LEGENDS.prefKey).assertIsOff()
    }
}