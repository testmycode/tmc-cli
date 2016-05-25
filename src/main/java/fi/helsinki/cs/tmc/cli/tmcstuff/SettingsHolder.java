package fi.helsinki.cs.tmc.cli.tmcstuff;

import java.util.ArrayList;

/**
 * This is a class for storing all different login settings as a single array.
 */
public class SettingsHolder {
    private ArrayList<Settings> settingsArray;

    public SettingsHolder() {
        this.settingsArray = new ArrayList<Settings>();
    }


    public Settings getSettings(String username, String server) {
        for (Settings settings : this.settingsArray) {
        }
        return null;
    }

    public void addSettings(Settings settings) {

    }
}
