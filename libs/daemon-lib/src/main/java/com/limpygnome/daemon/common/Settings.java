package com.limpygnome.daemon.common;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

/**
 * Used to read a JSON settings file.
 */
public class Settings
{
    private static final Logger LOG = LogManager.getLogger(Settings.class);

    private static final String SETTINGS_EXTENSION = ".json";

    private JSONObject root;

    public Settings() { }

    public Settings(Controller controller, String fileName)
    {
        reload(controller, fileName);
    }

    /**
     * Reloads settings from default controller config file.
     *
     * Current format:
     * .settings.[controller name].json
     *
     * @param controller The current controller instance
     */
    public synchronized void reload(Controller controller)
    {
        File defaultControllerSettingsFile = controller.findConfigFile(".settings." + controller.getControllerName() + SETTINGS_EXTENSION);
        reload(defaultControllerSettingsFile);
    }

    /**
     * Reloads settings from a global configuration file.
     *
     * @param controller The current controller instance
     * @param fileName The global configuration file
     */
    public synchronized void reload(Controller controller, String fileName)
    {
        File fileSettings = controller.findConfigFile(fileName);
        reload(fileSettings);
    }

    public synchronized void reload(File fileSettings)
    {
        LOG.info("Reloading settings...");
        LOG.debug("Using settings at {}", fileSettings.getAbsolutePath());

        // Clear existing settings
        this.root = null;

        // Attempt to parse as json
        try
        {
            JSONParser jsonParser = new JSONParser();
            root = (JSONObject) jsonParser.parse(new FileReader(fileSettings));

            LOG.info("Finished reloading settings successfully");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load settings - path: '" + fileSettings.getAbsolutePath() + "'", e);
        }
    }

    public synchronized JSONObject getRoot()
    {
        return root;
    }

    private synchronized Object getIterativeSettingAtPath(String path)
    {
        String[] pathSegments = path.trim().split("/");

        Object value = JsonUtil.getNestedNode(root, pathSegments);

        if (value == null)
        {
            return null;
        }

        return value;
    }

    public synchronized Object getObject(String path)
    {
        Object value = getIterativeSettingAtPath(path);

        if (value == null)
        {
            throw new RuntimeException("Setting '" + path + "' not found");
        }

        return value;
    }

    public synchronized JSONObject getJsonObject(String path)
    {
        return (JSONObject) getObject(path);
    }

    public synchronized JSONArray getJsonArray(String path)
    {
        return (JSONArray) getObject(path);
    }

    public synchronized int getInt(String path)
    {
        return (int) (long) getObject(path);
    }

    public synchronized long getLong(String path)
    {
        return (long) getObject(path);
    }

    public synchronized String getString(String path)
    {
        return (String) getObject(path);
    }

    public synchronized boolean getBoolean(String path)
    {
        return (boolean) getObject(path);
    }

    public synchronized Object getOptionalObject(String path, Object alternative)
    {
        Object value = getIterativeSettingAtPath(path);

        return value != null ? value : alternative;
    }

    public synchronized long getOptionalLong(String path, long alternative)
    {
        return (long) getOptionalObject(path, alternative);
    }

    public synchronized float getOptionalFloat(String path, float alternative)
    {
        return (float) getOptionalObject(path, alternative);
    }

}
