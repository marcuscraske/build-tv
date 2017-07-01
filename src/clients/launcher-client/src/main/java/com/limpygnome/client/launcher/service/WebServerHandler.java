package com.limpygnome.client.launcher.service;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.http.HttpServletResponse;

/**
 * Extended resource handler implementation to set headers.
 */
public class WebServerHandler extends ResourceHandler
{

    @Override
    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType)
    {
        super.doResponseHeaders(response, resource, mimeType);

        // Allow any page to be embedded in an iframe from HTTP/HTTPS...
        response.addHeader("Content-Security-Policy", "frame-src http://* https://*");
    }

}
