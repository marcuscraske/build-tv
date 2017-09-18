package com.limpygnome.daemon.remote.service.auth;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.common.rest.RestRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.SecureRandom;

/**
 * Generates a random key, if one does not already exist, which can be used to authenticate inbound requests.
 *
 * This is not the greatest security, but it'll prevent simple abuse.
 */
public class RandomTokenAuthProviderService implements AuthTokenProviderService
{
    private static final Logger LOG = LogManager.getLogger(RandomTokenAuthProviderService.class);

    /**
     * The length of auth keys generated.
     */
    public static final int AUTH_KEY_LENGTH = 256;

    /**
     * The root element in requests expecte to hold the auth key.
     */
    public static final String[] JSON_AUTH_ELEMENT = new String[] { "auth" };

    public static final String TOKEN_FILE = "auth-token.txt";


    private String authToken;

    @Override
    public synchronized void start(Controller controller)
    {
        try
        {
            File file = new File(TOKEN_FILE);

            if (!file.exists())
            {
                generateAuthKey(file);
            }
            else
            {
                readAuthKey(file);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("failed to read/write remote access token", e);
        }
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

    private void generateAuthKey(File file) throws IOException
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

        // write token to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(this.authToken);
        writer.flush();
        writer.close();

        // Log the key for dev purposes
        LOG.debug("Auth key generated: '{}' [{} chars]", authToken, AUTH_KEY_LENGTH);
    }

    private void readAuthKey(File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String authToken = reader.readLine();

        if (authToken != null)
        {
            authToken.trim();
        }

        this.authToken = authToken;
    }

    @Override
    public synchronized String getAuthToken()
    {
        return authToken;
    }

}
