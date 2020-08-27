/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.streams;

/**
 * Configures the streams to be redirected to this panel.
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum SelectedStreams {

    /**
     * Only System.out is affected.
     */
    OUT,
    /**
     * Only System.err is affected.
     */
    ERR,
    /**
     * Both System.out and System.err are affected.
     */
    BOTH;
}
