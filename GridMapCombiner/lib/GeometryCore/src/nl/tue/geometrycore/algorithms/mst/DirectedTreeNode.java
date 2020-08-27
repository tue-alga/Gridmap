/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.mst;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

/**
 * Directed tree to represent a tree contained in another graph structure.
 *
 * @param <TGraph> Class of the containing graph
 * @param <TGeom> Class of the containing graph's geometry
 * @param <TVertex> Class of the containing graph's vertices
 * @param <TEdge> Class of the containing graph's edges
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DirectedTreeNode<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final TEdge incoming;
    private final TVertex vertex;
    private final List<DirectedTreeNode<TGraph, TGeom, TVertex, TEdge>> children = new ArrayList();
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Creates a new leaf into the tree, following the given edge. The incoming
     * edge is set to null for the root of the tree.
     *
     * @param vertex the new leaf
     * @param incoming incoming edge
     */
    public DirectedTreeNode(TVertex vertex, TEdge incoming) {
        this.vertex = vertex;
        this.incoming = incoming;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Returns the incoming edge in the directed tree of this node.
     * 
     * @return the incoming edge
     */
    public TEdge getIncomingEdge() {
        return incoming;
    }

    /**
     * Tests whether this node is the root of its tree.
     * 
     * @return true iff the node has no incoming edge
     */
    public boolean isRoot() {
        return incoming == null;
    }

    /**
     * Tests whether this node is a leaf of its tree
     * 
     * @return true iff the node has no children
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns the degree of the this node in its tree.
     * 
     * @return the number of children
     */
    public int getDegree() {
        return children.size();
    }

    /**
     * Returns the actual vertex in the base graph that this tree is contained in.
     * 
     * @return the graph vertex
     */
    public TVertex getVertex() {
        return vertex;
    }

    /**
     * Returns the children of this node. Note that modifying the returned lists modifies the tree.
     * 
     * @return 
     */
    public List<DirectedTreeNode<TGraph, TGeom, TVertex, TEdge>> getChildrenNodes() {
        return children;
    }

    public List<TEdge> getOutgoingEdges() {
        List<TEdge> edges = new ArrayList();
        for (DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> child : children) {
            edges.add(child.incoming);
        }
        return edges;
    }

    public List<TVertex> getChildren() {
        List<TVertex> vertices = new ArrayList();
        for (DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> child : children) {
            vertices.add(child.vertex);
        }
        return vertices;
    }

    public List<TVertex> getAllVerticesInSubtree() {
        List<TVertex> vertices = new ArrayList();
        recurseVertex(vertices);
        return vertices;
    }

    public List<TEdge> getAllEdgesInSubtree() {
        List<TEdge> edges = new ArrayList();
        for (DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> child : children) {
            child.recurseEdge(edges);
        }
        return edges;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void recurseVertex(List<TVertex> vertices) {
        vertices.add(vertex);
        for (DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> child : children) {
            child.recurseVertex(vertices);
        }
    }

    private void recurseEdge(List<TEdge> edges) {
        edges.add(incoming);
        for (DirectedTreeNode<TGraph, TGeom, TVertex, TEdge> child : children) {
            child.recurseEdge(edges);
        }
    }
    //</editor-fold>    
}
