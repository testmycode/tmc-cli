package fi.helsinki.cs.tmc.cli.tmcstuff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a class for storing all different login settings as a single array.
 */
public class SettingsHolder {
    private ArrayList<Settings> settingsArray;

    public SettingsHolder() {
        this.settingsArray = new ArrayList<>();
    }

    public Settings getSettings() {
        if (this.settingsArray.size() > 0) {
            // Get last used settings by default
            return this.settingsArray.get(0);
        }
        return null;
    }

    public Settings getSettings(String username, String server) {
        if (username == null || server == null) {
            return getSettings();
        }
        for (Settings settings : this.settingsArray) {
            if (settings.getUsername().equals(username)
                    && settings.getServerAddress().equals(server)) {
                // Move settings to index 0 so we can always use the last used settings by default
                this.settingsArray.remove(settings);
                this.settingsArray.add(0, settings);
                return settings;
            }
        }
        return null;
    }

    public void addSettings(Settings newSettings) {
        for (Settings settings : this.settingsArray) {
            if (settings.getUsername().equals(newSettings.getUsername())
                    && settings.getServerAddress().equals(newSettings.getServerAddress())) {
                // Replace old settings if username and server match
                this.settingsArray.remove(settings);
                break;
            }
        }
        this.settingsArray.add(0, newSettings);
    }

    public void deleteSettings(String username, String server) {
        Settings remove = null;
        for (Settings settings : this.settingsArray) {
            if (settings.getUsername().equals(username)
                    && settings.getServerAddress().equals(server)) {
                remove = settings;
                break;
            }
        }
        if (remove != null) {
            this.settingsArray.remove(remove);
        }
    }

    public void deleteAllSettings() {
        this.settingsArray = new ArrayList<Settings>();
    }

    public int settingsCount() {
        return this.settingsArray.size();
    }
}
