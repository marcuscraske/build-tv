package com.limpygnome.daemon.remote.service.auth;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.security.SecureRandom;

/**
 * Generates a random key on startup, which is used to authenticate inbound requests.
 *
 * TODO: block IPs with too many incorrect attempts, prevent brute force; reset on successful auth, limt of e.g. 5 per 15 mins
 */
public class RandomKeyAuthProviderService implements AuthProviderService
{
    private static final Logger LOG = LogManager.getLogger(RandomKeyAuthProviderService.class);

    /**
     * The length of auth keys generated.
     */
    public static final int AUTH_KEY_LENGTH = 256;

    /**
     * The root element in requests expecte to hold the auth key.
     */
    public static final String[] JSON_AUTH_ELEMENT = new String[] { "auth" };


    private String authToken;

    @Override
    public synchronized void start(Controller controller)
    {
        generateAuthKey();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        authToken = null;
    }

    @Override
    public synchronized boolean isAuthorised(RestRequest restRequest)
    {
        // Check auth token setup
        if (authToken == null)
        {
            // Unlikely this case will ever occur...
            LOG.warn("Failed to authorise request, no auth token generated");
            return false;
        }

        boolean authorised = false;

        // Check request has a valid auth element
        Object rawAuth = restRequest.getJsonElement(JSON_AUTH_ELEMENT);

        if (rawAuth != null && (rawAuth instanceof String))
        {
            // Check it matches what we're expecting...
            String auth = (String) rawAuth;
            authorised = auth.equals(this.authToken);

            if (!authorised)
            {
                LOG.debug("Token mis-match - expected: {}, provided: {}", authToken, auth);
            }
        }

        LOG.info("Request auth result - ip: {}, authorised: {}, path: {}",
                restRequest.getHttpExchange().getRemoteAddress(), authorised, restRequest.getPath()
        );

        return authorised;
    }

    private synchronized void generateAuthKey()
    {
        // Generate using ASCII chars 48 to 122
        final int charMin = 48;
        final int charMax = 122;
        final int charRange = charMax - charMin;

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder buffer = new StringBuilder(AUTH_KEY_LENGTH);

        for (int i = 0; i < AUTH_KEY_LENGTH; i++)
        {
            buffer.append((char) (charMin + secureRandom.nextInt(charRange)));
        }

        this.authToken = buffer.toString();

        // Log the key for dev purposes
        LOG.debug("Auth key generated: '{}' [{} chars]", authToken, AUTH_KEY_LENGTH);
    }

    @Override
    public synchronized String getAuthToken()
    {
        return authToken;
    }

}
