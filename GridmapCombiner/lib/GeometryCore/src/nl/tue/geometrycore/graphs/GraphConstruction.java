/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryCloner;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.graphs.dcel.DCELDart;
import nl.tue.geometrycore.graphs.dcel.DCELFace;
import nl.tue.geometrycore.graphs.dcel.DCELGraph;
import nl.tue.geometrycore.graphs.dcel.DCELVertex;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GraphConstruction {

    // -------------------------------------------------------------------------
    // SIMPLE GRAPH -> DCEL 
    // -------------------------------------------------------------------------
    public static <TDCELGeom extends OrientedGeometry<TDCELGeom>, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertSimpleGraphToDCEL(TDCELGraph dcel, TSimpleGraph graph, GeometryCloner<TSimpleGeom, TDCELGeom> cloner) {
        convertSimpleGraphToDCEL(dcel, graph, cloner, null, null, null, null);
    }

    public static <TDCELGeom extends OrientedGeometry<TDCELGeom>, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertSimpleGraphToDCEL(TDCELGraph dcel, TSimpleGraph graph, GeometryCloner<TSimpleGeom, TDCELGeom> cloner,
                    Map<TSimpleVertex, TDCELVertex> vertexMap, Map<TDCELVertex, TSimpleVertex> vertexBackmap,
                    Map<TSimpleEdge, TDCELDart> edgeMap, Map<TDCELDart, TSimpleEdge> edgeBackmap) {

        if (vertexMap == null) {
            vertexMap = new HashMap<TSimpleVertex, TDCELVertex>();
        }
        if (edgeMap == null) {
            edgeMap = new HashMap<TSimpleEdge, TDCELDart>();
        }

        // make vertices
        for (TSimpleVertex vertex : graph.getVertices()) {

            TDCELVertex dcelvertex = dcel.createVertex(vertex.getX(), vertex.getY());
            dcel.addVertexToVertexList(dcelvertex);
            vertexMap.put(vertex, dcelvertex);

            if (vertexBackmap != null) {
                vertexBackmap.put(dcelvertex, vertex);
            }
        }

        // make darts
        for (TSimpleEdge edge : graph.getEdges()) {
            TDCELVertex dvStart = vertexMap.get(edge.getStart());
            TDCELVertex dvEnd = vertexMap.get(edge.getEnd());

            TDCELDart startToEnd = dcel.createDart();
            startToEnd.setOrigin(dvStart);
            dvStart.setDart(startToEnd);
            dcel.addDartToDartList(startToEnd);

            startToEnd.setGeometry(cloner.clone(edge.getGeometry()));

            TDCELDart endToStart = dcel.createDart();
            endToStart.setOrigin(dvEnd);
            dvEnd.setDart(endToStart);
            // storing only one dart

            TDCELGeom revgeom = cloner.clone(edge.getGeometry());
            revgeom.reverse();
            endToStart.setGeometry(revgeom);

            endToStart.setTwin(startToEnd);
            startToEnd.setTwin(endToStart);

            edgeMap.put(edge, startToEnd);
            if (edgeBackmap != null) {
                edgeBackmap.put(startToEnd, edge);
            }
        }

        // For each vertex, find the cyclic order of outgoing darts
        // The next vertex in the cyclic order is the next of your twin
        // So we can compute the next of all incoming darts and the previous of all outgoing darts this way
        for (TSimpleVertex vertex : graph.getVertices()) {
            // Sort the edges of this vertex in cyclic (clockwise) order
            vertex.sortEdges(false);

            final int degree = vertex.getDegree();
            List<TDCELDart> outgoingDarts = new ArrayList<TDCELDart>(degree);

            DCELVertex emvertex = vertexMap.get(vertex);

            for (TSimpleEdge edge : vertex.getEdges()) {
                // Find the outgoing dart that corresponds to this edge
                TDCELDart outgoing = edgeMap.get(edge);

                assert outgoing != null;

                if (outgoing.getOrigin() != emvertex) {
                    outgoing = outgoing.getTwin();
                }

                assert outgoing != null;

                outgoingDarts.add(outgoing);
            }

            for (int i = 0; i < degree; i++) {
                // The next of the twin of this dart is the next dart in clockwise order around the vertex
                TDCELDart dart = outgoingDarts.get(i);
                TDCELDart nextDart = outgoingDarts.get((i + 1) % degree);

                dart.getTwin().setNext(nextDart);
                nextDart.setPrevious(dart.getTwin());
            }
        }

        // make and set faces
        List<TDCELFace> counterclockwisecycles = new ArrayList();
        List<TDCELFace> clockwisecycles = new ArrayList();

        for (TDCELDart dart : dcel.getDarts()) {
            if (!dart.isMarked()) {
                // face has not been explored

                List<TDCELDart> cycle = new ArrayList();
                double cycleArea = 0;

                TDCELDart walkDart = dart;
                do {
                    cycle.add(walkDart);
                    walkDart.setMarked(true);

                    cycleArea += Vector.crossProduct(walkDart.getOrigin(), walkDart.getDestination());
                    walkDart = walkDart.getNext();
                } while (walkDart != dart);

                TDCELFace face = dcel.createFace();
                face.setContainingProperFace(null);
                dcel.addFaceToFaceList(face);

                face.setDart(dart);
                for (TDCELDart d : cycle) {
                    d.setFace(face);
                }

                if (cycleArea > DoubleUtil.EPS) {
                    // counterclockwise face
                    counterclockwisecycles.add(face);
                } else {
                    clockwisecycles.add(face);
                }
            }

            dart = dart.getTwin();

            if (!dart.isMarked()) {
                List<TDCELDart> cycle = new ArrayList();
                double cycleArea = 0;

                TDCELDart walkDart = dart;
                do {
                    cycle.add(walkDart);
                    walkDart.setMarked(true);

                    cycleArea += Vector.crossProduct(walkDart.getOrigin(), walkDart.getDestination());
                    walkDart = walkDart.getNext();
                } while (walkDart != dart);

                TDCELFace face = dcel.createFace();

                dcel.addFaceToFaceList(face);

                face.setDart(dart);
                for (TDCELDart d : cycle) {
                    d.setFace(face);
                }

                if (cycleArea > DoubleUtil.EPS) {
                    // counterclockwise face
                    counterclockwisecycles.add(face);
                } else {
                    clockwisecycles.add(face);
                }
            }
        }

        for (TDCELDart dart : dcel.getDarts()) {
            // unmark
            dart.setMarked(false);
            dart.getTwin().setMarked(false);
        }

        for (TDCELFace floater : clockwisecycles) {

            TDCELVertex test = floater.getDart().getOrigin();

            // find smallest face that contains this cycle
            TDCELFace face = dcel.getOuterFace();
            double lowerbound = -floater.computeArea(false);
            double facearea = Double.POSITIVE_INFINITY;
            for (TDCELFace exface : counterclockwisecycles) {
                if (exface.enclosesPoint(test, false)) {
                    double area = exface.computeArea(false);
                    if (lowerbound < area + DoubleUtil.EPS && area < facearea) {
                        facearea = area;
                        face = exface;
                    }
                }
            }

            face.getFloatingComponents().add(floater);
            floater.setContainingProperFace(face);
        }

        for (TDCELFace face : dcel.getFaces()) {
            face.recomputeContainedProperFaces();
        }

        // set floating vertices to appropriate faces
        for (TDCELVertex vertex : dcel.getVertices()) {
            if (vertex.getDart() == null) {
                TDCELFace face = dcel.computeContainingProperFace(vertex, false);

                assert face.isProperFace();

                face.getFloatingVertices().add(vertex);
                vertex.setFloatingInFace(face);
            }
        }

        dcel.sortToContainmentOrder();
    }

    // -------------------------------------------------------------------------
    // DCEL -> SIMPLE GRAPH
    // -------------------------------------------------------------------------
    public static <TDCELGeom extends OrientedGeometry<TDCELGeom>, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertDCELToSimpleGraph(TSimpleGraph graph, TDCELGraph dcel, GeometryCloner<TDCELGeom, TSimpleGeom> cloner) {
        convertDCELToSimpleGraph(graph, dcel, cloner, null, null, null, null);
    }

    public static <TDCELGeom extends OrientedGeometry<TDCELGeom>, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertDCELToSimpleGraph(TSimpleGraph graph, TDCELGraph dcel, GeometryCloner<TDCELGeom, TSimpleGeom> cloner,
                    Map<TDCELVertex, TSimpleVertex> vertexMap, Map<TSimpleVertex, TDCELVertex> vertexBackmap,
                    Map<TDCELDart, TSimpleEdge> edgeMap, Map<TSimpleEdge, TDCELDart> edgeBackmap) {

        if (vertexMap == null) {
            vertexMap = new HashMap<TDCELVertex, TSimpleVertex>();
        }
        if (edgeMap == null) {
            edgeMap = new HashMap<TDCELDart, TSimpleEdge>();
        }

        // make vertices
        for (TDCELVertex vertex : dcel.getVertices()) {

            TSimpleVertex vtx = graph.addVertex(vertex.getX(), vertex.getY());
            vertexMap.put(vertex, vtx);

            if (vertexBackmap != null) {
                vertexBackmap.put(vtx, vertex);
            }
        }

        // make darts
        for (TDCELDart dart : dcel.getDarts()) {
            TSimpleVertex dvStart = vertexMap.get(dart.getOrigin());
            TSimpleVertex dvEnd = vertexMap.get(dart.getDestination());

            TSimpleEdge startToEnd = graph.addEdge(dvStart, dvEnd, cloner.clone(dart.getGeometry()));

            edgeMap.put(dart, startToEnd);
            if (edgeBackmap != null) {
                edgeBackmap.put(startToEnd, dart);
            }
        }
    }

    // -------------------------------------------------------------------------
    // GEOMETRY -> SIMPLE GRAPH
    // -------------------------------------------------------------------------
    public static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertGeometriesToGraph(TSimpleGraph graph, List<? extends GeometryConvertable> geometries,
                    GeometryCloner<OrientedGeometry, TSimpleGeom> cloner) {
        convertGeometriesToGraph(graph, geometries, DoubleUtil.EPS, cloner, null, null, null, null);
    }

    public static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertGeometriesToGraph(TSimpleGraph graph, List<? extends GeometryConvertable> geometries, double precision,
                    GeometryCloner<OrientedGeometry, TSimpleGeom> cloner) {
        convertGeometriesToGraph(graph, geometries, precision, cloner, null, null, null, null);
    }

    public static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void convertGeometriesToGraph(TSimpleGraph graph, List<? extends GeometryConvertable> geometries, double precision,
                    GeometryCloner<OrientedGeometry, TSimpleGeom> cloner,
                    Map<GeometryConvertable, List<TSimpleVertex>> vertexMap, Map<TSimpleVertex, List<GeometryConvertable>> vertexBackmap,
                    Map<GeometryConvertable, List<TSimpleEdge>> edgeMap, Map<TSimpleEdge, List<GeometryConvertable>> edgeBackmap) {

        for (GeometryConvertable gc : geometries) {
            BaseGeometry g = gc.toGeometry();

            if (vertexMap != null) {
                vertexMap.put(gc, new ArrayList());
            }
            if (edgeMap != null) {
                edgeMap.put(gc, new ArrayList());
            }

            addGeometryToGraph(graph, gc, g, precision, cloner, vertexMap, vertexBackmap, edgeMap, edgeBackmap);

        }
    }

    private static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            void addGeometryToGraph(TSimpleGraph graph, GeometryConvertable base, BaseGeometry geometry, double precision,
                    GeometryCloner<OrientedGeometry, TSimpleGeom> cloner,
                    Map<GeometryConvertable, List<TSimpleVertex>> vertexMap, Map<TSimpleVertex, List<GeometryConvertable>> vertexBackmap,
                    Map<GeometryConvertable, List<TSimpleEdge>> edgeMap, Map<TSimpleEdge, List<GeometryConvertable>> edgeBackmap) {

        switch (geometry.getGeometryType()) {
            case VECTOR: {
                Vector vector = (Vector) geometry;
                getOrAddVertex(vector, graph, precision, base, vertexMap, vertexBackmap);
                break;
            }
            case LINESEGMENT: {
                LineSegment segment = (LineSegment) geometry;
                TSimpleVertex start = getOrAddVertex(segment.getStart(), graph, precision, base, vertexMap, vertexBackmap);
                TSimpleVertex end = getOrAddVertex(segment.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                getOrAddEdge(start, end, cloner.clone(segment), graph, base, edgeMap, edgeBackmap);
                break;
            }
            case CIRCULARARC: {
                CircularArc arc = (CircularArc) geometry;
                TSimpleVertex start = getOrAddVertex(arc.getStart(), graph, precision, base, vertexMap, vertexBackmap);
                TSimpleVertex end = getOrAddVertex(arc.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                getOrAddEdge(start, end, cloner.clone(arc), graph, base, edgeMap, edgeBackmap);
                break;
            }
            case POLYLINE: {
                PolyLine polyline = (PolyLine) geometry;
                TSimpleVertex start = getOrAddVertex(polyline.vertex(0), graph, precision, base, vertexMap, vertexBackmap);
                for (LineSegment segment : polyline.edges()) {
                    if (segment.squaredLength() < precision * precision) {
                        continue;
                    }
                    TSimpleVertex end = getOrAddVertex(segment.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                    getOrAddEdge(start, end, cloner.clone(segment), graph, base, edgeMap, edgeBackmap);
                    start = end;
                }
                break;
            }
            case POLYGON: {
                Polygon polygon = (Polygon) geometry;
                TSimpleVertex start = getOrAddVertex(polygon.vertex(0), graph, precision, base, vertexMap, vertexBackmap);
                for (LineSegment segment : polygon.edges()) {
                    if (segment.squaredLength() < precision * precision) {
                        continue;
                    }
                    TSimpleVertex end = getOrAddVertex(segment.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                    getOrAddEdge(start, end, cloner.clone(segment), graph, base, edgeMap, edgeBackmap);
                    start = end;
                }
                break;
            }
            case RECTANGLE: {
                Rectangle rect = (Rectangle) geometry;
                TSimpleVertex leftbottom = getOrAddVertex(rect.leftBottom(), graph, precision, base, vertexMap, vertexBackmap);
                TSimpleVertex lefttop = getOrAddVertex(rect.leftTop(), graph, precision, base, vertexMap, vertexBackmap);
                TSimpleVertex rightbottom = getOrAddVertex(rect.rightBottom(), graph, precision, base, vertexMap, vertexBackmap);
                TSimpleVertex righttop = getOrAddVertex(rect.rightTop(), graph, precision, base, vertexMap, vertexBackmap);
                getOrAddEdge(lefttop, leftbottom, cloner.clone(rect.leftSide()), graph, base, edgeMap, edgeBackmap);
                getOrAddEdge(leftbottom, rightbottom, cloner.clone(rect.rightSide()), graph, base, edgeMap, edgeBackmap);
                getOrAddEdge(rightbottom, righttop, cloner.clone(rect.bottomSide()), graph, base, edgeMap, edgeBackmap);
                getOrAddEdge(righttop, lefttop, cloner.clone(rect.topSide()), graph, base, edgeMap, edgeBackmap);
                break;
            }
            case GEOMETRYSTRING: {
                GeometryString<? extends OrientedGeometry> string = (GeometryString) geometry;
                TSimpleVertex start = getOrAddVertex(string.vertex(0), graph, precision, base, vertexMap, vertexBackmap);
                for (OrientedGeometry edge : string.edges()) {
                    TSimpleVertex end = getOrAddVertex(edge.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                    getOrAddEdge(start, end, cloner.clone(edge), graph, base, edgeMap, edgeBackmap);
                    start = end;
                }
                break;
            }
            case GEOMETRYCYCLE: {
                GeometryCycle<? extends OrientedGeometry> cycle = (GeometryCycle) geometry;
                TSimpleVertex start = getOrAddVertex(cycle.vertex(0), graph, precision, base, vertexMap, vertexBackmap);
                for (OrientedGeometry edge : cycle.edges()) {
                    TSimpleVertex end = getOrAddVertex(edge.getEnd(), graph, precision, base, vertexMap, vertexBackmap);
                    getOrAddEdge(start, end, cloner.clone(edge), graph, base, edgeMap, edgeBackmap);
                    start = end;
                }
                break;
            }
            case GEOMETRYGROUP: {
                GeometryGroup<? extends BaseGeometry> group = (GeometryGroup) geometry;
                for (BaseGeometry part : group.getParts()) {
                    addGeometryToGraph(graph, base, part, precision, cloner, vertexMap, vertexBackmap, edgeMap, edgeBackmap);
                }
                break;
            }
            case HALFLINE:
            case LINE:
                Logger.getLogger(GraphConstruction.class.getName()).log(Level.WARNING,
                        "Cannot add infinite geometries ({0}) to graphs",
                        geometry.getClass().getName());
                break;
            default:
                Logger.getLogger(GraphConstruction.class.getName()).log(Level.WARNING,
                        "Unexcepted geometry type in addGeometryToGraph(): {0}",
                        geometry.getClass().getName());
                break;
        }
    }

    private static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            TSimpleVertex getOrAddVertex(Vector position, TSimpleGraph graph, double precision,
                    GeometryConvertable base, Map<GeometryConvertable, List<TSimpleVertex>> vertexMap, Map<TSimpleVertex, List<GeometryConvertable>> vertexBackmap) {

        TSimpleVertex result = null;

        for (TSimpleVertex vertex : graph.getVertices()) {
            if (vertex.isApproximately(position, precision)) {
                result = vertex;
                break;
            }
        }

        if (result == null) {
            result = graph.addVertex(position);

            if (vertexBackmap != null) {
                vertexBackmap.put(result, new ArrayList());
            }
        }

        if (vertexMap != null) {
            vertexMap.get(base).add(result);
        }

        if (vertexBackmap != null) {
            vertexBackmap.get(result).add(base);
        }

        return result;
    }

    private static <TSimpleGeom extends OrientedGeometry<TSimpleGeom>, TSimpleGraph extends SimpleGraph<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleVertex extends SimpleVertex<TSimpleGeom, TSimpleVertex, TSimpleEdge>, TSimpleEdge extends SimpleEdge<TSimpleGeom, TSimpleVertex, TSimpleEdge>>
            TSimpleEdge getOrAddEdge(TSimpleVertex from, TSimpleVertex to, TSimpleGeom geometry, TSimpleGraph graph,
                    GeometryConvertable base, Map<GeometryConvertable, List<TSimpleEdge>> edgeMap, Map<TSimpleEdge, List<GeometryConvertable>> edgeBackmap) {

        TSimpleEdge result = from.getEdgeTo(to);
        if (result != null) {
            //Logger.getLogger(GraphConstruction.class.getName()).log(Level.INFO, "Duplicate edge found in input. Note that the SimpleGraph model does not allow for duplicate edges (with different geometries).");
        } else {
            result = graph.addEdge(from, to, geometry);

            if (edgeBackmap != null) {
                edgeBackmap.put(result, new ArrayList());
            }
        }

        if (edgeMap != null) {
            edgeMap.get(base).add(result);
        }

        if (edgeBackmap != null) {
            edgeBackmap.get(result).add(base);
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // GEOMETRY -> DCEL
    // -------------------------------------------------------------------------
    public static <TDCELGeom extends OrientedGeometry, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>>
            void convertGeometriesToDCEL(TDCELGraph graph, List<? extends GeometryConvertable> geometries,
                    GeometryCloner<OrientedGeometry, TDCELGeom> cloner) {
        convertGeometriesToDCEL(graph, geometries, DoubleUtil.EPS, cloner, null, null, null, null);
    }

    public static <TDCELGeom extends OrientedGeometry, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>>
            void convertGeometriesToDCEL(TDCELGraph graph, List<? extends GeometryConvertable> geometries, double precision,
                    GeometryCloner<OrientedGeometry, TDCELGeom> cloner) {
        convertGeometriesToDCEL(graph, geometries, precision, cloner, null, null, null, null);
    }

    public static <TDCELGeom extends OrientedGeometry, TDCELGraph extends DCELGraph<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELVertex extends DCELVertex<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELDart extends DCELDart<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>, TDCELFace extends DCELFace<TDCELGeom, TDCELVertex, TDCELDart, TDCELFace>>
            void convertGeometriesToDCEL(TDCELGraph graph, List<? extends GeometryConvertable> geometries, double precision,
                    GeometryCloner<OrientedGeometry, TDCELGeom> cloner,
                    Map<GeometryConvertable, List<TDCELVertex>> vertexMap, Map<TDCELVertex, List<GeometryConvertable>> vertexBackmap,
                    Map<GeometryConvertable, List<TDCELDart>> edgeMap, Map<TDCELDart, List<GeometryConvertable>> edgeBackmap) {

        SimpleGraph sg = new SimpleGraph() {

            @Override
            public SimpleVertex createVertex(double x, double y) {
                return new SimpleVertex(x, y) {
                };
            }

            @Override
            public SimpleEdge createEdge() {
                return new SimpleEdge() {
                };
            }
        };

        Map<GeometryConvertable, List<SimpleVertex>> geomgraph_vertexMap;
        Map<SimpleVertex, TDCELVertex> graphdcel_vertexMap;
        if (vertexMap != null) {
            geomgraph_vertexMap = new HashMap();
            graphdcel_vertexMap = new HashMap();
        } else {
            geomgraph_vertexMap = null;
            graphdcel_vertexMap = null;
        }

        Map<SimpleVertex, List<GeometryConvertable>> geomgraph_vertexBackmap;
        Map<TDCELVertex, SimpleVertex> graphdcel_vertexBackmap;
        if (vertexBackmap != null) {
            geomgraph_vertexBackmap = new HashMap();
            graphdcel_vertexBackmap = new HashMap();
        } else {
            geomgraph_vertexBackmap = null;
            graphdcel_vertexBackmap = null;
        }

        Map<GeometryConvertable, List<SimpleEdge>> geomgraph_edgeMap;
        Map<SimpleEdge, TDCELDart> graphdcel_edgeMap;
        if (edgeMap != null) {
            geomgraph_edgeMap = new HashMap();
            graphdcel_edgeMap = new HashMap();
        } else {
            geomgraph_edgeMap = null;
            graphdcel_edgeMap = null;
        }

        Map<SimpleEdge, List<GeometryConvertable>> geomgraph_edgeBackmap;
        Map<TDCELDart, SimpleEdge> graphdcel_edgeBackmap;
        if (edgeBackmap != null) {
            geomgraph_edgeBackmap = new HashMap();
            graphdcel_edgeBackmap = new HashMap();
        } else {
            geomgraph_edgeBackmap = null;
            graphdcel_edgeBackmap = null;
        }

        convertGeometriesToGraph(sg, geometries, precision, cloner, geomgraph_vertexMap, geomgraph_vertexBackmap, geomgraph_edgeMap, geomgraph_edgeBackmap);
        convertSimpleGraphToDCEL(graph, sg, cloner, graphdcel_vertexMap, graphdcel_vertexBackmap, graphdcel_edgeMap, graphdcel_edgeBackmap);

        if (vertexMap != null) {
            for (Entry<GeometryConvertable, List<SimpleVertex>> e : geomgraph_vertexMap.entrySet()) {
                List<TDCELVertex> dcelvtcs = new ArrayList();
                for (SimpleVertex sv : e.getValue()) {
                    dcelvtcs.add(graphdcel_vertexMap.get(sv));
                }
                vertexMap.put(e.getKey(), dcelvtcs);
            }
        }

        if (vertexBackmap != null) {
            for (Entry<TDCELVertex, SimpleVertex> e : graphdcel_vertexBackmap.entrySet()) {
                vertexBackmap.put(e.getKey(), geomgraph_vertexBackmap.get(e.getValue()));
            }
        }

        if (edgeMap != null) {
            for (Entry<GeometryConvertable, List<SimpleEdge>> e : geomgraph_edgeMap.entrySet()) {
                List<TDCELDart> dceldarts = new ArrayList();
                for (SimpleEdge se : e.getValue()) {
                    dceldarts.add(graphdcel_edgeMap.get(se));
                }
                edgeMap.put(e.getKey(), dceldarts);
            }
        }

        if (edgeBackmap != null) {
            for (Entry<TDCELDart, SimpleEdge> e : graphdcel_edgeBackmap.entrySet()) {
                edgeBackmap.put(e.getKey(), geomgraph_edgeBackmap.get(e.getValue()));
            }
        }
    }
}
