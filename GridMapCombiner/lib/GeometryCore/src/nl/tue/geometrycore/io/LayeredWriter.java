/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io;

/**
 * This interface can be used to make it explicit that writers can set a layer
 * for the upcoming objects to draw.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface LayeredWriter {

    public void setLayer(String layer);

}
