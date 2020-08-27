/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.dcel;

import java.util.Iterator;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class DCELVertex<TGeom extends OrientedGeometry, TVertex extends DCELVertex<TGeom, TVertex, TDart, TFace>, TDart extends DCELDart<TGeom, TVertex, TDart, TFace>, TFace extends DCELFace<TGeom, TVertex, TDart, TFace>> extends Vector {

    TDart _dart;
    TFace _floatingInFace;

    boolean _marked;
    int _graphIndex;

    protected DCELVertex(Vector position) {
        this(position.getX(), position.getY());
    }

    protected DCELVertex(double x, double y) {
        super(x, y);

        _marked = false;
        _graphIndex = -1;
    }

    public int getGraphIndex() {
        return _graphIndex;
    }

    public void updateIncidentEdges() {
        TDart walk = _dart;
        do {
            walk.getGeometry().updateStart(this);
            walk._twin.getGeometry().updateEnd(this);

            walk = walk._twin._next;
        } while (walk != _dart);
    }

    @Override
    public void set(double x, double y) {
        super.set(x, y);
        updateIncidentEdges();
    }

    @Override
    public void set(Vector v) {
        super.set(v);
        updateIncidentEdges();
    }

    @Override
    public void setY(double y) {
        super.setY(y);
        updateIncidentEdges();
    }

    @Override
    public void setX(double x) {
        super.setX(x);
        updateIncidentEdges();
    }

    @Override
    public void rotate90DegreesCounterclockwise() {
        super.rotate90DegreesCounterclockwise();
        updateIncidentEdges();
    }

    @Override
    public void rotate90DegreesClockwise() {
        super.rotate90DegreesClockwise();
        updateIncidentEdges();
    }

    @Override
    public void invert() {
        super.invert();
        updateIncidentEdges();
    }

    @Override
    public double normalize() {
        double len = super.normalize();
        updateIncidentEdges();
        return len;
    }

    @Override
    public void scale(double factorX, double factorY) {
        super.scale(factorX, factorY);
        updateIncidentEdges();
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        super.rotate(counterclockwiseangle);
        updateIncidentEdges();
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        super.translate(deltaX, deltaY);
        updateIncidentEdges();
    }

    public TDart getDart() {
        return _dart;
    }

    public void setDart(TDart dart) {
        _dart = dart;
    }

    public TFace getFloatingInFace() {
        return _floatingInFace;
    }

    public void setFloatingInFace(TFace face) {
        _floatingInFace = face;
    }

    public boolean isMarked() {
        return _marked;
    }

    public void setMarked(boolean marked) {
        _marked = marked;
    }

    /**
     * Convenience method to iterate over all neighboring vertices in O(d) time.
     *
     * @return iterable over the neighboring vertices
     */
    public Iterable<TVertex> neighbors() {
        return () -> new Iterator() {

            TDart walk = _dart;

            @Override
            public boolean hasNext() {
                return walk != null;
            }

            @Override
            public TVertex next() {
                TVertex v = walk.getDestination();
                walk = walk.getTwin().getNext();
                if (walk == _dart) {
                    walk = null;
                }
                return v;
            }
        };
    }

    /**
     * Convenience method to iterate over all outgoing darts in O(d) time.
     *
     * @return iterable over the outgoing darts
     */
    public Iterable<TDart> darts() {
        return () -> new Iterator() {

            TDart walk = _dart;

            @Override
            public boolean hasNext() {
                return walk != null;
            }

            @Override
            public TDart next() {
                TDart d = walk;
                walk = walk.getTwin().getNext();
                if (walk == _dart) {
                    walk = null;
                }
                return d;
            }
        };
    }

    /**
     * Convenience method to iterate over all incident faces in O(d) time. Note
     * that faces may be repeated in case there are multiple outgoing darts that
     * are incident to the same face
     *
     * @return iterable over the incident faces
     */
    public Iterable<TFace> faces() {
        return () -> new Iterator() {

            TDart walk = _dart;

            @Override
            public boolean hasNext() {
                return walk != null;
            }

            @Override
            public TFace next() {
                TFace f = walk._face;
                walk = walk.getTwin().getNext();
                if (walk == _dart) {
                    walk = null;
                }
                return f;
            }
        };
    }

    /**
     * O(d)-time where d is the degree of the vertex.
     *
     * @return the degree of the node
     */
    public int getDegree() {
        int deg = 0;
        if (_dart != null) {
            TDart walk = _dart;
            do {
                deg++;
                walk = walk.getTwin().getNext();
            } while (walk != _dart);
        }
        return deg;
    }

    /**
     * O(min(d,k))-time where d is the degree of the vertex.
     *
     * @return true iff the degree is k
     */
    public boolean isDegree(int k) {
        int deg = 0;
        if (_dart != null) {
            TDart walk = _dart;
            do {
                deg++;
                if (deg > k) {
                    return false;
                }
                walk = walk.getTwin().getNext();
            } while (walk != _dart);
        }
        return k == deg;
    }

    /**
     * O(min(d,k))-time where d is the degree of the vertex.
     *
     * @return true iff the degree is <= k
     */
    public boolean isAtMostDegree(int k) {
        int deg = 0;
        if (_dart != null) {
            TDart walk = _dart;
            do {
                deg++;
                if (deg > k) {
                    return false;
                }
                walk = walk.getTwin().getNext();
            } while (walk != _dart);
            return false;
        }
        return k >= deg;
    }

    /**
     * O(min(d,k))-time where d is the degree of the vertex.
     *
     * @return true iff the degree is >= k
     */
    public boolean isAtLeastDegree(int k) {
        int deg = 0;
        if (_dart != null) {
            TDart walk = _dart;
            do {
                deg++;
                if (deg >= k) {
                    return true;
                }
                walk = walk.getTwin().getNext();
            } while (walk != _dart);
            return false;
        }
        return k <= deg;
    }
}
