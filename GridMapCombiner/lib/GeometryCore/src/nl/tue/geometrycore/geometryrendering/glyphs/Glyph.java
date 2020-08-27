/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

import java.awt.Color;
import nl.tue.geometrycore.geometry.BaseGeometry;

/**
 * Basic glyph class. Glyphs are used to tie a geometric representation to
 * concepts that doesn't have one (or one not suitable for rendering). Glyphs
 * provide a basic shape that can be used to represent these concepts. Moreover,
 * it is possible to override the rendering context in one of number of ways.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class Glyph {

    /**
     * Constructs the basic geometry for the glyph. The origin of this geometry
     * should be (0,0).
     *
     * @return glyph representation
     */
    public abstract BaseGeometry getGlyphShape();

    /**
     * Returns the desired way of filling the glyph (if at all).
     *
     * @return one of GlyphFillMode
     */
    public abstract GlyphFillMode getFillMode();

    /**
     * Override this method to change the fixed color for filling. Only has
     * effect if {@link getFillMode} returns FIXED. Defaults to white if not
     * overridden by subclass.
     *
     * @return the fixed color
     */
    public Color getFillColor() {
        return Color.white;
    }

    /**
     * Returns the desired way of stroking the glyph (if at all).
     *
     * @return one of GlyphStrokeMode
     */
    public abstract GlyphStrokeMode getStrokeMode();

    /**
     * Override this method to change the fixed color for stroking. Only has
     * effect if {@link getStrokeMode} returns FIXED. Defaults to black if not
     * overridden by subclass.
     *
     * @return the fixed color
     */
    public Color getStrokeColor() {
        return Color.white;
    }

    /**
     * Returns the desired way of setting the strokewidth for the glyph. Only
     * has effect if {@link getStrokeMode} does not return CLEAR.
     *
     * @return one of GlyphStrokeWidthMode
     */
    public abstract GlyphStrokeWidthMode getStrokeWidthMode();

    /**
     * Override this method to change the fixed strokewidth. Only has effect if
     * {@link getStrokeWidthMode} returns FIXED. Defaults to 0.4 if not
     * overridden by subclass.
     *
     * @return the fixed strokewidth
     */
    public double getStrokeWidth() {
        return 0.4;
    }
}
