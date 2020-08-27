/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.dcel;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @param <TGeom> The geometry of an edge, must inherit from OrientedGeometry
 * @param <TVertex> The class of a vertex
 * @param <TDart> The class of a dart
 * @param <TFace> The class of a face
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class DCELGraph<TGeom extends OrientedGeometry, TVertex extends DCELVertex<TGeom, TVertex, TDart, TFace>, TDart extends DCELDart<TGeom, TVertex, TDart, TFace>, TFace extends DCELFace<TGeom, TVertex, TDart, TFace>> {

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------
    final List<TFace> _faces; // NB: face 0 will always be the general outerface
    final List<TDart> _darts; // NB: saves only one of two darts representing an edge
    final List<TVertex> _vertices;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------
    public DCELGraph() {
        _faces = new ArrayList<TFace>();
        _darts = new ArrayList<TDart>();
        _vertices = new ArrayList<TVertex>();

        initializeOuterFace();
    }

    private void initializeOuterFace() {
        TFace outerface = createFace();
        outerface._graphIndex = 0;
        _faces.add(outerface);
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------
    public List<TFace> getFaces() {
        return _faces;
    }

    public List<TDart> getDarts() {
        return _darts;
    }

    public List<TVertex> getVertices() {
        return _vertices;
    }

    // -------------------------------------------------------------------------
    // ABSTRACT METHODS
    // -------------------------------------------------------------------------
    public abstract TVertex createVertex(double x, double y);

    public abstract TDart createDart();

    public abstract TFace createFace();

    // -------------------------------------------------------------------------
    // GRAPH ADDITIONS
    // -------------------------------------------------------------------------
    public TVertex addVertex(Vector loc) {
        return addVertex(loc.getX(), loc.getY());
    }

    public TVertex addVertex(double x, double y) {
        TVertex vertex = createVertex(x, y);

        addVertexToVertexList(vertex);

        TFace face = computeContainingProperFace(vertex, false);
        face._proper_floatingVertices.add(vertex);

        return vertex;
    }

    public void addVertexToVertexList(TVertex vertex) {
        vertex._graphIndex = _vertices.size();
        _vertices.add(vertex);
    }

    public void addDartToDartList(TDart dart) {
        dart._graphIndex = _darts.size();
        _darts.add(dart);
    }

    public void addFaceToFaceList(TFace face) {
        face._graphIndex = _faces.size();
        _faces.add(face);
    }

    /**
     * O(1)-time operation as it does not cause topological changes in faces.
     */
    public TVertex splitEdge(TDart split, Vector position, TGeom geomOriginToNew, TGeom geomOriginToNewReversed, TGeom geomNewToDestination, TGeom geomNewToDestinationReversed) {

        TVertex from = split._origin;
        TVertex to = split._twin._origin;

        // create a new vertex
        // NB: no need to find enclosing face, as we're using it to split an edge
        TVertex splitvertex = createVertex(position.getX(), position.getY());

        splitvertex._graphIndex = _vertices.size();
        _vertices.add(splitvertex);

        TDart dart = createDart();
        TDart dartRev = createDart();

        // store only one dart in the dart list
        addDartToDartList(dart);

        // set geometry
        split._geometry = geomOriginToNew;
        split._geometry.updateEndpoints(from, splitvertex);

        split._twin._geometry = geomOriginToNewReversed;
        split._twin._geometry.updateEndpoints(splitvertex, from);

        dart._geometry = geomNewToDestination;
        dart._geometry.updateEndpoints(splitvertex, to);

        dartRev._geometry = geomNewToDestinationReversed;
        dartRev._geometry.updateEndpoints(to, splitvertex);

        // set next/prev pointers
        if (split._next == split._twin) {
            // destination is degree 1
            dart._next = dartRev;
            dartRev._previous = dart;
        } else {
            dart._next = split._next;
            dart._next._previous = dart;
            dartRev._previous = split._twin._previous;
            dartRev._previous._next = dartRev;
        }
        split._next = dart;
        dart._previous = split;
        split._twin._previous = dartRev;
        dartRev._next = split._twin;

        // set faces
        dart._face = split._face;
        dartRev._face = split._twin._face;

        return splitvertex;
    }

//    public TDart addEdge(TVertex from, TVertex to, TGeom geometry, TGeom reversed) {
//
//        TDart dart = createDart();
//        TDart dartRev = createDart();
//
//        // store only one dart in the dart list
//        addDartToDartList(dart);
//
//        // set geometry
//        dart._geometry = geometry;
//        dart._geometry.setStart(from);
//        dart._geometry.setEnd(to);
//
//        dartRev._geometry = reversed;
//        dartRev._geometry.setStart(to);
//        dartRev._geometry.setEnd(from);
//
//        // set origins
//        dart._origin = from;
//        dartRev._origin = to;
//
//        // set twins
//        dart._twin = dartRev;
//        dartRev._twin = dart;
//
//        // set next/prev at from
//        // set next/prev at to
//        // create/update faces
//        return dart;
//    }
    // -------------------------------------------------------------------------
    // GRAPH REMOVALS
    // -------------------------------------------------------------------------
    /**
     * O(n)-time operations as faces may need to be merged.
     *
     * @param vertex
     */
    public void removeVertex(TVertex vertex) {
        assert vertex._floatingInFace != null;

        vertex._floatingInFace._proper_floatingVertices.remove(vertex);

        removeVertexFromList(vertex);
    }

    /**
     * O(1)-time operation as it does not cause topological changes in faces.
     *
     * @param out
     * @param geometry
     * @param reversed
     */
    public void mergeDartAtOrigin(TDart out, TGeom geometry, TGeom reversed) {

        assert out._origin.isDegree(2) : "removeVertexAndReconnect() can be used only on degree-2 vertices...";

        removeVertexFromList(out._origin);
        removeDartFromList(out);

        final TDart inc = out._previous;
        if (out._face._dart == out) {
            out._face._dart = inc;
        }
        if (out._twin._face._dart == out._twin) {
            out._twin._face._dart = out._twin._next;
        }

        // update DCEL with shortcut
        // switch next pointer of inc to pass over vertex
        inc._next = out._next;
        inc._next._previous = inc;

        // switch prev pointer of inc.twin to pass over vertex and update its origin        
        inc._twin._origin = out._twin._origin;
        inc._twin._origin._dart = inc._twin;
        inc._twin._previous = out._twin._previous;
        inc._twin._previous._next = inc._twin;

        // update geometry
        inc._geometry = geometry;
        inc._geometry.updateEndpoints(inc._origin, inc._twin._origin);
        inc._twin._geometry = reversed;
        inc._twin._geometry.updateEndpoints(inc._twin._origin, inc._origin);
    }

//    /**
//     * O(n)-time operations as faces may need to be merged.
//     *
//     * @param dart
//     */
//    public void removeEdge(TDart dart) {
//        // Four cases, depending on degree of endpoints
//        // - Origin degree = 1 or Origin degree > 1
//        // - Destination degree = 1 or Destination degree > 1
//
//        boolean originIsDegreeOne = dart._twin._next == dart;
//        boolean destinationIsDegreeOne = dart._twin._previous == dart;
//
//        TFace face = dart._face;
//
//        if (originIsDegreeOne && destinationIsDegreeOne) {
//            // both degree one, a floating dart is vanishing, creating two floating vertices
//
//            face._floatingDarts.remove(dart);
//            face._floatingDarts.remove(dart._twin);
//
//            dart._origin._outgoing = null;
//            face._floatingVertices.add(dart._origin);
//
//            dart._twin._origin._outgoing = null;
//            face._floatingVertices.add(dart._twin._origin);
//
//        } else if (originIsDegreeOne && !destinationIsDegreeOne) {
//            // only one incident face, so no need to merge those
//            // creates one floating vertex
//
//            dart._next._previous = dart._twin._previous;
//            dart._twin._previous._next = dart._next;
//
//            dart._origin._outgoing = null;
//            face._floatingVertices.add(dart._origin);
//
//        } else if (!originIsDegreeOne && destinationIsDegreeOne) {
//            // only one incident face, so no need to merge those
//            // creates one floating vertex
//
//            dart._previous._next = dart._twin._next;
//            dart._twin._next._previous = dart._previous;
//
//            dart._twin._origin._outgoing = null;
//            face._floatingVertices.add(dart._twin._origin);
//
//        } else {
//            // !originIsDegreeOne && !destinationIsDegreeOne 
//            dart._next._previous = dart._twin._previous;
//            dart._twin._previous._next = dart._next;
//
//            dart._previous._next = dart._twin._next;
//            dart._twin._next._previous = dart._previous;
//
//            TFace twinface = dart._twin._face;
//            if (face != twinface) {
//                // two incident faces, need to merge those
//                // creates no floating vertices
//                // NB: must check whether one is contained in the other
//
//                if (face._floatingFaces.contains(twinface)) {
//                    // twinface contained in face
//
//                    removeFaceFromList(twinface);
//
//                    face._floatingFaces.addAll(twinface._floatingFaces);
//                    face._floatingDarts.addAll(twinface._floatingDarts);
//                    face._floatingVertices.addAll(twinface._floatingVertices);
//
//                } else if (twinface._floatingFaces.contains(face)) {
//                    // face contained in twinface
//                } else {
//                    // proper adjacency on the same level
//                }
//
//                TDart walk = dart._twin._next;
//                while (walk != dart._twin) {
//                    walk._face = face;
//
//                    walk = walk._next;
//                }
//
//            } else {
//                // one incident face, must create a hole in an inner face
//
//            }
//        }
//
//        removeDartFromList(dart);
//    }
    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------
    /**
     * This method ensures a containment order on faces. In other words, suppose
     * that face F that encloses another face H, then F will come before H in
     * the list of faces.
     *
     * This operation takes O(n) time, for n faces.
     */
    public void sortToContainmentOrder() {

        assert _faces.get(0).isOuterFace();

        final int nfaces = _faces.size();

        // first face is outerface
        // everything < head is processed
        // everything < tail is discovered, but not processed
        // everything >= tail is undiscovered
        int headFace = 0;
        int tailFace = 1;

        // must assign nesting depth, we do this on discovery
        // i.e., everything < tail must have its nesting depth set
        _faces.get(0)._nestingdepth = 0;

        while (tailFace < nfaces) {
            TFace face = _faces.get(headFace);
            headFace++;

            if (face.isProperFace()) {

                // proper face: process the floating components
                for (TFace floater : face._proper_floatingComponents) {
                    floater._nestingdepth = face._nestingdepth;
                    swapSetFace(tailFace, floater);
                    tailFace++;
                }
            } else {
                // floating face: process the contained proper faces
                for (TFace contained : face._floating_containedProperFaces) {
                    contained._nestingdepth = face._nestingdepth + 1;
                    swapSetFace(tailFace, contained);
                    tailFace++;
                }
            }

        }// end discovery of faces
    }

    public TFace getOuterFace() {
        return _faces.get(0);
    }

    public TFace computeContainingProperFace(Vector point, boolean rimToInnerFace) {
        return recurseWithEnclosingFace(point, _faces.get(0), rimToInnerFace);
    }

    private TFace recurseWithEnclosingFace(Vector point, TFace enclosing, boolean rimToInnerFace) {
        assert enclosing.isProperFace();

        // TODO: something goes wrong here
        for (TFace floater : enclosing._proper_floatingComponents) {
            for (TFace face : floater._floating_containedProperFaces) {
                if (face.enclosesPoint(point, rimToInnerFace)) {
                    return recurseWithEnclosingFace(point, face, rimToInnerFace);
                }
            }
        }

        return enclosing;
    }

    // -------------------------------------------------------------------------
    // INTERNAL METHODS
    // -------------------------------------------------------------------------
    private void swapSetFace(int index, TFace face) {
        if (face._graphIndex != index) {
            TFace other = _faces.get(index);

            _faces.set(face._graphIndex, other);
            other._graphIndex = face._graphIndex;

            _faces.set(index, face);
            face._graphIndex = index;
        }
    }

    private void removeVertexFromList(TVertex vertex) {
        TVertex last = _vertices.remove(_vertices.size() - 1);

        if (last != vertex) {
            _vertices.set(vertex._graphIndex, last);
            last._graphIndex = vertex._graphIndex;
        }

        vertex._graphIndex = -1;
    }

    private void removeDartFromList(TDart dart) {
        if (dart._graphIndex < 0) {
            dart = dart._twin;
        }

        TDart last = _darts.remove(_darts.size() - 1);
        if (last != dart) {
            _darts.set(dart._graphIndex, last);
            last._graphIndex = dart._graphIndex;
        }

        dart._graphIndex = -1;
    }

    private void removeFaceFromList(TFace face) {
        assert face.isOuterFace() : "Cannot remove outerface...";

        TFace last = _faces.remove(_faces.size() - 1);
        if (last != face) {
            _faces.set(face._graphIndex, last);
            last._graphIndex = face._graphIndex;
        }

        face._graphIndex = -1;
    }

    /**
     * Debugging routine.
     */
    public void verify() {
        System.err.println("Starting verification");
        // verify indices
        for (TVertex vertex : _vertices) {
            assert !Double.isInfinite(vertex.getX());
            assert !Double.isNaN(vertex.getX());
            assert !Double.isInfinite(vertex.getY());
            assert !Double.isNaN(vertex.getY());
            assert vertex == _vertices.get(vertex.getGraphIndex());
        }
        for (TDart dart : _darts) {
            assert dart == _darts.get(dart.getGraphIndex());
        }
        for (TFace face : _faces) {
            assert face == _faces.get(face.getGraphIndex());
        }

        // verify face darts
        for (TFace face : _faces) {
            if (!face.isOuterFace()) {
                assert face == face.getDart().getFace();
                assert face.getDart().getGraphIndex() >= 0 || face.getDart().getTwin().getGraphIndex() >= 0;
            }
        }

        // verify prev=next/twin
        for (TDart dart : _darts) {
            assert dart == dart.getNext().getPrevious();
            assert dart == dart.getTwin().getTwin();
        }

        // verify same faces for prev=next dart
        for (TDart dart : _darts) {
            assert dart.getFace() == dart.getNext().getFace();
        }

        // check floating vertices
        int nfloat = 0;
        for (TVertex vertex : _vertices) {
            if (vertex.isDegree(0)) {
                nfloat++;
            }
        }
        for (TFace face : _faces) {
            assert face.isProperFace() || face.getFloatingVertices().isEmpty();

            for (TVertex floater : face.getFloatingVertices()) {
                assert !floater.isMarked();

                floater.setMarked(true);
                nfloat--;
            }
        }
        for (TVertex vertex : _vertices) {
            vertex.setMarked(false);
        }
        assert nfloat == 0;

        // check whether walking around a face, starting from any dart is not an infinite loop...
        for (TDart dart : _darts) {

            TDart walk = dart;
            int remaining = _darts.size() * 2 + 2;
            do {
                assert remaining > 0;

                walk = walk.getNext();
                remaining--;
            } while (walk != dart);

            dart = dart.getTwin();
            walk = dart;
            remaining = _darts.size() * 2 + 2;
            do {
                assert remaining > 0;

                walk = walk.getNext();
                remaining--;
            } while (walk != dart);
        }

        System.err.println("Verification passed");
    }
}
