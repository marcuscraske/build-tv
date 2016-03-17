package com.limpygnome.daemon.util;

import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable URL functionality.
 */
public final class UrlUtil
{
    private UrlUtil() { }

    /**
     * Parses query parameters appended to the end of the URL.
     *
     * @param httpExchange The HTTP exchange
     * @return a map of query-string key-values
     */
    public static Map<String, String> parseQueryParams(HttpExchange httpExchange)
    {
        String query = httpExchange.getRequestURI().getQuery();
        String[] params = query.split("&");

        Map<String, String> result = new HashMap<>(params.length);

        String key;
        String value;
        int firstSeparator;

        for (String param : params)
        {
            firstSeparator = param.indexOf('=');

            if (firstSeparator > 0 && firstSeparator < param.length() - 1)
            {
                key = param.substring(0, firstSeparator);
                value = param.substring(firstSeparator+1);

                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * Checks if the provided URL is valid.
     *
     * @param url the url
     * @return true = valid, false = invalid
     */
    public static boolean isValidUrl(String url)
    {
        if (url == null || url.length() == 0)
        {
            return false;
        }

        try
        {
            new URI(url);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
