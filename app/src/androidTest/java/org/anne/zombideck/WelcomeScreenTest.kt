package org.anne.zombideck

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class WelcomeScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            ZombiDeckApp(
                navController = navController,
                playAbominationSound = { },
                playShooterSound = { }
            )
        }
    }

    @Test
    fun testWelcomeScreen() {
        composeTestRule.onNodeWithStringId(R.string.shuffle_and_start).assertExists()
    }

    @Test
    fun testSettingsButton() {
        composeTestRule.onNodeWithStringId(R.string.configure_the_deck)
            .assertExists()
            .performClick()
    }
}