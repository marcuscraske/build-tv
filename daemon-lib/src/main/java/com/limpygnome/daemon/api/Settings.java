package com.limpygnome.daemon.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final String[] SETTINGS_PATHS = {
            "config",
            "deploy/files/config"
    };

    private static final String SETTINGS_EXTENSION = ".json";

    private Controller controller;
    private JSONObject root;

    Settings(Controller controller)
    {
        this.controller = controller;
    }

    private File getSettingsFile(String daemonName)
    {
        File file;
        for (String path : SETTINGS_PATHS)
        {
            file = new File(path + "/settings." + daemonName + SETTINGS_EXTENSION);

            if (file.exists())
            {
                return file;
            }
        }

        // Build detailed exception message
        StringBuilder buffer = new StringBuilder();
        buffer.append("Unable to find daemon settings file; possible locations:\n");

        for (String path : SETTINGS_PATHS)
        {
            buffer.append("- '").append(path).append("/settings.").append(daemonName).append(SETTINGS_EXTENSION).append("\n");
        }

        buffer.deleteCharAt(buffer.length() - 1);

        throw new RuntimeException(buffer.toString());
    }

    public synchronized void reload()
    {
        LOG.info("Reloading settings...");

        // Locate settings file
        File fileSettings = getSettingsFile(controller.getControllerName());

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

        if (pathSegments.length == 0)
        {
            throw new RuntimeException("Invalid empty settings path provided");
        }

        String segment;
        JSONObject parent = root;

        for (int i = 0; i < pathSegments.length; i++)
        {
            segment = pathSegments[i];

            if (i == pathSegments.length - 1)
            {
                return parent.get(segment);
            }
            else
            {
                try
                {
                    parent = (JSONObject) parent.get(segment);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to read setting at '" + path + "'", e);
                }
            }
        }

        throw new RuntimeException("Failed to find setting at '" + path + "'");
    }

    public synchronized Object getObject(String path)
    {
        return getIterativeSettingAtPath(path);
    }

    public synchronized int getInt(String path)
    {
        return (int) getObject(path);
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

}
