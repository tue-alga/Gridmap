/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

/**
 * Glyphs can use strokewidths in one of two ways. Either, the width is
 * specified by the glyph, or the width is taken from the current rendering
 * context.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum GlyphStrokeWidthMode {

    /**
     * Use the strokewidth specified by the glyph.
     */
    FIXED,
    /**
     * Use the current context's strokewidth.
     */
    STROKE;

}
