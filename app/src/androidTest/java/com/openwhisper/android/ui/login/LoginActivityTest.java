package com.openwhisper.android.ui.login;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.openwhisper.android.R;
import com.openwhisper.android.testing.InstrumentedTestBase;
import com.openwhisper.android.testing.TestFixtures;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest extends InstrumentedTestBase {

    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void launchLogin() {
        scenario = ActivityScenario.launch(LoginActivity.class);
    }

    @After
    public void closeScenario() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void successfulLogin_showsMainBottomNav() {
        onView(withId(R.id.username)).perform(typeText("alice"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("secret"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.bottomNav)).check(matches(isDisplayed()));
    }

    @Test
    public void failedLogin_staysOnLoginScreen() {
        dispatcher.loginStatusCode = 401;
        dispatcher.loginBody = TestFixtures.LOGIN_ERROR_JSON;

        onView(withId(R.id.username)).perform(typeText("alice"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("wrong"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }
}
