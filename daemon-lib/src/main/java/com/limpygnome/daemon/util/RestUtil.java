package com.limpygnome.daemon.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by limpygnome on 20/07/15.
 */
public class RestUtil
{

    public static HttpResponse httpPostRequest(String url, JSONObject jsonRoot) throws IOException, ConnectException
    {
        try
        {
            String json = jsonRoot.toJSONString();

            // Make request
            HttpClient httpClient = HttpClients.createMinimal();

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(json));

            return httpClient.execute(httpPost);
        }
        catch (ConnectException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to make JSON post request to: " + url, e);
        }
    }

}
