/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

import java.awt.Color;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;

/**
 * Glyphs that can be used to style arrowheads, either in forward or backward
 * direction. The basic shape of the glyph should point in the right (1,0)
 * direction. By default, arrow glyphs follow the stroke of the render context,
 * in both color and width.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class ArrowStyle extends Glyph {

    //<editor-fold defaultstate="collapsed" desc="BASIC">
    /**
     * Instantiates this glyph for a given location with a desired direction and
     * size.
     *
     * @param position position to place the origin of the basic shape at
     * @param direction direction for the arrow
     * @param size scale factor for the basic shape
     * @return newly constructed geometry to represent the point
     */
    public BaseGeometry represent(Vector position, Vector direction, double size) {
        BaseGeometry base = getGlyphShape();
        double angle = Vector.right().computeCounterClockwiseAngleTo(direction, false, false);
        base.rotate(angle);
        base.scale(size);
        base.translate(position);
        return base;
    }

    @Override
    public GlyphStrokeMode getStrokeMode() {
        return GlyphStrokeMode.STROKE;
    }

    @Override
    public GlyphStrokeWidthMode getStrokeWidthMode() {
        return GlyphStrokeWidthMode.STROKE;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STANDARD GLYPHS">
    public final static ArrowStyle LINEAR = new ArrowStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new PolyLine(new Vector(-1.5, 1), new Vector(0, 0), new Vector(-1.5, -1));
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.CLEAR;
        }
    };

    public final static ArrowStyle TRIANGLE_SOLID = new ArrowStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Polygon(new Vector(-1.5, 1), new Vector(0, 0), new Vector(-1.5, -1));
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.STROKE;
        }
    };

    public final static ArrowStyle TRIANGLE_COLORED = new ArrowStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Polygon(new Vector(-1.5, 1), new Vector(0, 0), new Vector(-1.5, -1));
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FILL;
        }
    };

    public final static ArrowStyle TRIANGLE_WHITE = new ArrowStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Polygon(new Vector(-1.5, 1), new Vector(0, 0), new Vector(-1.5, -1));
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FIXED;
        }

        @Override
        public Color getFillColor() {
            return Color.white;
        }
    };
    //</editor-fold>
}
