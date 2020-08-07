/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.dcel;

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
    public void normalize() {
        super.normalize(); 
        updateIncidentEdges();
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
     * O(d)-time where d is the degree of the vertex.
     * 
     * @return 
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
     * O(k)-time.
     * 
     * @return 
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
     * O(k)-time
     * 
     * @return 
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
     * O(k)-time
     * 
     * @return 
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
