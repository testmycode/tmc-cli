package fi.helsinki.cs.tmc.cli.analytics;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.CliContext;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TimeTracker {
    private HashMap<String, String> properties;
    private static final String PROPERTY_KEY = "last-submit";

    public TimeTracker(CliContext context) {
        this.properties = context.getProperties();
    }

    public void restart() {
        long startTime = System.nanoTime();
        properties.put(PROPERTY_KEY, Long.toString(startTime));
        SettingsIo.saveProperties(properties);
    }

    public boolean anHourHasPassedSinceLastSubmit() {
        String submitProperty = SettingsIo.loadProperties().get(PROPERTY_KEY);
        if (submitProperty == null) {
            return false;
        }
        long startTime = Long.parseLong(submitProperty);
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        long hoursPassed = TimeUnit.HOURS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        return hoursPassed >= 1;
    }

}
