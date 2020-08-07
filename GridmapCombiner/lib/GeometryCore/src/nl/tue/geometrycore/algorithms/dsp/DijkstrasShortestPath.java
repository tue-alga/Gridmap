/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.dsp;

import nl.tue.geometrycore.algorithms.EdgeWeightInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import nl.tue.geometrycore.datastructures.priorityqueue.BasicIndexable;
import nl.tue.geometrycore.datastructures.priorityqueue.IndexedPriorityQueue;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.util.Pair;

/**
 * Implementation of Dijkstra's shortest path algorithm, running in O(V log V +
 * E) time. Note that an instance of this class assumes the vertices of the
 * graph do not change. If the graph is changed, create a fresh instance for new
 * shortest-paths queries.
 *
 * @param <TGraph> Class of graph to be used for shortest-path computations
 * @param <TGeom> Class of edge geometry used by TGraph
 * @param <TVertex> Class of vertex used by TGraph
 * @param <TEdge> Class of edge used by TGraph
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DijkstrasShortestPath<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final TGraph _graph;
    private final IndexedVertex[] _vertices;
    private final EdgeWeightInterface _weights;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Creates a object that can be queried for shortest paths in the given
     * graph. It uses length-weighted edges. Note that an instance of this class
     * assumes that the vertices of the graph do not change.
     *
     * @param graph the graph in which to compute shortest paths
     */
    public DijkstrasShortestPath(TGraph graph) {
        _graph = graph;
        _vertices = new IndexedVertex[_graph.getVertices().size()];
        for (int i = 0; i < _graph.getVertices().size(); i++) {
            _vertices[i] = new IndexedVertex();
            _vertices[i]._graphIndex = i;
            _vertices[i]._distance = Double.POSITIVE_INFINITY;
        }
        _weights = EdgeWeightInterface.LENGTH_WEIGHTS;
    }

    /**
     * Creates a object that can be queried for shortest paths in the given
     * graph. Note that an instance of this class assumes the vertices of the
     * graph do not change.
     *
     * @param graph the graph in which to compute shortest paths
     * @param weights interface that specifies custom edge weights for the
     * computation
     */
    public DijkstrasShortestPath(TGraph graph, EdgeWeightInterface<TEdge> weights) {
        _graph = graph;
        _vertices = new IndexedVertex[_graph.getVertices().size()];
        for (int i = 0; i < _graph.getVertices().size(); i++) {
            _vertices[i] = new IndexedVertex();
            _vertices[i]._graphIndex = i;
            _vertices[i]._distance = Double.POSITIVE_INFINITY;
        }
        _weights = weights;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Computes the shortest path between the given vertices and returns this
     * path as a list of edges as well as the computed length. Note that the
     * orientation of the edges need not correspond to the orientation required
     * to make a consistent path. Null is returned if no path exists.
     *
     *
     * @param from first node in the desired path
     * @param to last node in the desired path
     * @return null if no path was found, or a pair containing the path in a
     * list of edges as well as its computed length
     */
    public Pair<List<TEdge>, Double> computeShortestPathAndLength(TVertex from, TVertex to) {
        runDSP(from, to, null, Double.POSITIVE_INFINITY);
        List<TEdge> result = computePath(from, to);
        if (result == null) {
            return null;
        }
        double resultlength = _vertices[to.getGraphIndex()]._distance;
        cleanState();
        return new Pair(result, resultlength);
    }

    /**
     * Computes the shortest path between the given vertices and returns this
     * path as a list of edges. Note that the orientation of the edges need not
     * correspond to the orientation required to make a consistent path. Null is
     * returned if no path exists.
     *
     *
     * @param from first node in the desired path
     * @param to last node in the desired path
     * @return list of edges representing the shortest path or null
     */
    public List<TEdge> computeShortestPath(TVertex from, TVertex to) {
        runDSP(from, to, null, Double.POSITIVE_INFINITY);
        List<TEdge> result = computePath(from, to);
        cleanState();
        return result;
    }

    /**
     * Computes the shortest path between the given vertices and returns its
     * length. Positive infinity is returned if no path exists.
     *
     * @param from first node in the desired path
     * @param to last node in the desired path
     * @return length of the shortest path or positive infinity
     */
    public double computeShortestPathLength(TVertex from, TVertex to) {
        runDSP(from, to, null, Double.POSITIVE_INFINITY);
        double result = _vertices[to.getGraphIndex()]._distance;
        cleanState();
        return result;
    }

    /**
     * Computes the shortest path between the endpoints of the given edge,
     * without using the edge provided, and returns this path as a list of
     * edges. Note that the orientation of the edges need not correspond to the
     * orientation required to make a consistent path. Null is returned if no
     * path exists.
     *
     *
     * @param edge edge to find the shortest alternative path for
     * @return list of edges representing the shortest detour or null
     */
    public List<TEdge> computeShortestDetour(TEdge edge) {
        runDSP(edge.getStart(), edge.getEnd(), edge, Double.POSITIVE_INFINITY);
        List<TEdge> result = computePath(edge.getStart(), edge.getEnd());
        cleanState();
        return result;
    }

    /**
     * Computes the shortest path between the endpoints of the given edge,
     * without using the edge provided, and returns its length. Positive
     * infinity is returned if no path exists.
     *
     * @param edge edge to find the shortest alternative path for
     * @return length of the shortest detour or positive infinity
     */
    public double computeShortestDetourLength(TEdge edge) {
        runDSP(edge.getStart(), edge.getEnd(), edge, Double.POSITIVE_INFINITY);
        double result = _vertices[edge.getEnd().getGraphIndex()]._distance;
        cleanState();
        return result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void runDSP(TVertex from, TVertex to, TEdge ignore, double lengthcap) {

        IndexedPriorityQueue<IndexedVertex> queue = new IndexedPriorityQueue(_graph.getVertices().size() / 2, new Comparator<IndexedVertex>() {

            @Override
            public int compare(IndexedVertex o1, IndexedVertex o2) {
                return Double.compare(o1._distance, o2._distance);
            }
        });

        IndexedVertex first = _vertices[from.getGraphIndex()];
        first._distance = 0;
        queue.add(first);

        while (!queue.isEmpty()) {
            IndexedVertex iv = queue.poll();
            TVertex v = _graph.getVertices().get(iv._graphIndex);
                       
            if (v == to || iv._distance > lengthcap) {
                break;
            }

            for (TEdge e : v.getEdges()) {
                if (e == ignore) {
                    continue;
                }

                TVertex nb = e.getOtherVertex(v);
                IndexedVertex inb = _vertices[nb.getGraphIndex()];
                double w = _weights.getEdgeWeight(e);
                if (Double.isFinite(w)) {
                    double newdist = iv._distance + w;
                    if (Double.isInfinite(inb._distance)) {
                        inb._distance = newdist;
                        inb._previous = iv;
                        queue.add(inb);
                    } else if (inb._distance > newdist) {
                        inb._distance = newdist;
                        inb._previous = iv;
                        queue.priorityIncreased(inb);
                    }
                }
            }
        }
    }

    private List<TEdge> computePath(TVertex from, TVertex to) {
        if (_vertices[to.getGraphIndex()]._previous == null) {
            return null;
        }

        List<TEdge> result = new ArrayList();
        TVertex v = to;
        while (v != from) {
            TVertex prev = _graph.getVertices().get(_vertices[v.getGraphIndex()]._previous._graphIndex);
            result.add(v.getEdgeTo(prev));
            v = prev;
        }
        Collections.reverse(result);
        return result;
    }

    private void cleanState() {
        for (IndexedVertex iv : _vertices) {
            iv._distance = Double.POSITIVE_INFINITY;
            iv._previous = null;
        }
    }

    private static class IndexedVertex extends BasicIndexable {

        int _graphIndex;
        double _distance;
        IndexedVertex _previous;
    }
    //</editor-fold>
}
