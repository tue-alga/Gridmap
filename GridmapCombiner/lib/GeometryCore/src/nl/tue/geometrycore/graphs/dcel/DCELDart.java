/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.dcel;

import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.OrientedGeometry;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class DCELDart<TGeom extends OrientedGeometry, TVertex extends DCELVertex<TGeom, TVertex, TDart, TFace>, TDart extends DCELDart<TGeom, TVertex, TDart, TFace>, TFace extends DCELFace<TGeom, TVertex, TDart, TFace>>
        implements GeometryConvertable<TGeom> {

    TGeom _geometry;
    TVertex _origin;
    TDart _twin;
    TDart _previous;
    TDart _next;
    TFace _face;
    boolean _marked;
    int _graphIndex;

    protected DCELDart() {
        _marked = false;
        _graphIndex = -1;
    }
    
    
    public int getGraphIndex() {
        return _graphIndex;
    }

    public TVertex getDestination() {
        return _twin._origin;
    }

    public TFace getFace() {
        return _face;
    }

    public TDart getNext() {
        return _next;
    }

    public TVertex getOrigin() {
        return _origin;
    }

    public TDart getPrevious() {
        return _previous;
    }

    public TDart getTwin() {
        return _twin;
    }

    public TGeom getGeometry() {
        return _geometry;
    }

    public void setGeometry(TGeom geometry) {
        _geometry = geometry;
    }

    public void setOrigin(TVertex origin) {
        _origin = origin;
    }

    public void setDestination(TVertex destination) {
        _twin.setOrigin(destination);
    }

    public void setTwin(TDart twin) {
        _twin = twin;
    }

    public void setPrevious(TDart previous) {
        _previous = previous;
    }

    public void setNext(TDart next) {
        _next = next;
    }

    public void setFace(TFace face) {
        _face = face;
    }

    @Override
    public TGeom toGeometry() {
        return _geometry;
    }

    public boolean isMarked() {
        return _marked;
    }

    public void setMarked(boolean _marked) {
        this._marked = _marked;
    }

    @Override
    public String toString() {
        return _origin + " -> "+_twin._origin;
    }
    
    

}
