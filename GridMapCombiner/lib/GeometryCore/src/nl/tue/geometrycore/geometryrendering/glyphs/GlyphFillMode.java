/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

/**
 * Glyphs can be filled in one of four ways. No fill, filled by the current fill
 * color, filled by a fixed color, or filled by the stroke color.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum GlyphFillMode {

    /**
     * No fill is applied to this glyph.
     */
    CLEAR,
    /**
     * The fill is a fixed color.
     */
    FIXED,
    /**
     * The fill is colored according to the stroke color.
     */
    STROKE,
    /**
     * The fill is colored using the current rendering context.
     */
    FILL;
}
