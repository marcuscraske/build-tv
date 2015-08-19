package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.led.pattern.IntervalPattern;
import com.limpygnome.daemon.buildtv.led.LedDisplayPatterns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
    private LinkedList<IntervalPattern> intervalPatterns;

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
            IntervalPattern intervalPattern;

            for (Object obj : intervals)
            {
                interval = (JSONObject) obj;

                // Parse interval
                intervalPattern = new IntervalPattern(
                        (String) interval.get("name"),
                        LedDisplayPatterns.getByName((String) interval.get("pattern")),
                        (int) (long) interval.get("priority"),
                        (int) (long) interval.get("startHour"),
                        (int) (long) interval.get("startMinute"),
                        (int) (long) interval.get("endHour"),
                        (int) (long) interval.get("endMinute"),
                        (boolean) interval.get("screenOff")
                );

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

    @Override
    public synchronized void stop(Controller controller)
    {
        // Remove patterns from service
        for (IntervalPattern intervalPattern : intervalPatterns)
        {
            ledTimeService.removePatternSource(intervalPattern);
        }

        intervalPatterns.clear();
        ledTimeService = null;
    }
}
