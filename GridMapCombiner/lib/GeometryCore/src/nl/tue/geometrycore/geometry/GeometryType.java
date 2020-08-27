/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

/**
 * Enumeration to match the various types of geometries found in this library.
 * By using this enumeration, the code to check for various geometry types
 * becomes much simpler and faster.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum GeometryType {

    /**
     * For instances of {@link nl.tue.geometrycore.geometry.Vector}.
     */
    VECTOR {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.LineSegment}.
     */
    LINESEGMENT {
                @Override
                public boolean isOrientable() {
                    return true;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.HalfLine}.
     */
    HALFLINE {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return true;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.Line}.
     */
    LINE {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return true;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.PolyLine}.
     */
    POLYLINE {
                @Override
                public boolean isOrientable() {
                    return true;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.Polygon}.
     */
    POLYGON {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return true;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.linear.Rectangle}.
     */
    RECTANGLE {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return true;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.curved.CircularArc}.
     */
    CIRCULARARC {
                @Override
                public boolean isOrientable() {
                    return true;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of {@link nl.tue.geometrycore.geometry.curved.Circle}.
     */
    CIRCLE {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return true;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of
     * {@link nl.tue.geometrycore.geometry.mixed.GeometryCycle}.
     */
    GEOMETRYCYCLE {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return true;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of
     * {@link nl.tue.geometrycore.geometry.mixed.GeometryString}.
     */
    GEOMETRYSTRING {
                @Override
                public boolean isOrientable() {
                    return true;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of
     * {@link nl.tue.geometrycore.geometry.mixed.GeometryGroup}.
     */
    GEOMETRYGROUP {
                @Override
                public boolean isOrientable() {
                    return false;
                }

                @Override
                public boolean isCyclic() {
                    return false;
                }

                @Override
                public boolean isInfinite() {
                    return false;
                }
            },
    /**
     * For instances of
     * {@link nl.tue.geometrycore.geometry.curved.BezierCurve}.
     */
    BEZIERCURVE {
        @Override
        public boolean isOrientable() {
        return true;
        }

        @Override
        public boolean isCyclic() {
            return false;
        }

        @Override
        public boolean isInfinite() {
            return false;
        }
        
    };
    
    /**
     * Checks whether the geometry inherits from {@link OrientableGeometry}. In
     * other words, does it have a start and endpoint?
     *
     * @return whether the geometry can be cast to {@link OrientableGeometry}
     */
    public abstract boolean isOrientable();

    /**
     * Checks whether the geometry inherits from {@link CyclicGeometry}. In
     * other words, does it represent some simple areal feature?
     *
     * @return whether the geometry can be cast to {@link CyclicGeometry}
     */
    public abstract boolean isCyclic();

    /**
     * Checks whether the geometry implements {@link InfiniteGeometry}. In other
     * words, does it represent a feature with unbounded perimeter and/or area?
     *
     * @return whether the geometry can be cast to {@link InfiniteGeometry}
     */
    public abstract boolean isInfinite();
}
