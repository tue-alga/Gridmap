/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class BezierCurve extends ParameterizedCurve<BezierCurve> {

    private final List<Vector> _controlpoints;

    public BezierCurve(Vector... controlpoints) {
        _controlpoints = new ArrayList(Arrays.asList(controlpoints));
    }

    public BezierCurve(List<Vector> controlpoints) {
        _controlpoints = controlpoints;
    }

    public List<Vector> getControlpoints() {
        return _controlpoints;
    }

    @Override
    public Vector getStart() {
        return _controlpoints.get(0);
    }

    @Override
    public Vector getEnd() {
        return _controlpoints.get(_controlpoints.size() - 1);
    }

    @Override
    public void reverse() {
        Collections.reverse(_controlpoints);
    }

    @Override
    public void updateEndpoints(Vector start, Vector end) {
        getStart().set(start);
        getEnd().set(end);
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        for (Vector cp : _controlpoints) {
            cp.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        for (Vector cp : _controlpoints) {
            cp.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        for (Vector cp : _controlpoints) {
            cp.scale(factorX, factorY);
        }
    }

    @Override
    public Vector getStartTangent() {
        Vector t = Vector.subtract(_controlpoints.get(1), _controlpoints.get(0));
        t.normalize();
        return t;
    }

    @Override
    public Vector getEndTangent() {
        Vector t = Vector.subtract(_controlpoints.get(_controlpoints.size() - 1), _controlpoints.get(_controlpoints.size() - 2));
        t.normalize();
        return t;
    }

    @Override
    public double areaSigned() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double perimeter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector closestPoint(Vector point) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.BEZIERCURVE;
    }

    @Override
    public BezierCurve clone() {
        List<Vector> cloned = new ArrayList();
        for (Vector vertex : _controlpoints) {
            cloned.add(vertex.clone());
        }
        return new BezierCurve(cloned);
    }

    private int choose(int n, int k) {
        final int min = (k < n - k ? k : n - k);
        int bin = 1;
        for (int i = 1; i <= min; i++) {
            bin *= n;
            bin /= i;
            n--;
        }
        return bin;
    }

    @Override
    public double getMinimumParameter() {
        return 0;
    }

    @Override
    public double getMaximumParameter() {
        return 1;
    }

    @Override
    public Vector getPointAt(double t) {
        Vector point = Vector.origin();

        int n = _controlpoints.size() - 1;
        for (int i = 0; i <= n; i++) {
            double fac = Math.pow(t, i) * Math.pow(1 - t, n - i) * choose(n, i);
            point.translate(Vector.multiply(fac, _controlpoints.get(i)));
        }

        return point;
    }

}
