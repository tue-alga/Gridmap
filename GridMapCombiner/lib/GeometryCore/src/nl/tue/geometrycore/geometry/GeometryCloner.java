/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

/**
 * Interface to provide cloning methods between geometries.
 *
 * @param <TSource> class of the source object, to be cloned
 * @param <TTarget> class of the resulting object, the clone
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface GeometryCloner<TSource extends BaseGeometry, TTarget extends BaseGeometry> {

    /**
     * Clones the provided geometry into a new geometry, ensuring full
     * independence between the two.
     *
     * @param geometry geometry to be cloned
     * @return a clone of the given geometry
     */
    public TTarget clone(TSource geometry);
}
