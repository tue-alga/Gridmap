/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.styling;

/**
 * Represents a dashing pattern for strokes. It consists of a sequence of
 * values, such that starting with the first, it alternatingly specifies a dash
 * length, following by a gap length.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Dashing {

    //<editor-fold defaultstate="collapsed" desc="STATIC">
    /**
     * Solid strokes are represented by a null value. This is variable provided
     * to make the solid more explicit in code.
     */
    public final static Dashing SOLID = null;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public Dashing(double... pattern) {
        _pattern = pattern;
    }

    /**
     * Represents a dotted line with spacing equal to the dot size.
     *
     * @param linethickness line thickness for which to compute the pattern
     * @return dot pattern
     */
    public static Dashing dotted(double linethickness) {
        return new Dashing(0.001, 2 * linethickness);
    }

    /**
     * Represents a dashed line, with dashes being 3.5 times as long as thick.
     * Spacing is shorter, at 2.25 times the line thickness.
     *
     * @param linethickness line thickness for which to compute the pattern
     * @return dash pattern
     */
    public static Dashing dashed(double linethickness) {
        return new Dashing(2.5 * linethickness, 3.25 * linethickness);
    }

    public static Dashing dashdotted(double linethickness) {
        final double gap = 3 * linethickness;
        return new Dashing((2 * linethickness), gap, 0.001, gap);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PATTERN">
    private final double[] _pattern;

    public double[] getPattern() {
        return _pattern;
    }
    //</editor-fold>
}
