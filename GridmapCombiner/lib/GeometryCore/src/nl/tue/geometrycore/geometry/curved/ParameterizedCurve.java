/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class ParameterizedCurve<TActual extends ParameterizedCurve> extends OrientedGeometry<TActual> {


    public abstract double getMinimumParameter();
    
    public abstract double getMaximumParameter();
    
     public abstract Vector getPointAt(double t);

    public PolyLine getApproximation(int points) {
        List<Vector> vs = new ArrayList(points);

        double mint = getMinimumParameter();
        double maxt = getMaximumParameter();

        double tstep = (maxt - mint) / (double) (points - 1);
        for (int i = 0; i < points; i++) {
            double t = tstep * i;
            vs.add(getPointAt(t));
        }

        return new PolyLine(vs);
    }
}
