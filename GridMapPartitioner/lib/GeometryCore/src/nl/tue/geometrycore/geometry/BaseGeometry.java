/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * This class implements an abstract geometry type, providing a common base
 * class for various geometric objects.
 *
 * @param <TActual> the actual type of this geometry
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class BaseGeometry<TActual extends BaseGeometry> implements GeometryConvertable<TActual> {

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Compute the intersections between this and the given geometry, using the
     * standard precision of DoubleUtil.EPS. Note that the result must be
     * independent from the input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @return list of geometries describing the intersection
     */
    public List<BaseGeometry> intersect(BaseGeometry other) {
        return intersect(other, DoubleUtil.EPS);
    }

    /**
     * Compute the intersections between this and the given geometry, using a
     * specified precision. Note that the result must be independent from the
     * input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param prec precision of the computation
     * @return list of geometries describing the intersection
     */
    public List<BaseGeometry> intersect(BaseGeometry other, double prec) {
        List<BaseGeometry> list = new ArrayList();
        intersect(other, prec, list);
        return list;
    }

    /**
     * Compute the intersections between this and the given geometry, using the
     * standard precision of DoubleUtil.EPS. Note that the result must be
     * independent from the input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param intersections list in which to store the intersection
     */
    public void intersect(BaseGeometry other, List<BaseGeometry> intersections) {
        intersect(other, DoubleUtil.EPS, intersections);
    }

    /**
     * Computes whether the provided point lies on the boundary of this
     * geometry, with a precision of DoubleUtil.EPS.
     *
     * @param point test point
     * @return whether point touches the boundary
     */
    public boolean onBoundary(Vector point) {
        return onBoundary(point, DoubleUtil.EPS);
    }

    /**
     * Computes whether the provided point lies on the boundary of this
     * geometry, with a desired precision.
     *
     * @param point test point
     * @param prec desired precision
     * @return whether point touches the boundary
     */
    public abstract boolean onBoundary(Vector point, double prec);

    /**
     * Computes smallest distance from the provided point to this geometry.
     *
     * @param point smallest distance is computed to this point
     * @return smallest distance
     */
    public double distanceTo(Vector point) {
        return closestPoint(point).distanceTo(point);
    }

    /**
     * Computes the point on this geometry that is closest to the provided
     * point. Note that this need not be a new vector.
     *
     * @param point point for which to find the closest on this geometry
     * @return closest point
     */
    public abstract Vector closestPoint(Vector point);

    /**
     * Compute the intersections between this and the given geometry, using a
     * specified precision. Note that the result must be independent from the
     * input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param prec precision of the computation
     * @param intersections list in which to store the intersection
     */
    public abstract void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Translates the geometry.
     *
     * @param deltaX the displacement in X-coordinate
     * @param deltaY the displacement in Y-coordinate
     */
    public abstract void translate(double deltaX, double deltaY);

    /**
     * Translates the geometry.
     *
     * @param delta the coordinates of this Vector are used as displacement in
     * X-coordinate and Y-coordinate
     */
    public void translate(Vector delta) {
        translate(delta.getX(), delta.getY());
    }

    /**
     * Rotates the geometry. Positive values indicate a counterclockwise
     * rotation; negative values a clockwise rotation.
     *
     * @param counterclockwiseangle angle of rotation
     */
    public abstract void rotate(double counterclockwiseangle);

    /**
     * Rotates the geometry around a given center point. Positive values
     * indicate a counterclockwise rotation; negative values a clockwise
     * rotation.
     *
     * @param counterclockwiseangle angle of rotation
     * @param center center position around which to rotate
     */
    public void rotate(double counterclockwiseangle, Vector center) {
        translate(-center.getX(), -center.getY());
        rotate(counterclockwiseangle);
        translate(center.getX(), center.getY());
    }

    /**
     * Scales the geometry, differently in X- and Y-coordinates.
     *
     * @param factorX factor of scaling in X-coordinate
     * @param factorY factor of scaling in Y-coordinate
     */
    public abstract void scale(double factorX, double factorY);

    /**
     * Scales the geometry uniformly.
     *
     * @param factor factor of uniform scaling
     */
    public void scale(double factor) {
        scale(factor, factor);
    }

    /**
     * Scales the geometry uniformly, maintaining a given center point.
     *
     * @param factor factor of uniform scaling
     * @param center position that remains fixed while scaling
     */
    public void scale(double factor, Vector center) {
        translate(-center.getX(), -center.getY());
        scale(factor, factor);
        translate(center.getX(), center.getY());
    }

    /**
     * Scales the geometry, differently in X- and Y-coordinates while
     * maintaining a given center point.
     *
     * @param factorX factor of scaling in X-coordinate
     * @param factorY factor of scaling in Y-coordinate
     * @param center position that remains fixed while scaling
     */
    public void scale(double factorX, double factorY, Vector center) {
        translate(-center.getX(), -center.getY());
        scale(factorX, factorY);
        translate(center.getX(), center.getY());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    /**
     * Returns the {@link GeometryType} to match this geometry.
     * 
     * @return enum item of GeometryType
     */
    public abstract GeometryType getGeometryType();

    /**
     * Performs a deep clone of this geometry, ensuring full independence
     * between this geometry and its clone.
     *
     * @return an independent clone
     */
    @Override
    public abstract TActual clone();

    @Override
    public TActual toGeometry() {
        return (TActual) this;
    }
    //</editor-fold>
}
