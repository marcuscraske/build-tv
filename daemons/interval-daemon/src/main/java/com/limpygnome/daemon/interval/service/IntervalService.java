package com.limpygnome.daemon.interval.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.api.Notification;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.interval.led.IntervalThread;
import com.limpygnome.daemon.interval.led.pattern.source.IntervalPatternSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Responsible for controlling the LED requests sent to the LED daemon.
 */
public class IntervalService implements Service
{
    private static final Logger LOG = LogManager.getLogger(IntervalService.class);

    public static final String SERVICE_NAME = "interval-service";

    /**
     * The file used for loading interval events.
     */
    private static final String INTERVAL_JSON_FILE = "led-patterns.json";

    private IntervalThread intervalThread;

    @Override
    public synchronized void start(Controller controller)
    {
        // Fetch config file
        File ledPatternsConfig = controller.findConfigFile(INTERVAL_JSON_FILE);

        // Create thread to handle intervals
        intervalThread = new IntervalThread(controller);

        // Load all of the intervals
        try
        {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonRoot = (JSONObject) jsonParser.parse(new FileReader(ledPatternsConfig));

            JSONArray intervals = (JSONArray) jsonRoot.get("intervals");

            JSONObject interval;
            IntervalPatternSource intervalPattern;

            for (Object obj : intervals)
            {
                interval = (JSONObject) obj;

                // Parse interval
                intervalPattern = parseIntervalPatternSource(interval);

                // Add to our own list for cleanup and then to the LED service
                intervalThread.addPattern(intervalPattern);
            }
        }
        catch (FileNotFoundException e)
        {
            LOG.error("Interval LED JSON file not found", e);
            LOG.error("Interval LED JSON file is expected at: {}", INTERVAL_JSON_FILE);
        }
        catch (Exception e)
        {
            LOG.error("Cannot read interval LED JSON file", e);
        }

        // Start thread to handle intervals...
        intervalThread.start();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        // Kill the thread
        if (intervalThread != null)
        {
            intervalThread.kill();
            intervalThread = null;
        }
    }

    private IntervalPatternSource parseIntervalPatternSource(JSONObject root)
    {
        // Parse notification
        Notification notification;

        if (root.containsKey("notification"))
        {
            notification = parseIntervalPatternSourceNotification((JSONObject) root.get("notification"));
        }
        else
        {
            notification = null;
        }

        // Create new instance and parse remaining data
        try
        {
            return new IntervalPatternSource(
                    (String) root.get("name"),
                    LedPattern.getByName((String) root.get("pattern")),
                    (int) (long) root.get("priority"),
                    (int) (long) root.get("startHour"),
                    (int) (long) root.get("startMinute"),
                    (int) (long) root.get("endHour"),
                    (int) (long) root.get("endMinute"),
                    (boolean) root.get("screenOff"),
                    notification
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Failed to parse LED pattern - data: " + (root != null ? root.toJSONString() : "null"),
                    e
            );
        }
    }

    private Notification parseIntervalPatternSourceNotification(JSONObject root)
    {
        return new Notification(
                (String) root.get("header"),
                (String) root.get("text"),
                (long) root.get("lifespan"),
                Color.decode((String) root.get("background")),
                root.containsKey("priority") ? (int) (long) root.get("priority") : IntervalPatternSource.NOTIFICATION_DEFAULT_PRIORITY
        );
    }

}
