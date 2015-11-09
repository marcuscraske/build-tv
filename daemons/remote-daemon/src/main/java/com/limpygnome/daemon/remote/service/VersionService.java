package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.UUID;

/**
 * A service for retrieving the current version of this system's daemons etc.
 */
public class VersionService implements Service
{
    private static final Logger LOG = LogManager.getLogger(VersionService.class);

    public static final String SERVICE_NAME = "version";

    private static final String VERSION_FILENAME = "version.json";

    private String version;

    @Override
    public void start(Controller controller)
    {
        File file = controller.getFilePathConfig(VERSION_FILENAME);

        // Read version file - should already be defined
        try
        {
            JSONParser jsonParser = new JSONParser();
            JSONObject root = (JSONObject) jsonParser.parse(new FileReader(file));

            // Parse version
            this.version = (String) root.get("version");

            LOG.info("Loaded version data - version: {}", this.version);
        }
        catch (Exception e)
        {
            String absPath = file.getAbsolutePath();
            LOG.error("Failed to read version file - path: {}", absPath, e);
            throw new RuntimeException("Failed to load version from file - path: " + absPath, e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        this.version = null;
    }

    public String getVersion()
    {
        return version;
    }

}
