/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing convenience methods for dealing with the clipboard.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ClipboardUtil {

    /**
     * Shorthand for sending text to the clipboard.
     *
     * @param string Value to set the clipboard to
     */
    public static void setClipboardContents(String string) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(string), null);
    }

    /**
     * Shorthand for retrieving text from the clipboard.
     *
     * @return The contents of the clipboard, if it is a string. An empty string
     * is returned otherwise.
     */
    public static String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                //highly unlikely since we are using a standard DataFlavor
                Logger.getLogger(ClipboardUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            //highly unlikely since we are using a standard DataFlavor
        }
        return result;
    }
}
