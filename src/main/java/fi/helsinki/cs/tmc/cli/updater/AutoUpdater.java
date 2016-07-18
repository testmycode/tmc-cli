package fi.helsinki.cs.tmc.cli.updater;

import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
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
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AutoUpdater {

    /**
     * URL to a JSON that contains information about the latest tmc-cli release.
     */
    private static final String LATEST_RELEASE_URL
            = "https://api.github.com/repos/tmc-cli/tmc-cli/releases/latest";

    private static final Logger logger
            = LoggerFactory.getLogger(AutoUpdater.class);

    private final Io io;
    private final boolean isWindows;
    private final String currentVersion;

    public AutoUpdater(Io io, String currentVersion, boolean isWindows) {
        this.io = io;
        this.currentVersion = currentVersion;
        this.isWindows = isWindows;
    }

    public static AutoUpdater createUpdater(Io io, String currentVersion, boolean isWindows) {
        return new AutoUpdater(io, currentVersion, isWindows);
    }

    /**
     * Checks if there's a newer tmc-cli version released on Github and asks if
     * the user wants to download it. TODO: split it up
     */
    public boolean run() {
        JsonObject release = toJsonObject(fetchLatestReleaseJson());
        if (release == null || !isNewer(release)) {
            return false;
        }

        JsonObject binAsset = findCorrectAsset(release, isWindows);
        if (binAsset == null || !binAsset.has("name") || !binAsset.has("browser_download_url")) {
            logger.warn("The JSON does not contain necessary information for update.");
            return false;
        }

        io.println("A new version of tmc-cli is available!");

        if (isWindows) { //just show a link for Windows users now, todo...
            io.println("Download: https://github.com/tmc-cli/tmc-cli/releases/latest");
            return true;
        }

        if (! io.readConfirmation("Do you want to download it?", true)) {
            return false;
        }

        String binName = binAsset.get("name").getAsString() + ".new";
        String dlUrl = binAsset.get("browser_download_url").getAsString();
        String currentBinLocation = getJarLocation();
        if (currentBinLocation == null) {
            io.println("Unable to find current program location, aborting update.");
            return false;
        }
        File destination = new File(currentBinLocation + binName);

        io.println("Downloading...");
        if (!fetchTmcCliBinary(dlUrl, destination)) {
            return false;
        }

        // Below is "THE INSTALL SCRIPT DEBUGGING LINE"
        //destination = new File(currentBinLocation + "tmc");

        io.println("Running " + destination.getAbsolutePath());
        return runNewTmcCliBinary(destination.getAbsolutePath());
    }

    protected byte[] fetchHttpEntity(String urlAddress) {
        URL url;

        try {
            url = new URL(urlAddress);
        } catch (MalformedURLException ex) {
            logger.warn("Url formatting failed", ex);
            return null;
        }

        InputStream inputStream;
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "tmc-cli (https://github.com/tmc-cli/tmc-cli)");
            inputStream = connection.getInputStream();
        } catch (IOException ex) {
            logger.warn("Failed to fetch page", ex);
            io.println("Failed to create https connection to github.");
            return null;
        }

        byte[] content;
        try {
            content = IOUtils.toByteArray(inputStream);
        } catch (IOException ex) {
            logger.warn("Failed to fetch data from github", ex);
            content = null;
        }
        return content;
    }

    /**
     * Downloads a JSON string that contains information about the latest
     * tmc-cli release.
     */
    protected String fetchLatestReleaseJson() {
        byte[] content = fetchHttpEntity(LATEST_RELEASE_URL);
        if (content == null) {
            return null;
        }
        try {
            return new String(content, "UTF-8");
        } catch (IOException ex) {
            logger.warn("Failed to fetch JSON data for the latest release", ex);
        }
        return null;
    }

    /**
     * Downloads a binary file from downloadUrl and saves it to destination
     * file.
     */
    protected boolean fetchTmcCliBinary(String downloadUrl, File destination) {
        byte[] content = fetchHttpEntity(downloadUrl);
        if (content == null) {
            io.println("Failed to download tmc-cli.");
            return false;
        }
        try {
            FileUtils.writeByteArrayToFile(destination, content);
            return true;
        } catch (IOException ex) {
            io.println("Failed to write the new version into \'" + destination + "\'.");
            return false;
        }
    }

    /**
     * Finish the update by running downloaded binary.
     */
    protected boolean runNewTmcCliBinary(String pathToNewBinary) {
        return ExternalsUtil.runUpdater(io, pathToNewBinary);
    }

    /**
     * True if release version differs from current version.
     */
    private boolean isNewer(JsonObject release) {
        if (!release.has("tag_name")) {
            return false;
        }

        Version releaseVer = new Version(release.get("tag_name").getAsString());
        Version installedVer = new Version(currentVersion);

        return releaseVer.isNewerThan(installedVer);
    }

    /**
     * Goes through the release JSON and returns a *.jar asset if on Windows and
     * 'tmc' bash script asset if on *nix.
     */
    private JsonObject findCorrectAsset(JsonObject release, boolean isWindows) {
        JsonArray assets = release.getAsJsonArray("assets");
        if (assets == null) {
            return null;
        }

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

        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonString);
        } catch (Exception e) {
            logger.warn("Unable to parse malformed JSON string.");
            return null;
        }

        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        }
        return null;
    }

    private static String getJarLocation() {
        try {
            return new File(AutoUpdater.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent()
                    + File.separator;
        } catch (Exception ex) {
            logger.warn("Unable to get current jar folder.", ex);
            return null;
        }
    }
}
