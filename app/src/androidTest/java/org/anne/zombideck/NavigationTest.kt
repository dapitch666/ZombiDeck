package org.anne.zombideck

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupNavHost() {
        hiltRule.inject()

        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            ZombiDeckApp(
                navController = navController,
                playAbominationSound = { },
                playShooterSound = { }
            )
        }
    }

    @Test
    fun testNavigationStartDestination() {
        composeTestRule.onNodeWithStringId(R.string.shuffle_and_start).assertExists()
        assertTrue(this::navController.isInitialized)
        navController.assertCurrentRouteName("Welcome")
    }

    @Test
    fun testNavigationToSettings() {
        composeTestRule.onNodeWithStringId(R.string.configure_the_deck).performClick()
        navController.assertCurrentRouteName("Settings")
    }

    @Test
    fun testNavigationBackFromSettings() {
        composeTestRule.onNodeWithStringId(R.string.configure_the_deck).performClick()
        navController.assertCurrentRouteName("Settings")
        composeTestRule.onNodeWithDescriptionStringId(R.string.back).performClick()
        navController.assertCurrentRouteName("Welcome")
    }

    @Test
    fun testNavigationToDraw() {
        composeTestRule.onNodeWithStringId(R.string.shuffle_and_start).performClick()
        navController.assertCurrentRouteName("Draw")
    }
}