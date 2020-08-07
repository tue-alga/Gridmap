/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

/**
 * Base class for a non-point geometry with a start and end point.
 *
 * @param <TActual> the actual type of this geometry
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class OrientedGeometry<TActual extends OrientedGeometry> extends BaseGeometry<TActual> {

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    public abstract Vector getStart();

    public abstract Vector getEnd();

    public abstract Vector getStartTangent();

    public abstract Vector getEndTangent();

    /**
     * Compute and return the signed area for this oriented geometry. Positive
     * area lies "right" of the chord from start to end; negative area lies
     * "left" of the chord.
     *
     * @return signed area
     */
    public abstract double areaSigned();

    /**
     * Computes the perimeter length of this geometry.
     *
     * @return Length of the perimeter
     */
    public abstract double perimeter();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Reverse this geometry from start to end, without changing its appearance.
     */
    public abstract void reverse();

    /**
     * Updates the startpoint of this geometry.
     *
     * @param start new position for the startpoint
     */
    public void updateStart(Vector start) {
        updateEndpoints(start, null);
    }

    /**
     * Updates the endpoint of this geometry.
     *
     * @param end new position for the endpoint
     */
    public void updateEnd(Vector end) {
        updateEndpoints(null, end);
    }

    /**
     * Update the geometry to start and end at the given positions. Pass null
     * pointers to not update an endpoint.
     *
     * @param start new position for the startpoint
     * @param end new position for the endpoint
     */
    public abstract void updateEndpoints(Vector start, Vector end);
    //</editor-fold>
}
