/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

/**
 * Utility class providing convenience methods for GUI code to reduce code
 * duplication.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GUIUtil {

    /**
     * Creates and shows a JFrame window, with a given title. The frame is shown
     * maximized initially and has minimal dimensions of 300x300. It uses a
     * border layout to place a draw panel in the center and a tabbed side panel
     * on the left. The constructed frame terminates the java program on close.
     *
     * @param title Title for the JFrame
     * @param draw Draw panel to be placed centrally
     * @param side Side panel to be placed on the left (may be null)
     * @param logo Logo set to use for the frame
     * @return the constructed and shown JFrame
     */
    public static JFrame makeMainFrame(String title, GeometryPanel draw, TabbedSidePanel side, FrameLogo logo) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(300, 300));
        try {
            List<Image> icons = logo.getLogos();
            if (icons != null) {
                frame.setIconImages(icons);
            }
        } catch (IOException ex) {
            Logger.getLogger(GUIUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (side != null) {
            frame.add(side, BorderLayout.WEST);
        }
        if (draw != null) {
            frame.add(draw, BorderLayout.CENTER);
        }
        frame.setVisible(true);
        return frame;
    }

    /**
     * Creates and shows a JFrame window, with a given title. The frame is shown
     * maximized initially and has minimal dimensions of 300x300. It uses a
     * border layout to place a draw panel in the center and a tabbed side panel
     * on the left. The constructed frame terminates the java program on close.
     *
     * @param title Title for the JFrame
     * @param draw Draw panel to be placed centrally
     * @param side Side panel to be placed on the left (may be null)
     * @return the constructed and shown JFrame
     */
    public static JFrame makeMainFrame(String title, GeometryPanel draw, TabbedSidePanel side) {
        return makeMainFrame(title, draw, side, FrameLogo.GEOMETRYCORE);
    }
}
