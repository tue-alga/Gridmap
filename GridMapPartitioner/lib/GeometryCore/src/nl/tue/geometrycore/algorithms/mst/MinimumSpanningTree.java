/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.mst;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nl.tue.geometrycore.algorithms.EdgeWeightInterface;
import nl.tue.geometrycore.datastructures.priorityqueue.BasicIndexable;
import nl.tue.geometrycore.datastructures.priorityqueue.IndexedPriorityQueue;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

/**
 * Implementation of Prim's minimum spanning tree algorithm, running in O(V + E
 * log V) time. Note that an instance of this class assumes the vertices of the
 * graph do not change. If the graph is changed, create a fresh instance for new
 * spanning-tree queries.
 *
 * @param <TGraph> Class of graph to be used for spanning-tree computations
 * @param <TGeom> Class of edge geometry used by TGraph
 * @param <TVertex> Class of vertex used by TGraph
 * @param <TEdge> Class of edge used by TGraph
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class MinimumSpanningTree<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<VertexState> vstates;
    private final TGraph graph;
    private final EdgeWeightInterface ewi;
    private double lengthOfLastQuery = Double.NaN;
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public MinimumSpanningTree(TGraph graph, EdgeWeightInterface ewi) {
        this.graph = graph;
        this.vstates = new ArrayList(graph.getVertices().size());
        for (TVertex v : graph.getVertices()) {
            vstates.add(new VertexState(v));
        }
        this.ewi = ewi;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Returns the total length of the tree or forest computed by this object.
     *
     * @return sum of edge weights
     */
    public double getWeightOfLastQuery() {
        return lengthOfLastQuery;
    }

    /**
     * Computes the minimum spanning forest.
     *
     * @return a list of spanning tree nodes, each node is the root of a tree
     * spanning a component
     */
    public List<DirectedTreeNode<TGraph, TGeom, TVertex, TEdge>> computeMinimumSpanningForest() {

        lengthOfLastQuery = 0;
        IndexedPriorityQueue<VertexState> queue = new IndexedPriorityQueue(vstates.size(), new Comparator<VertexState>() {

            @Override
            public int compare(VertexState o1, VertexState o2) {
                return Double.compare(o1.incoming_weight, o2.incoming_weight);
            }
        });

        for (VertexState vs : vstates) {
            vs.incoming_weight = Double.POSITIVE_INFINITY;
            vs.treenode = null;
            queue.add(vs);
        }

        List<DirectedTreeNode<TGraph, TGeom, TVertex, TEdge>> roots = new ArrayList();

        while (!queue.isEmpty()) {
            VertexState vs = queue.poll();
            lengthOfLastQuery += Double.isFinite(vs.incoming_weight) ? vs.incoming_weight : 0;
            vs.treenode = new DirectedTreeNode(vs.vertex, vs.incoming_edge);

            if (vs.incoming_edge == null) {
                // new component, add this as a root node
                roots.add(vs.treenode);
            } else {
                // existing component, add it as a child the the node of the source                
                vstates.get(vs.incoming_edge.getOtherVertex(vs.vertex).getGraphIndex()).treenode.getChildrenNodes().add(vs.treenode);
            }
            for (TEdge edge : vs.vertex.getEdges()) {
                TVertex neighbor = edge.getOtherVertex(vs.vertex);
                VertexState nbrstate = vstates.get(neighbor.getGraphIndex());
                if (nbrstate.getIndex() >= 0) {
                    double w = ewi.getEdgeWeight(edge);
                    if (w < nbrstate.incoming_weight) {
                        nbrstate.incoming_edge = edge;
                        nbrstate.incoming_weight = w;
                        queue.priorityIncreased(nbrstate);
                    }
                }
            }
        }

        return roots;
    }

    /**
     * Computes the minimum spanning tree of the component containing the given
     * vertex.
     *
     * @param vertex vertex of the desired component
     * @return a directed tree, rooted at the given vertex
     */
    public DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> computeMinimumSpanningTree(TVertex vertex) {
        assert graph.getVertices().get(vertex.getGraphIndex()) == vertex;

        lengthOfLastQuery = 0;

        IndexedPriorityQueue<VertexState> queue = new IndexedPriorityQueue(vstates.size(), new Comparator<VertexState>() {

            @Override
            public int compare(VertexState o1, VertexState o2) {
                return Double.compare(o1.incoming_weight, o2.incoming_weight);
            }
        });

        for (VertexState vs : vstates) {
            if (vs.vertex == vertex) {
                vs.incoming_weight = 0;
                queue.add(vs);
            } else {
                vs.incoming_weight = Double.POSITIVE_INFINITY;
            }
            vs.treenode = null;
        }

        while (!queue.isEmpty()) {
            VertexState vs = queue.poll();
            lengthOfLastQuery += vs.incoming_weight;
            vs.treenode = new DirectedTreeNode(vs.vertex, vs.incoming_edge);
            if (vs.incoming_edge == null) {
                assert vs.vertex == vertex;
            } else {
                // existing component, add it as a child the the node of the source                
                vstates.get(vs.incoming_edge.getOtherVertex(vs.vertex).getGraphIndex()).treenode.getChildrenNodes().add(vs.treenode);
            }
            for (TEdge edge : vs.vertex.getEdges()) {
                TVertex neighbor = edge.getOtherVertex(vs.vertex);
                VertexState nbrstate = vstates.get(neighbor.getGraphIndex());
                boolean inqueue = nbrstate.getIndex() >= 0;
                double w = ewi.getEdgeWeight(edge);
                if ((inqueue && w < nbrstate.incoming_weight)
                        || (!inqueue && Double.isInfinite(nbrstate.incoming_weight))) {
                    nbrstate.incoming_edge = edge;
                    nbrstate.incoming_weight = w;
                    if (inqueue) {
                        queue.priorityIncreased(nbrstate);
                    } else {
                        queue.add(nbrstate);
                    }
                }
            }
        }

        return vstates.get(vertex.getGraphIndex()).treenode;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private class VertexState extends BasicIndexable {

        final TVertex vertex;
        boolean in_forest = false;
        TEdge incoming_edge = null;
        double incoming_weight = Double.POSITIVE_INFINITY;
        DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> treenode = null;

        public VertexState(TVertex vertex) {
            this.vertex = vertex;
        }
    }
    //</editor-fold>    
}
