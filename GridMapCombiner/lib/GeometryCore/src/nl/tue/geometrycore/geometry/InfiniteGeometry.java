/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Interface that must be implemented by geometries that have an infinite span.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface InfiniteGeometry<TClip extends BaseGeometry<TClip>> {

    /**
     * Clips the infinite geometry to the part that lies strictly within the clipbox.
     * Degeneracies (e.g. the parts coinciding with or touching the boundary), need not be
     * returned. A null result indicates that the geometry has no part strictly
     * within the clipbox.
     *
     * @param clipbox
     * @return
     */
    public TClip clip(Rectangle clipbox);
}
