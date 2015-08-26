package com.impygnome.client;

import com.impygnome.client.ui.MessageWindow;

/**
 * Created by limpygnome on 26/08/15.
 */
public class Program
{

    public static void main(String[] args)
    {
        MessageWindow messageWindow = new MessageWindow(
                "BUILD FAILURE",
                "Failed project:\ncom.test.project.integration",
                5000,
                255,
                0,
                0
        );

//        MessageWindow messageWindow = new MessageWindow("BUILD FAILURE", null);
    }

}
