package fi.helsinki.cs.tmc.cli.updater;

import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
    private final String currentVersion;

    public TmcCliUpdater(Io io, String currentVersion, boolean isWindows) {
        this.io = io;
        this.currentVersion = currentVersion;
        this.isWindows = isWindows;
    }

    /**
     * Checks if there's a newer tmc-cli version released on Github and asks if
     * the user wants to download it. TODO: split it up
     */
    public void run() {
        JsonObject release = toJsonObject(fetchLatestReleaseJson());
        if (release == null || !isNewer(release)) {
            return;
        }

        JsonObject binAsset = findCorrectAsset(release, isWindows);
        if (binAsset == null || !binAsset.has("name") || !binAsset.has("browser_download_url")) {
            logger.warn("The JSON does not contain necessary information for update.");
            return;
        }

        io.println("A new version of tmc-cli is available!");

        if (isWindows) { //just show a link for Windows users now, todo...
            io.println("Download: https://github.com/tmc-cli/tmc-cli/releases/latest");
            return;
        }

        if (! io.readConfirmation("Do you want to download it?", true)) {
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

        io.println("Running " + destination.getAbsolutePath());
        runNewTmcCliBinary(destination.getAbsolutePath());
    }

    protected byte[] fetchHttpEntity(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "tmc-cli (https://github.com/tmc-cli/tmc-cli)");

        HttpEntity entity;
        byte[] content;
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            entity = httpResponse.getEntity();
            if (entity == null) {
                logger.warn("Failed to get http request content.");
                httpGet.releaseConnection();
                return null;
            }

        } catch (IOException ex) {
            logger.warn("Failed to create http connection to github.", ex);
            return null;
        }
        try {
            content = IOUtils.toByteArray(entity.getContent());
        } catch (IOException ex) {
            logger.warn("Failed to fetch data from github", ex);
            content = null;
        }
        httpGet.releaseConnection();
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
    protected void fetchTmcCliBinary(String downloadUrl, File destination) {
        byte[] content = fetchHttpEntity(downloadUrl);
        if (content == null) {
            io.println("Failed to download tmc-cli.");
            return;
        }
        try {
            FileUtils.writeByteArrayToFile(destination, content);
        } catch (IOException ex) {
            io.println("Failed to write the new version into \'" + destination + "\'.");
        }
    }

    /**
     * Finish the update by running downloaded binary.
     */
    protected void runNewTmcCliBinary(String pathToNewBinary) {
        ExternalsUtil.runUpdater(io, pathToNewBinary);
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
            return new File(TmcCliUpdater.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent()
                    + File.separator;
        } catch (Exception ex) {
            logger.warn("Unable to get current jar folder.", ex);
            return null;
        }
    }
}
