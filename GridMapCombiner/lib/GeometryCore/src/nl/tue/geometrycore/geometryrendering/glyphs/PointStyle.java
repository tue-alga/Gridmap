/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.glyphs;

import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Glyphs that can be used to style points. By default, point glyphs follow the
 * stroke of the render context, but have a fixed strokewidth.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class PointStyle extends Glyph {

    //<editor-fold defaultstate="collapsed" desc="BASIC">
    /**
     * Instantiates this glyph for a given location with a desired size.
     *
     * @param position position to place the origin of the basic shape at
     * @param size scale factor for the basic shape
     * @return newly constructed geometry to represent the point
     */
    public BaseGeometry represent(Vector position, double size) {
        BaseGeometry base = getGlyphShape();
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
        return GlyphStrokeWidthMode.FIXED;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STANDARD GLYPHS">
    public final static PointStyle CIRCLE_WHITE = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Circle(Vector.origin(), 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FIXED;
        }
    };

    public final static PointStyle CIRCLE_SOLID = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Circle(Vector.origin(), 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.STROKE;
        }
    };

    public final static PointStyle CIRCLE_COLORED = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Circle(Vector.origin(), 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FILL;
        }
    };

    public final static PointStyle SQUARE_SOLID = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Rectangle(-1, 1, -1, 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.STROKE;
        }
    };

    public final static PointStyle SQUARE_WHITE = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Rectangle(-1, 1, -1, 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FIXED;
        }
    };

    public final static PointStyle SQUARE_COLORED = new PointStyle() {

        @Override
        public BaseGeometry getGlyphShape() {
            return new Rectangle(-1, 1, -1, 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0.3;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.FILL;
        }
    };
    //</editor-fold>
}
