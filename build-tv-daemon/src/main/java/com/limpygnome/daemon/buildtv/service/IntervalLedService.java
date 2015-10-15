package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.led.pattern.source.IntervalPatternSource;
import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;
import com.limpygnome.daemon.buildtv.model.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

/**
 * A service for loading LED patterns to be displayed during intervals / time periods.
 */
public class IntervalLedService implements Service
{
    private static final Logger LOG = LogManager.getLogger(IntervalLedService.class);

    private static final String INTERVAL_JSON_FILE = "led-patterns.json";

    private Controller controller;
    private LedTimeService ledTimeService;
    private LinkedList<IntervalPatternSource> intervalPatterns;

    public IntervalLedService(Controller controller)
    {
        this.controller = controller;
        this.intervalPatterns = new LinkedList<>();
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Fetch LED service instance
        ledTimeService = (LedTimeService) controller.getServiceByName("led-time");

        // Fetch config file
        File ledPatternsConfig = controller.findConfigFile(INTERVAL_JSON_FILE);

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
                intervalPatterns.add(intervalPattern);
                ledTimeService.addPatternSource(intervalPattern);
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
    }

    private IntervalPatternSource parseIntervalPatternSource(JSONObject root)
    {
        Notification notification;

        if (root.containsKey("notfication"))
        {
            notification = parseIntervalPatternSourceNotification((JSONObject) root.get("notification"));
        }
        else
        {
            notification = null;
        }

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

    private Notification parseIntervalPatternSourceNotification(JSONObject root)
    {
        return new Notification(
                (String) root.get("header"),
                (String) root.get("text"),
                (long) root.get("lifespan"),
                Color.decode((String) root.get("background"))
        );
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        // Remove patterns from service
        for (IntervalPatternSource intervalPattern : intervalPatterns)
        {
            ledTimeService.removePatternSource(intervalPattern);
        }

        intervalPatterns.clear();
        ledTimeService = null;
    }
}
