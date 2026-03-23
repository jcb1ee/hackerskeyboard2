package kr.jcb1ee.hackerskeyboard2;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that each settings activity launches without crashing on Android 12+.
 * Tests observable behavior only — not internal class structure.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsActivityLaunchTest {

    @Test
    public void latinIMESettings_launchesWithoutCrash() {
        try (ActivityScenario<LatinIMESettings> scenario =
                     ActivityScenario.launch(LatinIMESettings.class)) {
            // No assertion needed — if the activity crashes, the test fails automatically.
        }
    }

    @Test
    public void prefScreenActions_launchesWithoutCrash() {
        try (ActivityScenario<PrefScreenActions> scenario =
                     ActivityScenario.launch(PrefScreenActions.class)) {
        }
    }

    @Test
    public void prefScreenView_launchesWithoutCrash() {
        try (ActivityScenario<PrefScreenView> scenario =
                     ActivityScenario.launch(PrefScreenView.class)) {
        }
    }

    @Test
    public void prefScreenFeedback_launchesWithoutCrash() {
        try (ActivityScenario<PrefScreenFeedback> scenario =
                     ActivityScenario.launch(PrefScreenFeedback.class)) {
        }
    }

    @Test
    public void inputLanguageSelection_launchesWithoutCrash() {
        try (ActivityScenario<InputLanguageSelection> scenario =
                     ActivityScenario.launch(InputLanguageSelection.class)) {
        }
    }
}
