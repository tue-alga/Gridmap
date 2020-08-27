/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.simple;

import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.OrientedGeometry;

/**
 * Class to represent an edge in a simple edge-list implementation of a graph.
 *
 * @param <TGeom> The geometry of an edge, must inherit from OrientedGeometry
 * @param <TVertex> The class of a vertex
 * @param <TEdge> The class of an edge
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class SimpleEdge<TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> implements GeometryConvertable<TGeom> {

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------
    TGeom _geometry;
    TVertex _start;
    TVertex _end;
    int _graphIndex;

    // -------------------------------------------------------------------------
    // CONSTUCTORS
    // -------------------------------------------------------------------------
    protected SimpleEdge() {
        _graphIndex = -1;
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------
    public TGeom getGeometry() {
        return _geometry;
    }

    public TVertex getStart() {
        return _start;
    }

    public TVertex getEnd() {
        return _end;
    }

    public int getGraphIndex() {
        return _graphIndex;
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------
    public void reverse() {
        _geometry.reverse();
        TVertex tmp = _end;
        _end = _start;
        _start = tmp;
    }

    public TVertex getOtherVertex(SimpleVertex v) {
        assert v == _start || v == _end : "Calling getOtherVertex() with vertex that is neither...";

        if (v == _start) {
            return _end;
        } else {
            return _start;
        }
    }

    @Override
    public TGeom toGeometry() {
        return _geometry;
    }

    public TVertex getCommonVertex(SimpleEdge edge) {
        if (_start == edge._start || _start == edge._end) {
            return _start;
        } else if (_end == edge._start || _end == edge._end) {
            return _end;
        } else {
            return null;
        }
    }
}
