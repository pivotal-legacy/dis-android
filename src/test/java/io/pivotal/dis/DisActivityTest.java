package io.pivotal.dis;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.AbstractModule;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import io.pivotal.dis.activity.DisActivity;
import io.pivotal.dis.lines.Line;
import io.pivotal.dis.lines.LinesClient;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DisActivityTest {

    @Test
    public void testLineColor() {
        final String redColor = "#E41F1F";
        final String whiteColor = "#113892";

        int backgroundIntColor = Color.parseColor(redColor);
        int foregroundIntColor = Color.parseColor(whiteColor);

        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(LinesClient.class).toInstance(new FakeLineClient(asList(new Line("Central", "Severe Delays", "12:30", "13:30", "13:10", "13:50", redColor, whiteColor))));
                bind(SharedPreferences.class).toInstance(PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application));
            }
        };

        DisApplication.overrideInjectorModule(module);

        DisActivity activity = setupActivity(DisActivity.class);
        ListView lines = (ListView) activity.findViewById(R.id.lines);

        shadowOf(lines).populateItems();
        View line = lines.getChildAt(0);
        assertThat(shadowOf(line).getBackgroundColor(), equalTo(backgroundIntColor));

        TextView lineName = (TextView) line.findViewById(R.id.line_name);
        assertThat(lineName.getCurrentTextColor(), equalTo(foregroundIntColor));
    }
}
