package com.limpygnome.daemon.api;

import com.limpygnome.daemon.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

/**
 * Created by limpygnome on 18/08/15.
 */
public class Settings
{
    private static final Logger LOG = LogManager.getLogger(Settings.class);

    private static final String SETTINGS_EXTENSION = ".json";

    private Controller controller;
    private JSONObject root;

    Settings(Controller controller)
    {
        this.controller = controller;
    }

    public synchronized void reload()
    {
        LOG.info("Reloading settings...");

        // Fetch settings file
        File fileSettings = controller.findConfigFile("settings." + controller.getControllerName() + SETTINGS_EXTENSION);

        LOG.info("Using settings at: {}", fileSettings.getAbsolutePath());

        // Attempt to parse as json
        try
        {
            JSONParser jsonParser = new JSONParser();
            root = (JSONObject) jsonParser.parse(new FileReader(fileSettings));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load settings - path: '" + fileSettings.getAbsolutePath() + "'", e);
        }

        LOG.info("Finished reloading settings");
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
