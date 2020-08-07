/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.dcel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TGeom>
 * @param <TVertex>
 * @param <TFace>
 * @param <TDart>
 */
public abstract class DCELFace<TGeom extends OrientedGeometry, TVertex extends DCELVertex<TGeom, TVertex, TDart, TFace>, TDart extends DCELDart<TGeom, TVertex, TDart, TFace>, TFace extends DCELFace<TGeom, TVertex, TDart, TFace>> {

    // general info
    TDart _dart;
    boolean _marked;
    int _graphIndex;
    // for faces that are contained within another (clockwise cycles)
    // AKA: floating face
    TFace _floating_containingProperFace;
    final List<TFace> _floating_containedProperFaces; // each top-level face in the interior components
    // for faces that can contain other components (counterclockwise cycles)
    // AKA: proper face
    final List<TFace> _proper_floatingComponents; // one dart per interior component (bigger than a degree-0 vertex)
    final List<TVertex> _proper_floatingVertices; // degree-0 vertices
    // The outer face is a special case of a proper face that has no rim itself
    // dart = null

    // number of floating faces enclosing this face 
    // (NB: dont include self for floating face)
    // outer face and the floating faces on in the outer face have nesting 
    // depth 0. First level of proper faces has nesting depth 1, etc.
    int _nestingdepth;

    protected DCELFace() {
        _floating_containingProperFace = null;

        _floating_containedProperFaces = new ArrayList<TFace>();
        _proper_floatingComponents = new ArrayList<TFace>();
        _proper_floatingVertices = new ArrayList<TVertex>();

        _marked = false;
        _graphIndex = -1;
    }

    public int getNestingDepth() {
        return _nestingdepth;
    }

    public int getGraphIndex() {
        return _graphIndex;
    }

    public TDart getDart() {
        return _dart;
    }

    public boolean isOuterFace() {
        return _dart == null;
    }

    public boolean isFloatingFace() {
        return _floating_containingProperFace != null;
    }

    public boolean isProperFace() {
        return _floating_containingProperFace == null;
    }

    public List<TFace> getFloatingComponents() {
        return _proper_floatingComponents;
    }

    public TFace getProperFace() {
        if (isProperFace()) {
            return (TFace) this;
        } else {
            return _floating_containingProperFace;
        }
    }

    public List<TVertex> getFloatingVertices() {
        return _proper_floatingVertices;
    }

    public TFace getContainingProperFace() {
        return _floating_containingProperFace;
    }

    public void setContainingProperFace(TFace floatingIn) {
        _floating_containingProperFace = floatingIn;
    }

    public List<TFace> getContainedProperFaces() {
        return _floating_containedProperFaces;
    }

    public void setDart(TDart dart) {
        _dart = dart;
    }

    public Polygon computeOuterRim() {
        List<Vector> vertices = new ArrayList();
        TDart dart = _dart;
        do {
            vertices.add(dart.getOrigin());

            dart = dart._next;
        } while (dart != _dart);

        return new Polygon(vertices);
    }

    public void recomputeContainedProperFaces() {
        if (!isFloatingFace()) {
            // only do it for floating faces
            return;
        }

        _floating_containedProperFaces.clear();

        List<TDart> markeddarts = new ArrayList();
        int next = 0;

        TDart initdart = _dart;
        initdart._marked = true;
        markeddarts.add(initdart);

        while (next < markeddarts.size()) {
            TDart exploredart = markeddarts.get(next);
            next++;

            if (exploredart._face != this && !exploredart._face._marked) {
                _floating_containedProperFaces.add(exploredart._face);
                exploredart._face._marked = true;
            }

            if (!exploredart._previous._marked) {
                exploredart._previous._marked = true;
                markeddarts.add(exploredart._previous);

            }

            if (!exploredart._next._marked) {
                exploredart._next._marked = true;
                markeddarts.add(exploredart._next);
            }

            if (!exploredart._twin._marked) {
                exploredart._twin._marked = true;
                markeddarts.add(exploredart._twin);
            }
        }

        for (TDart dart : markeddarts) {
            dart._marked = false;
        }

        for (TFace face : _floating_containedProperFaces) {
            face._marked = false;
        }
    }

