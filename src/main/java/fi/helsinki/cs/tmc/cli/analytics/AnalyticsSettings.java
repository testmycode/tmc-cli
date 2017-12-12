package fi.helsinki.cs.tmc.cli.analytics;

import fi.helsinki.cs.tmc.spyware.SpywareSettings;


public class AnalyticsSettings implements SpywareSettings {
    private boolean spyWareEnabled;
    private boolean detailedSpywareEnabled;

    @Override
    public boolean isSpywareEnabled() {
        return this.spyWareEnabled;
    }

    public void setSpyWareEnabled(boolean value) {
        this.spyWareEnabled = value;
    }

    @Override
    public boolean isDetailedSpywareEnabled() {
        return false;
    }

    public void setDetailedSpywareEnabled(boolean value) {
        this.detailedSpywareEnabled = value;
    }
}
