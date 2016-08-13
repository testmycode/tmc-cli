package fi.helsinki.cs.tmc.cli.updater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Version implements Comparable<Version> {

    private static final String MATCH_REGEX = "^(\\d+)\\.(\\d+)\\.(\\d+)(?:-(.*))?$";

    private final int major;
    private final int minor;
    private final int patch;
    private final String metadata;

    public Version(String versionString) {
        Matcher matcher = Pattern.compile(MATCH_REGEX).matcher(versionString);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Illegal version string " + "'" + versionString + "'");
        }

        this.major = Integer.parseInt(matcher.group(1));
        this.minor = Integer.parseInt(matcher.group(2));
        this.patch = Integer.parseInt(matcher.group(3));
        this.metadata = matcher.group(4);
    }

    public Version(int major, int minor, int patch, String metadata) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.metadata = metadata;
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getMetadata() {
        return metadata;
    }

    public boolean isNewerThan(Version other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(Version that) {
        int majorDiff = this.major - that.major;
        int minorDiff = this.minor - that.minor;
        int patchDiff = this.patch - that.patch;

        if (majorDiff != 0) {
            return majorDiff;
        }
        if (minorDiff != 0) {
            return minorDiff;
        }
        return patchDiff;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (metadata == null ? "" : "-" + metadata);
    }
}
