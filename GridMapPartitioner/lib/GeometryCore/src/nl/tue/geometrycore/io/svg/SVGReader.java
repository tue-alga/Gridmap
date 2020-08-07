/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.svg;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.io.BaseReader;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * This class provides a reader for the SVG format.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SVGReader extends BaseReader {

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------
    private Map<String, Color> _namedColors;
    private final BufferedReader _source;
    private List<ReadItem> _items;
    private Rectangle _viewbox = null;

    private SVGReader(BufferedReader source) {
        _source = source;
    }

    public static SVGReader fileReader(File file) throws FileNotFoundException {
        return new SVGReader(new BufferedReader(new FileReader(file)));
    }

    public static SVGReader stringReader(String string) {
        return new SVGReader(new BufferedReader(new StringReader(string)));
    }

    public static SVGReader clipboardReader() {
        return stringReader(ClipboardUtil.getClipboardContents());
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATIONS OF BASE CLASS
    // -------------------------------------------------------------------------
    public Rectangle getViewBox() {
        return _viewbox;
    }
    
    @Override
    public void close() throws IOException {
        _source.close();
    }

    @Override
    public void read(List<ReadItem> items) throws IOException {

        _items = items;

        String line = _source.readLine();

        boolean instyle = false;

        _namedColors = new HashMap();

        // fill up with some CSS/SVG defaults?
        _namedColors.put("black", Color.black);
        _namedColors.put("white", Color.white);

        while (line != null) {

            if (line.startsWith("<svg")) {
                readViewBox(line);
            } else if (line.startsWith("<defs")) {
                instyle = true;
            } else if (line.startsWith("</defs")) {
                instyle = false;
            } else if (!instyle) {

                if (line.startsWith("<path")) {
                    ReadItem item = readPath(line);
                    _items.add(item);
                } else if (line.startsWith("<polyline")) {
                    ReadItem item = readPolyline(line);
                    _items.add(item);
                } else if (line.startsWith("<rect")) {
                    ReadItem item = readRect(line);
                    _items.add(item);
                } else if (line.startsWith("<circle")) {
                    ReadItem item = readCircle(line);
                    _items.add(item);
                } else if (line.startsWith("<use")) {
                    // TODO
                } else if (line.startsWith("<g")) {
                    ReadItem item = readGroup(line);
                    _items.add(item);
                }
            } else {
                // in style
            }

            line = _source.readLine();
        }
    }

    // -------------------------------------------------------------------------
    // INTERNAL METHODS
    // -------------------------------------------------------------------------
    private void readViewBox(String line) {
        String box = readAttribute(line, "viewBox");
        String[] split = box.split(" ");
        
        double left = Double.parseDouble(split[0]);
        double top = Double.parseDouble(split[1]);
        double width = Double.parseDouble(split[2]);
        double height = Double.parseDouble(split[3]);
        
        _viewbox = Rectangle.byCornerAndSize(new Vector(left,top), width, -height);
    }
    
    /**
     * Reads the value of a given XML attribute in a given line.
     *
     * @param line Full line to search in
     * @param attributename Attribute name, include "=" sign
     * @return Attribute value (without quotes) or null if attribute is not
     * found.
     */
    private String readAttribute(String line, String attributename) {
        int index = line.indexOf(attributename);
        if (index < 0) {
            return null;
        } else {
            String value = line.substring(index);
            value = value.substring(value.indexOf("\"") + 1);
            value = value.substring(0, value.indexOf("\""));
            return value;
        }
    }

    private Map<String, String> readAuxiliary(String line, String... ignore) {
        Map<String, String> aux = new HashMap();

        // trim the opening <tag-name and the closing >
        line = line.substring(line.indexOf(" "), line.length() - 1).trim();

        while (line.length() > 0) {
            int eq = line.indexOf("=");

            if (eq < 0) {
                break;
            }

            int open = line.indexOf("\"", eq + 1);
            int close = line.indexOf("\"", open + 1);
            String key = line.substring(0, eq).trim();

            boolean skip = false;
            for (String ign : ignore) {
                skip = skip || ign.equals(key);
            }
            if (!skip) {
                String value = line.substring(open + 1, close);
                aux.put(key, value);
            }

            line = line.substring(close + 1).trim();
        }

        return aux;
    }

    /**
     * Reads the value of a given CSS attribute in a given string.
     *
     * @param style Full CSS attribute to search in
     * @param attributename Attribute name, include ":" sign
     * @return Attribute value (without colon or semi colon) or null if
     * attribute is not found.
     */
    private String readStyle(String style, String attributename) {
        if (style == null) {
            return null;
        }
        int index = style.indexOf(attributename);
        if (index < 0) {
            return null;
        } else {
            String value = style.substring(index);
            value = value.substring(value.indexOf(":") + 1);
            value = value.substring(0, value.indexOf(";"));
            return value.trim();
        }
    }

    /**
     * Read a <path> element in the XML format.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readPath(String line) throws IOException {

        String style = readAttribute(line, "style=");
        Color stroke = interpretColor(readStyle(style, "stroke:"));
        Color fill = interpretColor(readStyle(style, "fill:"));
        double strokewidth = interpretPen(readStyle(style, "stroke-width:"));
        double alpha = interpretTransparency(readStyle(style, "opacity:"));
        double[][] m = null; // interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = null; //interpretDash(readAttribute(line, "dash="));

        // start reading geometries
        List<BaseGeometry> complexgeos = new ArrayList();

        String path = readAttribute(line, "d=");

        PathReadState state = new PathReadState();

        char command = '0';
        String build = "";
        List<Double> values = new ArrayList();

        for (char ch : path.toCharArray()) {
            switch (ch) {
                case 'M':
                case 'm':
                case 'L':
                case 'l':
                case 'H':
                case 'h':
                case 'V':
                case 'v':
                case 'Z':
                case 'z':
                case 'A':
                case 'a':
                case 'C':
                case 'c':
                case 'S':
                case 's':
                    if (build.length() > 0) {
                        values.add(Double.parseDouble(build));
                    }
                    executeCommand(command, values, state, m, complexgeos);
                    command = ch;
                    values.clear();
                    build = "";
                    break;
                case ' ':
                case ',':
                    if (build.length() > 0) {
                        values.add(Double.parseDouble(build));
                        build = "";
                    }
                    break;
                case '-':
                    if (build.length() > 0 && !build.endsWith("e")) {
                        values.add(Double.parseDouble(build));
                        build = "";
                    }
                    build += ch;
                    break;
                case '.':
                    if (build.contains(".")) {
                        values.add(Double.parseDouble(build));
                        build = "";
                    }
                    build += ch;
                    break;
                default:
                    build += ch;
                    break;
            }
        }

        if (build.length() > 0) {
            values.add(Double.parseDouble(build));
        }
        executeCommand(command, values, state, m, complexgeos);

        if (state.polyline != null) {
            if (state.polyline.size() > 2) {
                complexgeos.add(new PolyLine(state.polyline));
            } else if (state.polyline.size() == 2) {
                complexgeos.add(new LineSegment(state.polyline.get(0), state.polyline.get(1)));
            }
        }

        BaseGeometry newgeom;
        if (complexgeos.size() == 1) {
            newgeom = complexgeos.get(0);
        } else {
            newgeom = new GeometryGroup(complexgeos);
        }

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(null);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);
        item.setAuxiliary(readAuxiliary(line, "d", "style"));

        return item;
    }

    /**
     * Read a <rect> element in the XML format.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readRect(String line) throws IOException {

        String style = readAttribute(line, "style=");
        Color stroke = interpretColor(readStyle(style, "stroke:"));
        Color fill = interpretColor(readStyle(style, "fill:"));
        double strokewidth = interpretPen(readStyle(style, "stroke-width:"));
        double alpha = interpretTransparency(readStyle(style, "opacity:"));
        double[][] m = null; // interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = null; //interpretDash(readAttribute(line, "dash="));

        Vector topleft = interpretPosition(
                Double.parseDouble(readAttribute(line, "x=")),
                Double.parseDouble(readAttribute(line, "y=")),
                m, null);

        Vector bottomright = interpretPosition(
                Double.parseDouble(readAttribute(line, "width=")),
                Double.parseDouble(readAttribute(line, "height=")),
                m, topleft);

        BaseGeometry newgeom = Rectangle.byCorners(topleft, bottomright);

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(null);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);
        item.setAuxiliary(readAuxiliary(line, "x", "y", "width", "height", "style"));

        return item;
    }

    /**
     * Read a <polyline> element in the XML format.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readPolyline(String line) throws IOException {

        String style = readAttribute(line, "style=");
        Color stroke = interpretColor(readStyle(style, "stroke:"));
        Color fill = interpretColor(readStyle(style, "fill:"));
        double strokewidth = interpretPen(readStyle(style, "stroke-width:"));
        double alpha = interpretTransparency(readStyle(style, "opacity:"));
        double[][] m = null; // interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = null; //interpretDash(readAttribute(line, "dash="));

        List<Vector> vs = new ArrayList();

        String pts = readAttribute(line, "points=").trim().replaceAll(" [ ]+", " ");
        String[] vals = pts.split("[ ,]");
        int i = 0;
        while (i < vals.length) {
            vs.add(interpretPosition(
                    Double.parseDouble(vals[i]),
                    Double.parseDouble(vals[i + 1]),
                    m, null));
            i += 2;
        }

        BaseGeometry newgeom = new PolyLine(vs);

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(null);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);
        item.setAuxiliary(readAuxiliary(line, "points", "style"));

        return item;
    }

    /**
     * Read a <circle> element in the XML format.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readCircle(String line) throws IOException {

        String style = readAttribute(line, "style=");
        Color stroke = interpretColor(readStyle(style, "stroke:"));
        Color fill = interpretColor(readStyle(style, "fill:"));
        double strokewidth = interpretPen(readStyle(style, "stroke-width:"));
        double alpha = interpretTransparency(readStyle(style, "opacity:"));
        double[][] m = null; // interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = null; //interpretDash(readAttribute(line, "dash="));

        Vector center = interpretPosition(
                Double.parseDouble(readAttribute(line, "x=")),
                Double.parseDouble(readAttribute(line, "y=")),
                m, null);

        Vector radiusPoint = interpretPosition(
                Double.parseDouble(readAttribute(line, "r=")),
                0,
                m, center);

        BaseGeometry newgeom = Circle.byThroughPoint(center, radiusPoint);

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(null);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);
        item.setAuxiliary(readAuxiliary(line, "x", "y", "r", "style"));

        return item;
    }

    /**
     * Read a <g> elements in the XML format.
     *
     * NB: will read up to and including the closing <g> tag.
     *
     * @param line first line containing the group-tag
     * @return ReadItem containing the geometry and last encountered style of
     * the group
     * @throws IOException
     */
    private ReadItem readGroup(String line) throws IOException {

        double[][] m = interpretMatrix(readAttribute(line, "transform"));

        List<BaseGeometry> parts = new ArrayList();

        Color stroke = null;
        Color fill = null;
        double strokewidth = -1;
        double size = -1;
        Dashing dash = null;
        double alpha = -1;

        Map<String, String> aux = readAuxiliary(line, "transform");

        line = _source.readLine();

        while (!line.startsWith("</g")) {
            ReadItem item = null;
            if (line.startsWith("<path")) {
                item = readPath(line);
            } else if (line.startsWith("<polyline")) {
                item = readPolyline(line);
            } else if (line.startsWith("<rect")) {
                item = readRect(line);
            } else if (line.startsWith("<circle")) {
                item = readCircle(line);
            } else if (line.startsWith("<use")) {
                // TODO
            } else if (line.startsWith("<g")) {
                item = readGroup(line);
            }

            if (item != null) {
                BaseGeometry geo = item.getGeometry();
                applyMatrixToGeometry(geo, m);
                parts.add(geo);

                stroke = item.getStroke();
                fill = item.getFill();
                strokewidth = item.getStrokewidth();
                size = item.getSymbolsize();
                dash = item.getDash();
                alpha = item.getAlpha();
            }

            line = _source.readLine();
        }

        ReadItem item = new ReadItem();

        item.setLayer(null);
        item.setGeometry(new GeometryGroup(parts));

        if (alpha >= 0) {
            item.setAlpha(alpha);
        }
        if (dash != null) {
            item.setDash(dash);
        }
        if (fill != null) {
            item.setFill(fill);
        }
        if (stroke != null) {
            item.setStroke(stroke);
        }
        if (strokewidth >= 0) {
            item.setStrokewidth(strokewidth);
        }
        if (size >= 0) {
            item.setSymbolsize(size);
        }
        item.setAuxiliary(aux);

        return item;
    }

    private void applyMatrixToGeometry(BaseGeometry geometry, double[][] matrix) {
        if (matrix == null) {
            return;
        }

        if (geometry instanceof Vector) {
            Vector v = (Vector) geometry;
            applyMatrixToPosition(v, matrix);
        } else if (geometry instanceof LineSegment) {
            LineSegment ls = (LineSegment) geometry;
            applyMatrixToPosition(ls.getStart(), matrix);
            applyMatrixToPosition(ls.getEnd(), matrix);
        } else if (geometry instanceof PolyLine) {
            PolyLine pl = (PolyLine) geometry;
            for (Vector v : pl.vertices()) {
                applyMatrixToPosition(v, matrix);
            }
        } else if (geometry instanceof Polygon) {
            Polygon p = (Polygon) geometry;
            for (Vector v : p.vertices()) {
                applyMatrixToPosition(v, matrix);
            }
        } else if (geometry instanceof GeometryGroup) {
            GeometryGroup<? extends BaseGeometry> g = (GeometryGroup) geometry;
            for (BaseGeometry part : g.getParts()) {
                applyMatrixToGeometry(part, matrix);
            }
        } else {
            Logger.getLogger(SVGReader.class.getName()).log(Level.WARNING, "Unexpected type to apply matrix: {0}", geometry.getClass().getName());
        }
    }

    /**
     * Interpret a color value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private Color interpretColor(String attr) {
        if (attr == null || attr.toLowerCase().equals("none")) {
            return null;
        }

        if (_namedColors.containsKey(attr)) {
            return _namedColors.get(attr);
        }

        try {
            int r, g, b;
            if (attr.startsWith("#")) {

                if (attr.length() == 4) {
                    r = Integer.parseInt(attr.substring(1, 2), 16);
                    g = Integer.parseInt(attr.substring(2, 3), 16);
                    b = Integer.parseInt(attr.substring(3, 4), 16);
                } else {
                    r = Integer.parseInt(attr.substring(1, 3), 16);
                    g = Integer.parseInt(attr.substring(3, 5), 16);
                    b = Integer.parseInt(attr.substring(5, 7), 16);
                }

            } else if (attr.startsWith("rgb")) {
                String[] split = attr.substring(4, attr.length() - 1).split(",");

                split[0] = split[0].trim();
                if (split[0].endsWith("%")) {
                    r = (int) Math.round(255 * Integer.parseInt(split[0].substring(0, split[0].length() - 1)) / 100.0);
                } else {
                    r = Integer.parseInt(split[0]);
                }

                if (split.length == 1) {
                    g = r;
                    b = r;
                } else {
                    split[1] = split[1].trim();
                    if (split[1].endsWith("%")) {
                        g = (int) Math.round(255 * Integer.parseInt(split[1].substring(0, split[1].length() - 1)) / 100.0);
                    } else {
                        g = Integer.parseInt(split[1]);
                    }

                    split[2] = split[2].trim();
                    if (split[2].endsWith("%")) {
                        b = (int) Math.round(255 * Integer.parseInt(split[2].substring(0, split[2].length() - 1)) / 100.0);
                    } else {
                        b = Integer.parseInt(split[2]);
                    }
                }
            } else {
                r = 255;
                g = 0;
                b = 0;
                Logger.getLogger(SVGReader.class.getName()).log(Level.WARNING, "Unexpected color: {0}", attr);
            }

            return new Color(r, g, b);
        } catch (NumberFormatException ex) {
            Logger.getLogger(SVGReader.class.getName()).log(Level.WARNING, "Unexpected color: {0}", attr);
            return Color.red;
        }
    }

    /**
     * Interpret a transparency value, possibly looking it up in the named
     * values.
     *
     * @param attr Attribute value
     */
    private double interpretTransparency(String attr) {
        if (attr == null) {
            return 1;
        }
        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            Logger.getLogger(SVGReader.class.getName()).log(Level.WARNING, "Unexpected transparency: {0}", attr);
            return 1;
        }
    }

    /**
     * Interpret a position (x,y) value, applying the transformation matrix, if
     * any, and relative to the given position (if not null).
     *
     * @param attr Attribute value
     * @param matrix Matrix for transforming the (x,y) value
     */
    private Vector interpretPosition(double x, double y, double[][] matrix, Vector relative) {

        Vector v = new Vector(x, -y);
        if (relative != null) {
            v.translate(relative);
        }
        applyMatrixToPosition(v, matrix);
        return v;
    }

    private double interpretLength(double l, double[][] matrix) {

        Vector v = new Vector(l, 0);
        applyMatrixToPosition(v, matrix);
        return v.length();
    }

    /**
     * Applies a matrix transformation to the given position. If null is
     * supplied as matrix, nothing happens.
     *
     * @param position Position to which the matrix must be applied
     * @param matrix 2x3 matrix describing the transformation or null
     */
    private void applyMatrixToPosition(Vector position, double[][] matrix) {
        if (matrix != null) {
            position.set(
                    matrix[0][0] * position.getX() + matrix[0][1] * position.getY() + matrix[0][2],
                    matrix[1][0] * position.getX() + matrix[1][1] * position.getY() + matrix[1][2]
            );
        }
    }

    /**
     * Interpret a matrix value.
     *
     * @param attr Attribute value
     */
    private double[][] interpretMatrix(String attr) {
        if (attr == null) {
            return null;
        }

        attr = attr.replace(" ", "");

        if (attr.startsWith("matrix")) {
            attr = attr.substring(attr.indexOf("(") + 1);
            attr = attr.substring(0, attr.indexOf(")"));

            String[] split = attr.split(",");

            return new double[][]{
                {Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[4])},
                {Double.parseDouble(split[1]), Double.parseDouble(split[3]), Double.parseDouble(split[5])}
            };
        } else {
            Logger.getLogger(SVGReader.class.getName()).log(Level.SEVERE, "Unexpected transformation: '{0}'", attr);
            return null;
        }
    }

    /**
     * Interpret a stroke-width value, possibly looking it up in the named
     * values.
     *
     * @param attr Attribute value
     */
    private double interpretPen(String attr) {
        if (attr == null || attr.toLowerCase().equals("none")) {
            return 0;
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException e) {
            Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected pen name: {0}", attr);
            return 0;
        }
    }

    private void executeCommand(char command, List<Double> vals, PathReadState state, double[][] matrix, List<BaseGeometry> complexgeos) {

        switch (command) {
            case 'Z':
            case 'z':
                // return to first
                if (state.polyline != null) {
                    complexgeos.add(new Polygon(state.polyline));
                } else if (state.prev != null) {
                    complexgeos.add(new LineSegment(state.prev, state.first));
                }
                state.polyline = null;
                state.first = null;
                state.lastcurvecontrol = null;
                break;
            case 'M':
            case 'm':
                if (state.polyline != null) {
                    if (state.polyline.size() > 2) {
                        complexgeos.add(new PolyLine(state.polyline));
                    } else if (state.polyline.size() == 2) {
                        complexgeos.add(new LineSegment(state.polyline.get(0), state.polyline.get(1)));
                    }
                }
                // move
                assert vals.size() >= 2 && vals.size() % 2 == 0;
                state.first = interpretPosition(vals.get(0), vals.get(1), matrix, command == 'm' ? state.prev : null);
                state.prev = state.first;

                state.polyline = new ArrayList();
                state.polyline.add(state.first);

                // implicit line-to's
                for (int i = 2; i < vals.size(); i += 2) {
                    state.prev = interpretPosition(vals.get(i), vals.get(i + 1), matrix, command == 'm' ? state.prev : null);

                    state.polyline.add(state.prev);
                }

                state.lastcurvecontrol = null;
                break;
            case 'L':
            case 'l': {
                // line to
                assert vals.size() > 0 && vals.size() % 2 == 0;

                if (state.polyline == null) {
                    state.polyline = new ArrayList();
                    state.polyline.add(state.prev);
                }

                for (int i = 0; i < vals.size(); i += 2) {
                    state.prev = interpretPosition(vals.get(i), vals.get(i + 1), matrix, command == 'l' ? state.prev : null);

                    state.polyline.add(state.prev);
                }
                state.lastcurvecontrol = null;
                break;
            }
            case 'H':
            case 'h': {
                // horizontal line to
                assert vals.size() > 0;

                if (state.polyline == null) {
                    state.polyline = new ArrayList();
                    state.polyline.add(state.prev);
                }

                double y = state.prev.getY();
                for (int i = 0; i < vals.size(); i++) {
                    if (command == 'h') {
                        state.prev = interpretPosition(vals.get(i), 0, matrix, state.prev);
                    } else {
                        state.prev = interpretPosition(vals.get(i), y, matrix, null);
                    }

                    state.polyline.add(state.prev);
                }
                state.lastcurvecontrol = null;
                break;
            }
            case 'V':
            case 'v': {
                // horizontal line to
                assert vals.size() > 0;

                if (state.polyline == null) {
                    state.polyline = new ArrayList();
                    state.polyline.add(state.prev);
                }

                double x = state.prev.getX();
                for (int i = 0; i < vals.size(); i++) {
                    if (command == 'v') {
                        state.prev = interpretPosition(0, vals.get(i), matrix, state.prev);
                    } else {
                        state.prev = interpretPosition(x, vals.get(i), matrix, null);
                    }

                    state.polyline.add(state.prev);
                }
                state.lastcurvecontrol = null;
                break;
            }
            case 'A':
            case 'a':
                if (state.polyline != null) {
                    if (state.polyline.size() > 2) {
                        complexgeos.add(new PolyLine(state.polyline));
                    } else if (state.polyline.size() == 2) {
                        complexgeos.add(new LineSegment(state.polyline.get(0), state.polyline.get(1)));
                    }
                }

                // circular arc
                CircularArc arc = interpretCircularArc(vals, state.prev, matrix, command == 'a');
                complexgeos.add(arc);
                state.prev = arc.getEnd();

                //first = null;
                state.polyline = null;
                state.lastcurvecontrol = null;
                break;
            case '0':
                // nothing
                break;
            case 'C':
            case 'c':
            case 'S':
            case 's': {
                // curve to
                assert command == 'C' || command == 'c' || (vals.size() > 3 && vals.size() % 4 == 0);
                assert command == 'S' || command == 's' || (vals.size() > 5 && vals.size() % 6 == 0);

                if (state.polyline == null) {
                    state.polyline = new ArrayList();
                    state.polyline.add(state.prev);
                }

                int i = 0;
                while (i < vals.size()) {
                    Vector c0 = state.prev;
                    Vector c1;
                    if (command == 'c' || command == 'C') {
                        c1 = interpretPosition(vals.get(i), vals.get(i + 1), matrix, command == 'c' ? c0 : null);
                        i += 2;
                    } else {
                        // s/S
                        if (state.lastcurvecontrol == null) {
                            c1 = c0;
                        } else {
                            c1 = Vector.add(c0, Vector.subtract(c0, state.lastcurvecontrol));
                        }
                    }
                    Vector c2 = interpretPosition(vals.get(i), vals.get(i + 1), matrix, command == 'c' || command == 's' ? c0 : null);
                    Vector c3 = interpretPosition(vals.get(i + 2), vals.get(i + 3), matrix, command == 'c' || command == 's' ? c0 : null);

                    // sample with 10 segments, for now...
                    for (double t = 0.1; t < 0.999; t += 0.1) {
                        double x = (1 - t) * (1 - t) * (1 - t) * c0.getX() + 3 * (1 - t) * (1 - t) * t * c1.getX() + 3 * (1 - t) * t * t * c2.getX() + t * t * t * c3.getX();
                        double y = (1 - t) * (1 - t) * (1 - t) * c0.getY() + 3 * (1 - t) * (1 - t) * t * c1.getY() + 3 * (1 - t) * t * t * c2.getY() + t * t * t * c3.getY();
                        state.polyline.add(new Vector(x, y));
                    }

                    state.polyline.add(c3);
                    state.prev = c3;
                    state.lastcurvecontrol = c2;

                    i += 4;
                }
                break;
            }
            default:
                Logger.getLogger(SVGReader.class.getName()).log(Level.WARNING, "Encountered unsupported command ''{0}''. Ignoring...", command);
                break;
        }

        vals.clear();
    }

    /**
     * Interpret a specified CircularArc from the command values, applying the
     * given matrix, if any, possibly relative to the previous position.
     *
     * @param matrix Matrix to be applied (if any)
     */
    private CircularArc interpretCircularArc(List<Double> vals, Vector prev, double[][] matrix, boolean relative) {

        double rx = vals.get(0);
        //double ry = vals.get(1);
        //double xaxis = vals.get(2);
        double largeCA = vals.get(3);
        double sweepCW = vals.get(4);
        double endx = vals.get(5);
        double endy = vals.get(6);

        Vector end = interpretPosition(endx, endy, matrix, relative ? prev : null);

        double r = interpretLength(rx, matrix);

        Circle circ1 = new Circle(prev, r);
        Circle circ2 = new Circle(end, r);

        List<BaseGeometry> ints = circ1.intersect(circ2);

        Vector c1 = (Vector) ints.get(0);
        CircularArc arc1 = new CircularArc(c1, prev, end, sweepCW < 0.5);

        if (ints.size() == 1) {
            return arc1;
        }

        Vector c2 = (Vector) ints.get(1);
        CircularArc arc2 = new CircularArc(c2, prev, end, sweepCW < 0.5);

        double ca1 = arc1.centralAngle();
        if (ca1 < Math.PI) {
            assert arc2.centralAngle() >= Math.PI;
            if (largeCA < 0.5) {
                return arc1;
            } else {
                return arc2;
            }
        } else {
            // ca1 >= Math.PI
            assert arc2.centralAngle() < Math.PI;
            if (largeCA < 0.5) {
                return arc2;
            } else {
                return arc1;
            }
        }
    }

    private class PathReadState {

        List<Vector> polyline;
        Vector first;
        Vector prev;
        Vector lastcurvecontrol;
    }
}
