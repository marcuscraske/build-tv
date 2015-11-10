package com.limpygnome.daemon.util;

import com.limpygnome.daemon.common.rest.RestResponse;
import org.json.simple.JSONObject;

import java.io.*;

/**
 * Created by limpygnome on 19/07/15.
 */
public class StreamUtil
{
    private static final int BUFFER_CHUNK_SIZE = 4096;

    /**
     * Reads input stream into a String and closes the stream.
     *
     * @param inputStream
     * @return
     */
    public static String readInputStream(InputStream inputStream, int maxBufferSize) throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        int readChars;
        char[] rawBufferChunk = new char[BUFFER_CHUNK_SIZE];

        // Limit is not strictly imposed, can be up to 1023 bytes over limit
        while   (
                    (readChars = bufferedReader.read(rawBufferChunk)) > 0 &&
                    (maxBufferSize == -1 || buffer.length() < maxBufferSize)
                )
        {
            buffer.append(rawBufferChunk, 0, readChars);
        }

        bufferedReader.close();

        return buffer.toString();
    }

    public static void writeJsonResponse(RestResponse restResponse, JSONObject jsonObject) throws IOException
    {
        String data = jsonObject.toJSONString();
        writeResponse(restResponse, data);
    }

    public static void writeResponse(RestResponse restResponse, String response) throws IOException
    {
        byte[] rawData = response.getBytes();

        // Set header
        restResponse.sendStatus(200, rawData.length);

        // Write data
        OutputStream outputStream = restResponse.getHttpExchange().getResponseBody();
        outputStream.write(rawData);
        outputStream.flush();
        outputStream.close();
    }

}
