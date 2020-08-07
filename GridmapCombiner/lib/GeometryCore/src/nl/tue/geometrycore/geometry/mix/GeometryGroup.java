/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.mix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;

/**
 * This represents a group of multiple geometries, not necessarily connected /
 * sequential.
 *
 * @param <TPart> type of geometries contained in this group
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeometryGroup<TPart extends BaseGeometry<TPart>> extends BaseGeometry<GeometryGroup<TPart>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">    
    private final List<TPart> _parts;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty geometry group.
     */
    public GeometryGroup() {
        _parts = new ArrayList();
    }

    /**
     * Constructs a geometry group with the given parts.
     *
     * @param parts parts of the desired group
     */
    public GeometryGroup(List<? extends TPart> parts) {
        _parts = (List) parts;
    }

    /**
     * Constructs a geometry group with the given parts.
     *
     * @param parts parts of the desired group
     */
    public GeometryGroup(TPart... parts) {
        this();
        _parts.addAll(Arrays.asList(parts));

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public List<TPart> getParts() {
        return _parts;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public boolean onBoundary(Vector point, double prec) {
        for (TPart part : _parts) {
            if (part.onBoundary(point, prec)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (TPart part : _parts) {
            Vector closest = part.closestPoint(point);
            double d = closest.squaredDistanceTo(point);
            if (d < distance) {
                result = closest;
                distance = d;
            }
        }
        return result;
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        for (int i = 0; i < getParts().size(); i++) {
            TPart part = getParts().get(i);
            intersections.addAll(part.intersect(otherGeom, prec));
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        for (TPart part : _parts) {
            part.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        for (TPart part : _parts) {
            part.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        for (TPart part : _parts) {
            part.scale(factorX, factorY);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.GEOMETRYGROUP;
    }

    @Override
    public GeometryGroup<TPart> clone() {
        List<TPart> cloned = new ArrayList();
        for (TPart part : _parts) {
            cloned.add(part.clone());
        }
        return new GeometryGroup(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _parts.size() + "]";
    }
    //</editor-fold>
}
