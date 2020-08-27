/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

/**
 * Glyphs can be stroked in one of three ways. No stroke, stroked with the
 * current stroke color, or stroked by a fixed color.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum GlyphStrokeMode {

    /**
     * No stroke is applied to this glyph
     */
    CLEAR,
    /**
     * The stroke is a fixed color
     */
    FIXED,
    /**
     * The stroke is colored according the current rendering context
     */
    STROKE;
}
