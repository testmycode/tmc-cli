package fi.helsinki.cs.tmc.cli.updater;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TmcCliUpdater {

    /**
     * URL to a JSON that contains information about the latest tmc-cli release.
     */
    private static final String LATEST_RELEASE_URL
            = "https://api.github.com/repos/tmc-cli/tmc-cli/releases/latest";

    private static final Logger logger
            = LoggerFactory.getLogger(TmcCliUpdater.class);

    private final Io io;
    private final boolean isWindows;

    public TmcCliUpdater(Io io) {
        this.io = io;
        this.isWindows = Application.isWindows();
    }

    /**
     * Checks if there's a newer tmc-cli version released on Github and asks if
     * the user wants to download it. todo: split it up
     */
    public void run() {
        JsonObject release = toJsonObject(fetchLatestReleaseJson());
        if (release == null || !isNewer(release)) {
            return;
        }

        io.println("A new version of tmc-cli is available!");

        if (isWindows) { //just show a link for Windows users now, todo...
            io.println("Download: https://github.com/tmc-cli/tmc-cli/releases/latest");
            return;
        }

        String answer = io.readLine("Do you want to download it? (y/N): ");
        if (!"y".equalsIgnoreCase(answer) && !"yes".equalsIgnoreCase(answer)) {
            return;
        }

        JsonObject binAsset = findCorrectAsset(release, isWindows);
        if (binAsset == null) {
            return;
        }

        String binName = binAsset.get("name").getAsString() + ".new";
        String dlUrl = binAsset.get("browser_download_url").getAsString();
        String currentBinLocation = getJarLocation();
        if (currentBinLocation == null) {
            io.println("Unable to find current program location, aborting update.");
            return;
        }
        File destination = new File(currentBinLocation + binName);

        io.println("Downloading...");
        fetchTmcCliBinary(dlUrl, destination);
    }

    /**
     * Downloads a JSON string that contains information about the latest
     * tmc-cli release.
     */
    private String fetchLatestReleaseJson() {
        try {
            return IOUtils.toString(new URL(LATEST_RELEASE_URL), "UTF-8");
        } catch (IOException ex) {
            logger.warn("Failed to fetch JSON data for the latest release", ex);
        }
        return null;
    }

    /**
     * Downloads a binary file from downloadUrl and saves it to destination
     * file.
     */
    private void fetchTmcCliBinary(String downloadUrl, File destination) {
        try {
            URL url = new URL(downloadUrl);
            FileUtils.copyURLToFile(url, destination);
        } catch (IOException ex) {
            io.println("Failed to download tmc-cli.");
            logger.warn("Failed to download tmc-cli.", ex);
        }
    }

    /**
     * True if release version differs from current version.
     */
    private boolean isNewer(JsonObject release) {
        if (!release.has("tag_name")) {
            return false;
        }
        String releaseVersion = release.get("tag_name").getAsString();
        String installedVersion = Application.getVersion();
        return !installedVersion.equals(releaseVersion);
    }

    /**
     * Goes through the release JSON and returns a *.jar asset if on Windows and
     * 'tmc' bash script asset if on *nix.
     */
    private JsonObject findCorrectAsset(JsonObject release, boolean isWindows) {
        JsonArray assets = release.getAsJsonArray("assets");

        for (JsonElement assetElement : assets) {
            JsonObject asset = assetElement.getAsJsonObject();
            String name = asset.get("name").getAsString();

            if (isWindows && name.endsWith(".jar")) {
                return asset;
            } else if (name.equals("tmc")) {
                return asset;
            }
        }
        return null;
    }

    /**
     * Parses a JSON string into JsonObject. Returns null if string is not valid
     * JsonObject.
     */
    private JsonObject toJsonObject(String jsonString) {
        if (jsonString == null) {
            return null;
        }

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        }
        return null;
    }

    private static String getJarLocation() {
        try {
            return new File(TmcCliUpdater.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent()
                    + File.separator;
        } catch (Exception ex) {
            logger.warn("Unable to get current jar folder.", ex);
            return null;
        }
    }
}
