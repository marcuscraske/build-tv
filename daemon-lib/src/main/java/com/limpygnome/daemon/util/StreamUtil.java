package com.limpygnome.daemon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by limpygnome on 19/07/15.
 */
public class StreamUtil
{
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
        char[] rawBuffer = new char[1024];

        // Limit is not strictly imposed, can be up to 1023 bytes over limit
        while   (
                    (readChars = bufferedReader.read(rawBuffer)) > 0 &&
                    (maxBufferSize == -1 || buffer.length() < maxBufferSize)
                )
        {
            buffer.append(rawBuffer, 0, readChars);
        }
        bufferedReader.close();

        return buffer.toString();
    }
}
