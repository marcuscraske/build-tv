package com.limpygnome.daemon.remote.service.proxy;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.util.UrlUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Handles connections requiring URLs to be proxied.
 *
 * @see {@link ProxyService}
 */
public class ProxyServiceHandler implements HttpHandler
{
    private static final Logger LOG = LogManager.getLogger(ProxyServiceHandler.class);

    private final int connectTimeout;
    private final int readTimeout;
    private final int bufferSize;

    private long requestIdCounter;

    public ProxyServiceHandler(Controller controller)
    {
        requestIdCounter = 0;

        connectTimeout = (int) controller.getSettings().getOptionalLong("proxy/timeouts/connect", 5_000);
        readTimeout = (int) controller.getSettings().getOptionalLong("proxy/timeouts/read", 5_000);
        bufferSize = (int) controller.getSettings().getOptionalLong("proxy/buffer", 128_000);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        Map<String, String> params = UrlUtil.parseQueryParams(httpExchange);

        // Fetch request ID for logging purposes
        long requestId;

        synchronized (this)
        {
            requestId = this.requestIdCounter++;
        }

        // Fetch URL from query string
        String url = params.get("url");

        if (UrlUtil.isValidUrl(url))
        {
            LOG.info("Proxy request - rid: {}, url: {}", requestId, url);

            try
            {
                long requestStart = System.currentTimeMillis();

                // Make web request to URL
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);

                // Read data
                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[bufferSize];
                int offset = 0;
                int read;

                while (offset < buffer.length && (read = inputStream.read(buffer, offset, buffer.length - offset)) > 0)
                {
                    offset += read;
                }

                long requestEnd = System.currentTimeMillis();
                long requestTime = requestEnd - requestStart;

                LOG.info("Proxy data received - rid: {}, time (ms): {}", requestId, requestTime);

                // Write data back to client...
                httpExchange.sendResponseHeaders(HttpStatus.SC_OK, buffer.length);

                OutputStream outputStream = httpExchange.getResponseBody();
                outputStream.write(buffer);
                outputStream.flush();
                outputStream.close();

                LOG.info("Proxy response finished - rid: {}", requestId);
            }
            catch (Exception e)
            {
                LOG.error("Failed during proxy request - rid: {}", requestId, e);

                try
                {
                    httpExchange.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, 0);
                }
                catch (Exception e2)
                {
                    LOG.debug("Failed to send error response", e2);
                }
            }
            finally
            {
                httpExchange.close();
            }
        }
        else
        {
            LOG.warn("invalid URL requested for proxy service - rid: {}, url: {}", url);
        }
    }

}
