package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

/**
 * Responsible for generating and loading the UUID associated with this system and its series of daemons.
 */
public class InstanceIdentityService implements Service
{
    private static final Logger LOG = LogManager.getLogger(InstanceIdentityService.class);

    public static final String SERVICE_NAME = "instance-uuid";

    private static final String UUID_FILE_NAME = "identity.json";

    private static final String DEFAULT_INSTANCE_TITLE = "unnamed instance";

    private UUID uuid;
    private String title;

    @Override
    public void start(Controller controller)
    {
        // Read UUID file, else generate it
        File uuidFile = controller.getFilePathConfig(UUID_FILE_NAME);

        if (!uuidFile.exists())
        {
            setupNewFile(uuidFile);
        }
        else
        {
            readFile(uuidFile);
        }
    }

    private void readFile(File file)
    {
        try
        {
            JSONParser jsonParser = new JSONParser();
            JSONObject root = (JSONObject) jsonParser.parse(new FileReader(file));

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

    private void setupNewFile(File file)
    {
        try
        {
            // Generate new UUID
            this.uuid = UUID.randomUUID();

            // Generate default title
            this.title = DEFAULT_INSTANCE_TITLE;

            // Persist to file
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", this.uuid);
            jsonObject.put("title", this.title);

            String json = jsonObject.toJSONString();

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.flush();
            fileWriter.close();

            LOG.info("New identity generated for instance - uuid: {}, title: {}", this.uuid, this.title);
        }
        catch (Exception e)
        {
            String absPath = file.getAbsolutePath();
            LOG.error("Failed to setup new uuid - path: {}", absPath, e);
            throw new RuntimeException("Failed to setup new UUID to file - path: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        uuid = null;
    }

    public UUID getInstanceUuid()
    {
        return uuid;
    }

    public String getTitle()
    {
        return title;
    }

}
