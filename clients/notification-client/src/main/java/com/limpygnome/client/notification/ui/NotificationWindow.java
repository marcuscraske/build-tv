package com.limpygnome.client.notification.ui;

import com.limpygnome.daemon.util.EnvironmentUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

/**
 * A notification window, used to visually display a notification.
 *
 * Consists of a header (optional), text (optional), lifespan and background colour.
 */
public class NotificationWindow extends JFrame
{
    private JPanel jPanel;
    private JLabel jLabelHeader;
    private JLabel jLabelText;

    public NotificationWindow(String header, String text, long lifespanMs, int backgroundR, int backgroundG, int backgroundB)
    {
        Dimension screenSize = EnvironmentUtil.getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();
        boolean hasText = (text != null);

        // Set form properties
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Notification Client");

        // We don't want this when developing...
        if (!EnvironmentUtil.isDevEnvironment())
        {
            setAlwaysOnTop(true);
        }

        // Fetch size of monitor, make size 80% of screen
        int targetWidth = (int) (screenWidth * 0.8f);
        int targetHeight = (int) (screenHeight * 0.8f);
        setSize(targetWidth, targetHeight);

        // Create panel for controls
        jPanel = new JPanel();
        jPanel.setBackground(new Color(backgroundR, backgroundG, backgroundB));
        jPanel.setLayout(new GridLayout(hasText ? 2 : 1, 1));
        add(jPanel);

        // Add labels for text
        double minScreenDimension = Math.min(screenWidth, screenHeight);

        jLabelHeader = new JLabel();
        jLabelHeader.setText(prepareLabelText(header));
        jLabelHeader.setFont(new Font("Arial", Font.PLAIN, (int) (minScreenDimension * 0.1)));
        jLabelHeader.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelHeader.setVerticalAlignment(hasText ? SwingConstants.BOTTOM : SwingConstants.CENTER);
        jLabelHeader.setForeground(Color.WHITE);
        jLabelHeader.setVisible(false);
        jPanel.add(jLabelHeader);

        if (hasText)
        {
            jLabelText = new JLabel();
            jLabelText.setText(prepareLabelText(text));
            jLabelText.setFont(new Font("Arial", Font.PLAIN, (int) (minScreenDimension * 0.05)));
            jLabelText.setHorizontalAlignment(SwingConstants.CENTER);
            jLabelText.setVerticalAlignment(SwingConstants.TOP);
            jLabelText.setForeground(Color.WHITE);
            jLabelText.setVisible(false);
            jPanel.add(jLabelText);
        }
        else
        {
            jLabelText = null;
        }

        // Show window
        centerOnScreen();
        setVisible(true);

        showMessage();

        // Setup timer to self-close window
        if (lifespanMs != 0)
        {
            new Timer((int) lifespanMs, new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    close();
                }
            }).start();
        }
    }

    private String prepareLabelText(String text)
    {
        if (text != null)
        {
            return "<html><center>" + text.replace("\n", "<br />") + "</center></html>";
        }
        else
        {
            return "";
        }
    }

    public void centerOnScreen()
    {
        Dimension screenSize = EnvironmentUtil.getScreenSize();

        double x = (screenSize.getWidth() / 2.0) - ((double) getWidth() / 2.0);
        double y = (screenSize.getHeight() / 2.0) - ((double) getHeight() / 2.0);

        setLocation((int) x, (int) y);
    }

    public void showMessage()
    {
        jLabelHeader.setVisible(true);

        if (jLabelText != null)
        {
            jLabelText.setVisible(true);
        }
    }

    public void close()
    {
        // This will correctly close and dispose the window whilst firing events
        dispatchEvent(new WindowEvent(NotificationWindow.this, WindowEvent.WINDOW_CLOSING));
    }

}
