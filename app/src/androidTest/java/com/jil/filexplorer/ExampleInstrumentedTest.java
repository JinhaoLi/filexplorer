package com.jil.filexplorer;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.jil.filexplorer.activity.MainActivity;
import com.jil.filexplorer.bean.Item;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    @Test
    public void useAppContext() {
         //Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.jil.filexplorer", appContext.getPackageName());
        Intent i =new Intent(appContext, MainActivity.class);
        getActivity(appContext,0,i,0);

        //onView(withId(R.id.imageButton3)).perform(click());
        Espresso.onView(withId(R.id.editText)).perform(click());

        try {
            Thread.sleep( 5000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void viewClick(){
        Matcher<View> matcher = ViewMatchers.withText("Backup");

        ViewInteraction vi =Espresso.onView(matcher);

        vi.perform(ViewActions.longClick());

        try {
            Thread.sleep( 5000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void textReplace(){
        Matcher<View> matcher = ViewMatchers.withId(R.id.editText);

        ViewInteraction vi =Espresso.onView(matcher);

        vi.perform(ViewActions.replaceText("hello,world"));

    }

    @Test
    public void testList() throws InterruptedException {
        //instanceOf(String.class);
        Thread.sleep( 1000 );
        Matcher<View> matcher = ViewMatchers.withText("Backup");

        ViewInteraction vi =Espresso.onView(matcher);

        vi.perform(ViewActions.longClick());
        Thread.sleep( 1000 );
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.hasChildCount(7))
                .atPosition(6)
                .perform(ViewActions.click());

        Thread.sleep( 5000 );
    }
}
