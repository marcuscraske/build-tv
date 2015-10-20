package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.UUID;

/**
 * Created by limpygnome on 20/10/15.
 */
public class VersionService implements Service
{
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

            LOG.

            // Parse UUID
            String rawUuid = (String) root.get("uuid");
            this.uuid = UUID.fromString(rawUuid);

            // Parse title
            String title = (String) root.get("title");

            LOG.info("Loaded identity for this instance - uuid: {}, title: {}", this.uuid, this.title);
        }
        catch (Exception e)
        {
            String absPath = file.getAbsolutePath();
            LOG.error("Failed to read existing uuid - path: {}", absPath, e);
            throw new RuntimeException("Failed to load UUID from file - path: " + absPath, e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

}
