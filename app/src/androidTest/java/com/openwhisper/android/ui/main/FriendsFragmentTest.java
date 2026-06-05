package com.openwhisper.android.ui.main;

import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.openwhisper.android.R;
import com.openwhisper.android.testing.InstrumentedTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FriendsFragmentTest extends InstrumentedTestBase {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void launchMain() {
        seedAuthenticatedSession();
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void closeScenario() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void showsFriendSectionsAndRows() {
        onView(withId(R.id.nav_contacts)).perform(click());
        onView(withText(R.string.incoming_requests)).check(matches(isDisplayed()));
        onView(withText(R.string.outgoing_requests)).check(matches(isDisplayed()));
        onView(withText(R.string.your_friends)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.username), withText("bob"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.username), withText("eve"))).check(matches(isDisplayed()));
    }
}
