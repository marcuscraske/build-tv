package com.limpygnome.daemon.remote.service.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by limpygnome on 16/03/16.
 */
public class ProxyServiceHandler implements HttpHandler
{

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        // TODO: move params into a nice util place
        Map<String, String> params = parseQueryParams(httpExchange);

        // Fetch URL from query string
        String url = params.get("url");

        System.out.println("url: " + url);

        // Make web request to URL
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);

        // Read data
        InputStream inputStream = connection.getInputStream();
        byte[] buffer = new byte[64000];
        int offset = 0;
        int read;

        while (offset < buffer.length && (read = inputStream.read(buffer, offset, buffer.length - offset)) > 0)
        {
            offset += read;
            System.out.println("offset:" + offset);
        }

//        String response = new String(buffer, Charset.forName("UTF-8"));

        // Write response
//        byte[] filteredResponse = response.getBytes(Charset.forName("UTF-8"));
        httpExchange.sendResponseHeaders(200, buffer.length);

        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(buffer);
        outputStream.flush();
        outputStream.close();

        System.out.println("response finished");
    }

    private Map<String, String> parseQueryParams(HttpExchange httpExchange)
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

}
