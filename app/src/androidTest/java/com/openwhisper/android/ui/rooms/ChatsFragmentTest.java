package com.openwhisper.android.ui.rooms;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.openwhisper.android.testing.InstrumentedTestBase;
import com.openwhisper.android.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChatsFragmentTest extends InstrumentedTestBase {

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
    public void showsServerDisplayTitleAndMemberSubtitle() {
        onView(withText("bob")).check(matches(isDisplayed()));
        onView(withText("New group chat")).check(matches(isDisplayed()));
        onView(allOf(withText(containsString("You")), withText(containsString("bob"))))
                .check(matches(isDisplayed()));
    }
}
