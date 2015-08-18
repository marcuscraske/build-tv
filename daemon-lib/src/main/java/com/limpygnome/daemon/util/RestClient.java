package com.limpygnome.daemon.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * A reusable REST client.
 */
public class RestClient
{
    private String userAgent;
    private int bufferSize;

    public RestClient(String userAgent, int bufferSize)
    {
        this.userAgent = userAgent;
        this.bufferSize = bufferSize;
    }

    public String executeStr(String url) throws IOException
    {
        HttpClient httpClient = HttpClients.createMinimal();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", userAgent);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        return StreamUtil.readInputStream(httpResponse.getEntity().getContent(), bufferSize);
    }

    public JSONObject executeJson(String url) throws IOException
    {
        String response = executeStr(url);

        try
        {
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(response);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Failed to parse content [" + response.length() + " chars]: " + response, e);
        }
    }
}
