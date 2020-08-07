/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

/**
 * This interface implements a simple conversion-to-BaseGeometry to convert any
 * object into a geometry representation. This is mostly used to allow generic
 * functions for writing and drawing geometries, without requiring said objects
 * to inherit from BaseGeometry.
 *
 * @param <T>
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface GeometryConvertable<T extends BaseGeometry> {

    /**
     * Returns an object that inherits from BaseGeometry that represents this
     * object.
     *
     * @return an object inheriting from BaseGeometry
     */
    public T toGeometry();
}
