package com.limpygnome.daemon.util;

import org.json.simple.JSONObject;

/**
 * A utility/helper class for dealing with JSON.
 */
public final class JsonUtil
{

    private JsonUtil() { }

    /**
     * Retrieves a nested node, but returns the default value if missing.
     *
     * @param root The root element
     * @param path The path of the node
     * @param defaultValue The default value returned if the target value is missing
     * @param <T> The type expected
     * @return Either the value at the specified path or default value if missing
     */
    public static <T> T getOptionalNestedNode(JSONObject root, String[] path, T defaultValue)
    {
        T object = (T) getNestedNode(root, path);

        if (object == null)
        {
            object = defaultValue;
        }

        return object;
    }

    /**
     * Retrieves a nested JSON object.
     *
     * @param root The root element
     * @param path The path of the node
     * @return The object, or null if not found
     */
    public static Object getNestedNode(JSONObject root, String[] path)
    {
        // Check we have a valid path
        if (path == null || path.length == 0)
        {
            throw new RuntimeException("Invalid path provided, most likely a programmatic error");
        }

        // Iterate each path segment, attempt to get to item
        String segment;
        JSONObject parent = root;
        Object element;

        for (int i = 0; i < path.length; i++)
        {
            segment = path[i];

            // Check if this is the last item
            if (i == path.length - 1)
            {
                return parent.get(segment);
            }
            else
            {
                element = parent.get(segment);

                // Check next element is a node
                if (element instanceof JSONObject)
                {
                    parent = (JSONObject) element;
                }
                else
                {
                    return null;
                }
            }
        }

        throw new RuntimeException("Failed to find setting at '" + path + "'");
    }

}
