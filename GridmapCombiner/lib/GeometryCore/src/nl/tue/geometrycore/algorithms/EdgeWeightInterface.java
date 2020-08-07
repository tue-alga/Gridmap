/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms;

import nl.tue.geometrycore.graphs.simple.SimpleEdge;

/**
 * Interface used in graph algorithms to specify the weight of an edge.
 *
 * @param <TEdge> Class of the edges of a graph
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface EdgeWeightInterface<TEdge extends SimpleEdge> {

    /**
     * Default length-based weights for edges. In particular, it calls
     * {@link nl.tue.geometrycore.geometry.OrientedGeometry#perimeter()} on the
     * geometry of the edge.
     */
    public static EdgeWeightInterface LENGTH_WEIGHTS = new EdgeWeightInterface() {
        @Override
        public double getEdgeWeight(SimpleEdge edge) {
            return edge.toGeometry().perimeter();
        }
    };

    /**
     * Computes the weight of an edge for the use in algorithms. Ideally, this
     * operation should take only constant time.
     *
     * @param edge the given edge
     * @return the weight of the given edge
     */
    public double getEdgeWeight(TEdge edge);
}
