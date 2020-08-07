/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.styling;

/**
 * Configures in what space (typically view or world space) sizes are specified.
 * This applies to glyph sizes (pointsymbols and arrowheads), strokewidths, text
 * size, dash patterns, hashure widths and spacing, etc.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum SizeMode {

    /**
     * Sizes are specified in pixels (view space).
     */
    VIEW,
    /**
     * Sizes are specified in the coordinate system of the geometric objects
     * (world space).
     */
    WORLD;
}
