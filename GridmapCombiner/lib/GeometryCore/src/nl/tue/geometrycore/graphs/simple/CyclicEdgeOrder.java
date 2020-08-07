/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.simple;

import java.util.Comparator;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CyclicEdgeOrder<TEdge extends SimpleEdge> implements Comparator<TEdge> {

    private final SimpleVertex center;
    private final Vector reference;
    private final boolean ccw;

    public CyclicEdgeOrder() {
        this.center = null;
        this.reference = Vector.right();
        this.ccw = true;
    }

    public CyclicEdgeOrder(SimpleVertex center) {
        this.center = center;
        this.reference = Vector.right();
        this.ccw = true;
    }

    public CyclicEdgeOrder(SimpleVertex center, boolean ccw) {
        this.center = center;
        this.reference = Vector.right();
        this.ccw = ccw;
    }

    public CyclicEdgeOrder(SimpleVertex center, Vector reference, boolean ccw) {
        this.center = center;
        this.reference = reference.clone();
        this.reference.normalize();
        this.ccw = ccw;
    }

    @Override
    public int compare(TEdge o1, TEdge o2) {
        Vector arm1 = Vector.subtract(o1.getOtherVertex(center), center);
        Vector arm2 = Vector.subtract(o2.getOtherVertex(center), center);

        double a1 = ccw ? reference.computeCounterClockwiseAngleTo(arm1, false, true) : reference.computeClockwiseAngleTo(arm1, false, true);
        double a2 = ccw ? reference.computeCounterClockwiseAngleTo(arm2, false, true) : reference.computeClockwiseAngleTo(arm2, false, true);

        return Double.compare(a1, a2);
    }

}
