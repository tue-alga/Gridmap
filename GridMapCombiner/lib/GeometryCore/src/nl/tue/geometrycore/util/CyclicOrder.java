/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import java.util.Comparator;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CyclicOrder<TVector extends Vector> implements Comparator<TVector> {

    private final Vector center;
    private final Vector reference;
    private final boolean ccw;

    public CyclicOrder() {
        this.center = null;
        this.reference = Vector.right();
        this.ccw = true;
    }
    
    public CyclicOrder(Vector center) {
        this.center = center;
        this.reference = Vector.right();
        this.ccw = true;
    }

    public CyclicOrder(Vector center, boolean ccw) {
        this.center = center;
        this.reference = Vector.right();
        this.ccw = ccw;
    }

    public CyclicOrder(Vector center, Vector reference, boolean ccw) {
        this.center = center;
        this.reference = reference.clone();
        this.reference.normalize();
        this.ccw = ccw;
    }

    @Override
    public int compare(TVector o1, TVector o2) {
        Vector arm1 = center == null ? o1 : Vector.subtract(o1,center);
        Vector arm2 = center == null ? o2 : Vector.subtract(o2,center);
        
        double a1 = ccw ? reference.computeCounterClockwiseAngleTo(arm1, false, true) : reference.computeClockwiseAngleTo(arm1, false, true);
        double a2 = ccw ? reference.computeCounterClockwiseAngleTo(arm2, false, true) : reference.computeClockwiseAngleTo(arm2, false, true);
        
        return Double.compare(a1,a2);
    }

}
