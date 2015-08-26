package com.impygnome.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by limpygnome on 26/08/15.
 */
public class MessageWindow extends JFrame
{
    private JLabel jLabel;

    public MessageWindow(String text)
    {
        Dimension screenSize = getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();

        // Set form properties
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Notification Client");

        // Disable window frame
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Create panel for controls
        JPanel jPanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.setPaint(new Color(255, 255, 255, 159));
                graphics2D.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        jPanel.setLayout(new GridBagLayout());
        add(jPanel);

        // Add label for text
        jLabel = new JLabel();
        jLabel.setVisible(false);
        jLabel.setText(text);
        int fontSize = (int) (Math.min(screenWidth, screenHeight) * 0.1f);
        jLabel.setFont(new Font("", Font.PLAIN, fontSize));
        jLabel.setForeground(Color.WHITE);
        jPanel.add(jLabel);

        // Set initial size of form
        setSize(200, (int) (screenWidth * 0.1));

        // Show window
        centerOnScreen();
        setVisible(true);

        // Gradually expand window
        MessageWindowExpanderThread expander = new MessageWindowExpanderThread(this, 60);
        expander.start();
    }

    public void centerOnScreen()
    {
        Dimension screenSize = getScreenSize();

        double x = (screenSize.getWidth() / 2.0) - ((double) getWidth() / 2.0);
        double y = (screenSize.getHeight() / 2.0) - ((double) getHeight() / 2.0);

        setLocation((int) x, (int) y);
    }

    public Dimension getScreenSize()
    {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public void showMessage()
    {
        jLabel.setVisible(true);
    }

}