    public Iterable<TVertex> iterateVertices() {
        return () -> new Iterator<TVertex>() {
            TDart walk = _dart;
            boolean first = true;

            @Override
            public boolean hasNext() {
                return first || walk != _dart;
            }

            @Override
            public TVertex next() {
                first = false;
                walk = walk.getNext();
                return walk.getOrigin();
            }
        };
    }

    public Iterable<TDart> iterateDarts() {
        return () -> new Iterator<TDart>() {
            TDart walk = _dart;
            boolean first = true;

            @Override
            public boolean hasNext() {
                return first || walk != _dart;
            }

            @Override
            public TDart next() {
                first = false;
                walk = walk.getNext();
                return walk;
            }
        };
    }

    /**
     *
     * @param subtractholes
     * @return Negative for floating faces, positive for proper faces, infinite
     * for the outer face
     */
    public double computeArea(boolean subtractholes) {
        if (_dart == null) {
            // outerface
            return Double.POSITIVE_INFINITY;
        }

        double area = computeSignedAreaOfCycle(_dart);

        if (subtractholes) {
            for (TFace face : _floating_containedProperFaces) {
                area -= computeSignedAreaOfCycle(face._dart);
            }
        }

        return area;
    }

    public static double computeSignedAreaOfCycle(DCELDart dart) {
        double area = 0;
        DCELDart walk = dart;
        do {
            area += Vector.crossProduct(walk._origin, walk._twin._origin);
            walk = walk.getNext();
        } while (walk != dart);
        return 0.5 * area;
    }

    public boolean enclosesPoint(Vector point, boolean includerim) {

        if (isOuterFace()) {
            System.err.println("DCELFace -- calling enclosesPoint on outerface");
            return true;
        }

        return walkContains(_dart, point, includerim);
    }

    public boolean containsPoint(Vector point, boolean includerim) {
        if (!enclosesPoint(point, includerim)) {
            return false;
        } else {
            // check if not any hole contains it
            if (isFloatingFace()) {
                for (TFace face : _floating_containedProperFaces) {
                    if (walkContains(face._dart, point, !includerim)) {
                        // contained by hole
                        return false;
                    }
                }
            } else {
                for (TFace floating : _proper_floatingComponents) {
                    for (TFace face : floating._floating_containedProperFaces) {
                        if (walkContains(face._dart, point, !includerim)) {
                            // contained by hole
                            return false;
                        }
                    }
                }
            }

            // no hole contains it
            return true;
        }
    }

    private boolean walkContains(TDart dart, Vector point, boolean includerim) {

        // TODO: take other kinds of edges into account?         
        // TODO: include/exclude if point lies on a dart
        TDart walk = dart;
        if (walk._origin.isApproximately(point)) {
            return includerim;
        }

        Vector point_origin = Vector.subtract(dart._origin, point);
        point_origin.normalize();

        double totalAngle = 0;
        do {

            walk = walk._next;
            if (walk._origin.isApproximately(point)) {
                return includerim;
            }

            Vector point_destination = Vector.subtract(walk._origin, point);
            point_destination.normalize();

            assert DoubleUtil.close(point_destination.length(), 1);
            assert DoubleUtil.close(point_origin.length(), 1);

            totalAngle += point_origin.computeSignedAngleTo(point_destination, false, false);

            point_origin = point_destination;

        } while (walk != dart);

        // its either 0 or a multiple of 2 PI
        // 0  -> outside
        // !0 -> inside
        return !(-0.5 < totalAngle && totalAngle < 0.5);
    }

    public boolean isMarked() {
        return _marked;
    }

    public void setMarked(boolean _marked) {
        this._marked = _marked;
    }
}
