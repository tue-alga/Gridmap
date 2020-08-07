/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryCloner;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

/**
 * Class to represent an undirected graph with a simple edge-list
 * implementation. It provides standard graph functionality, but is abstract to
 * allow for easy addition of auxiliary data to its elements. For practical use,
 * create derivations of SimpleGraph, SimpleEdge and SimpleVertex.
 *
 * @param <TGeom> The geometry of an edge, must inherit from OrientedGeometry
 * @param <TVertex> The class of a vertex
 * @param <TEdge> The class of an edge
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class SimpleGraph<TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    /**
     * For debugging purposes.
     */
    public void verify() {
        for (int i = 0; i < _vertices.size(); i++) {
            if (_vertices.get(i)._graphIndex != i) {
                System.err.println("Vertex error");
            }
        }

        for (int i = 0; i < _edges.size(); i++) {
            if (_edges.get(i)._graphIndex != i) {
                System.err.println("Edge error");
            }

            if (_vertices.get(_edges.get(i).getStart()._graphIndex) != _edges.get(i).getStart()) {
                System.err.println("Edge origin error");
            }

            if (_vertices.get(_edges.get(i).getEnd()._graphIndex) != _edges.get(i).getEnd()) {
                System.err.println("Edge destin error");
            }
        }
    }

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------
    private final List<TVertex> _vertices;
    private final List<TEdge> _edges;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------
    public SimpleGraph() {
        _vertices = new ArrayList();
        _edges = new ArrayList();
    }

    // -------------------------------------------------------------------------
    // GET & SET
    // -------------------------------------------------------------------------
    public List<TVertex> getVertices() {
        return _vertices;
    }

    public List<TEdge> getEdges() {
        return _edges;
    }

    // -------------------------------------------------------------------------
    // GRAPH ELEMENT CONSTRUCTORS
    // -------------------------------------------------------------------------
    public abstract TVertex createVertex(double x, double y);

    public abstract TEdge createEdge();

    // -------------------------------------------------------------------------
    // GRAPH MODIFICATIONS
    // -------------------------------------------------------------------------
    public int planarize() {
        int fixes = 0;

        for (int i = 0; i < _edges.size(); i++) {
            TEdge edge = _edges.get(i);
            for (int j = i + 1; j < _edges.size(); j++) {
                TEdge other = _edges.get(j);

                for (BaseGeometry geom : edge._geometry.intersect(other._geometry)) {

                    switch (geom.getGeometryType()) {
                        case VECTOR:
                            Vector point = (Vector) geom;

                            boolean splitEdge = true;
                            TVertex newVtx = null;
                            if (edge.getStart().isApproximately(point)) {
                                // nothing to do for edge
                                newVtx = edge.getStart();
                                splitEdge = false;
                            } else if (edge.getEnd().isApproximately(point)) {
                                // nothing to do for edge
                                newVtx = edge.getEnd();
                                splitEdge = false;
                            }

                            boolean splitOther = true;
                            if (other.getStart().isApproximately(point)) {
                                // nothing to do for other
                                if (newVtx == null) {
                                    newVtx = other.getStart();
                                    splitOther = false;
                                } else {
                                    continue;
                                }
                            } else if (other.getEnd().isApproximately(point)) {
                                // nothing to do for other
                                if (newVtx == null) {
                                    newVtx = other.getEnd();
                                    splitOther = false;
                                } else {
                                    continue;
                                }
                            }

                            if (newVtx == null) {
                                newVtx = addVertex(point);
                            }

                            if (splitEdge) {
                                fixes++;

                                TGeom clone = edge._geometry.clone();
                                clone.updateStart(newVtx);
                                addEdge(newVtx, edge.getEnd(), clone);

                                edge._end._edges.remove(edge);
                                edge._end = newVtx;
                                edge._geometry.updateEnd(edge._end);
                                edge._end._edges.add(edge);
                            }

                            if (splitOther) {
                                fixes++;
                                TGeom clone = other._geometry.clone();
                                clone.updateStart(newVtx);
                                addEdge(newVtx, other.getEnd(), clone);

                                other._end._edges.remove(other);
                                other._end = newVtx;
                                other._geometry.updateEnd(other._end);
                                other._end._edges.add(other);
                            }

                            break;
                        default:
                            // NYI
                            break;
                    }

                }
            }
        }

        return fixes;
    }

    public TVertex addVertex(Vector loc) {
        return addVertex(loc.getX(), loc.getY());
    }

    public TVertex addVertex(double x, double y) {
        TVertex vertex = createVertex(x, y);

        vertex._graphIndex = _vertices.size();
        _vertices.add(vertex);

        return vertex;
    }

    public TVertex findVertex(Vector loc) {
        return findVertex(loc.getX(), loc.getY(), DoubleUtil.EPS);
    }

    public TVertex findVertex(Vector loc, double precision) {
        for (TVertex v : _vertices) {
            if (v.isApproximately(loc, precision)) {
                return v;
            }
        }
        return null;
    }

    public TVertex findVertex(double x, double y) {
        return findVertex(new Vector(x, y), DoubleUtil.EPS);
    }

    public TVertex findVertex(double x, double y, double precision) {
        return findVertex(new Vector(x, y), precision);
    }

    public void removeVertex(TVertex vertex) {
        assert _vertices.get(vertex._graphIndex) == vertex : "Trying to remove vertex from graph of which it is not a vertex...";

        int n = vertex._edges.size() - 1;
        while (n >= 0) {
            removeEdge(vertex._edges.get(n));
            n--;
        }

        TVertex last = _vertices.remove(_vertices.size() - 1);
        if (last != vertex) {
            _vertices.set(vertex._graphIndex, last);
            last._graphIndex = vertex._graphIndex;
        }

        vertex._graphIndex = -1;
    }
    
    public void clear() {
        
        clearEdges();
        
        int n = _vertices.size()-1;
        while (n >= 0) {
            removeVertex(_vertices.get(n));
            n--;
        }
    }
    
    public void clearEdges() {
        
        int n = _edges.size();
        while (n > 0) {
            removeEdge(_edges.get(n-1));
            n--;
        }
    }

    public TEdge addEdge(TVertex from, TVertex to, TGeom geometry) {

        assert from != null : "Origin is null...";
        assert to != null : "Destination is null...";

        assert _vertices.get(from._graphIndex) == from : "Origin of edge is not part of this graph!";
        assert _vertices.get(to._graphIndex) == to : "Destination of edge is not part of this graph!";

        TEdge edge = from.getEdgeTo(to);

        if (edge == null) {
            edge = createEdge();

            edge._start = from;
            edge._end = to;
            edge._geometry = geometry;
            geometry.updateEndpoints(from, to);

            from._edges.add(edge);
            to._edges.add(edge);

            edge._graphIndex = _edges.size();
            _edges.add(edge);
        }

        return edge;
    }

    public void removeEdge(TEdge edge) {
        assert _edges.get(edge._graphIndex) == edge : "Trying to remove edge from graph of which it is not an edge...";

        edge._start._edges.remove(edge);
        edge._end._edges.remove(edge);

        TEdge last = _edges.remove(_edges.size() - 1);
        if (last != edge) {
            _edges.set(edge._graphIndex, last);
            last._graphIndex = edge._graphIndex;
        }

        edge._graphIndex = -1;
    }

    public Pair<List<GeometryString<TGeom>>, List<GeometryCycle<TGeom>>> computeChains() {
        List<GeometryCycle<TGeom>> islands = new ArrayList();
        List<GeometryString<TGeom>> chains = new ArrayList();

        boolean[] treated = new boolean[_edges.size()];
        for (int i = 0; i < treated.length; i++) {
            treated[i] = false;
        }

        for (int i = 0; i < treated.length; i++) {
            if (treated[i]) {
                continue;
            }

            TEdge edge = _edges.get(i);
            treated[i] = true;

            List<TGeom> chain = new ArrayList();
            chain.add(edge.getGeometry().clone());

            boolean looped = false;
            TEdge walk = edge;
            TVertex hop = walk.getStart();
            while (hop.getDegree() == 2) {
                walk = hop.getEdges().get(0) == walk ? hop.getEdges().get(1) : hop.getEdges().get(0);
                hop = walk.getOtherVertex(hop);

                if (treated[walk.getGraphIndex()]) {
                    looped = true;
                    break;
                } else {
                    treated[walk.getGraphIndex()] = true;

                    TGeom geom = walk.getGeometry().clone();
                    if (walk.getStart() != hop) {
                        geom.reverse();
                    }
                    chain.add(geom);
                }
            }

            Collections.reverse(chain);

            if (looped) {
                islands.add(new GeometryCycle(chain));
            } else {

                walk = edge;
                hop = walk.getEnd();
                while (hop.getDegree() == 2) {
                    walk = hop.getEdges().get(0) == walk ? hop.getEdges().get(1) : hop.getEdges().get(0);
                    hop = walk.getOtherVertex(hop);

                    assert !treated[walk.getGraphIndex()];

                    treated[walk.getGraphIndex()] = true;

                    TGeom geom = walk.getGeometry().clone();
                    if (walk.getEnd() != hop) {
                        geom.reverse();
                    }
                    chain.add(geom);
                }

                chains.add(new GeometryString(chain));
            }
        }

        return new Pair(chains, islands);
    }

    /**
     * Clones this graph into the target.
     *
     * @param target
     * @param cloner
     */
    public void cloneGraph(SimpleGraph<TGeom, TVertex, TEdge> target, GeometryCloner<TGeom, TGeom> cloner) {
        cloneGraph(target, cloner, null, null, null, null);
    }

    /**
     * Clones this graph into the target.
     *
     * @param target
     * @param cloner
     */
    public void cloneGraph(SimpleGraph<TGeom, TVertex, TEdge> target, GeometryCloner<TGeom, TGeom> cloner, Map<TVertex, TVertex> vertexMap, Map<TVertex, TVertex> vertexBackMap, Map<TEdge, TEdge> edgeMap, Map<TEdge, TEdge> edgeBackMap) {

        if (vertexMap == null) {
            vertexMap = new HashMap<TVertex, TVertex>();
        }

        for (TVertex vertex : _vertices) {
            TVertex newvertex = target.addVertex(vertex);

            vertexMap.put(vertex, newvertex);
        }

        for (TEdge edge : _edges) {
            TVertex from = vertexMap.get(edge._start);
            TVertex to = vertexMap.get(edge._end);

            target.addEdge(from, to, cloner.clone(edge._geometry));
        }
    }

}
