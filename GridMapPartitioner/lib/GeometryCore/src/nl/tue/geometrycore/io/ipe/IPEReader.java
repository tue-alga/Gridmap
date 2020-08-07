/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.ipe;

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
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.BaseReader;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * This class provides a reader for the IPE XML format. It can handle both files
 * as well as selection code, allowing easy copy-pasting from IPE into Java. The
 * applicable type is detected automatically.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IPEReader extends BaseReader {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Map<String, Color> _namedColors;
    private Map<String, Double> _namedStrokewidths;
    private Map<String, Double> _namedSymbolsizes;
    private Map<String, Double> _namedTransparencies;
    private Map<String, Dashing> _namedDashing;
    private Rectangle _pagebounds = IPEWriter.getA4Size();
    private final BufferedReader _source;
    private List<ReadItem> _items;
    private String _currentLayer;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public IPEReader(BufferedReader source) {
        _source = source;
    }

    /**
     * Constructs the reader of a file containing the IPE XML code.
     *
     * @param file the ipe file to be read
     * @return new reader for the provided file
     * @throws FileNotFoundException
     */
    public static IPEReader fileReader(File file) throws FileNotFoundException {
        return new IPEReader(new BufferedReader(new FileReader(file)));
    }

    /**
     * Constructs a reader for the provided string containing IPE XML code.
     *
     * @param string IPE XML code
     * @return new reader for the code
     */
    public static IPEReader stringReader(String string) {
        return new IPEReader(new BufferedReader(new StringReader(string)));
    }

    /**
     * Constructs a custom reader using some buffered reader that provides the
     * IPE XML code line by line.
     *
     * @param reader buffered reader providing the IPE XML code
     * @return new reader for the code
     */
    public static IPEReader customReader(BufferedReader reader) {
        return new IPEReader(reader);
    }

    /**
     * Constructs a reader based on the contents of the clipboard.
     *
     * @return new reader for the clipboard string
     */
    public static IPEReader clipboardReader() {
        return stringReader(ClipboardUtil.getClipboardContents());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void close() throws IOException {
        _source.close();
    }

    /**
     * Reads only the items from a specific page. First page is numbered 1. Any
     * value below 1 will result in all pages being read.
     *
     * @param items
     * @param page
     * @throws IOException
     */
    public List<ReadItem> read(int page) throws IOException {
        List<ReadItem> items = new ArrayList();
        read(items, page);
        return items;
    }

    @Override
    public void read(List<ReadItem> items) throws IOException {
        read(items, -1);
    }

    /**
     * Reads only the items from a specific page. First page is numbered 1. Any
     * value below 1 will result in all pages being read.
     *
     * @param items
     * @param page
     * @throws IOException
     */
    public void read(List<ReadItem> items, int page) throws IOException {

        _currentLayer = "default";
        _items = items;

        String line = _source.readLine();

        boolean onpage = line.startsWith("<ipeselection");
        boolean instyle = false;

        int pageNumber = 0;

        if (onpage) {
            // selection
            _namedStrokewidths = null;
            _namedSymbolsizes = null;
            _namedTransparencies = null;
            _namedColors = null;
            _namedDashing = null;
            pageNumber++;
        } else {
            _namedStrokewidths = new HashMap();
            _namedSymbolsizes = new HashMap();
            _namedTransparencies = new HashMap();
            _namedColors = new HashMap();
            _namedDashing = new HashMap();

            // IPE doesnt store some default values...
            _namedStrokewidths.put("normal", 0.4);
            _namedSymbolsizes.put("normal", 3.0);
            _namedColors.put("black", Color.black);
            _namedColors.put("white", Color.white);
        }

        while (line != null) {

            if (line.startsWith("<page")) {
                pageNumber++;
                onpage = true;
            } else if (line.startsWith("</page")) {
                onpage = false;
            } else if (line.startsWith("<ipestyle")) {
                instyle = true;
            } else if (line.startsWith("</ipestyle")) {
                instyle = false;
            } else if (onpage && (page < 1 || pageNumber == page)) {

                if (line.startsWith("<path")) {
                    ReadItem item = readPath(line);
                    item.setPageNumber(pageNumber);
                    _items.add(item);
                } else if (line.startsWith("<use") && line.contains("name=\"mark")) {
                    ReadItem item = readMark(line);
                    item.setPageNumber(pageNumber);
                    _items.add(item);
                } else if (line.startsWith("<group")) {
                    ReadItem item = readGroup(line);
                    item.setPageNumber(pageNumber);
                    _items.add(item);
                } else if (line.startsWith("<text")) {
                    ReadItem item = readText(line);
                    item.setPageNumber(pageNumber);
                    _items.add(item);
                }
            } else if (instyle) {

                if (line.startsWith("<dashstyle")) {
                    _namedDashing.put(readAttribute(line, "name="), interpretDash(readAttribute(line, "value=")));
                } else if (line.startsWith("<pen")) {
                    _namedStrokewidths.put(readAttribute(line, "name="), interpretPen(readAttribute(line, "value=")));
                } else if (line.startsWith("<color")) {
                    _namedColors.put(readAttribute(line, "name="), interpretColor(readAttribute(line, "value=")));
                } else if (line.startsWith("<symbolsize")) {
                    _namedSymbolsizes.put(readAttribute(line, "name="), interpretSymbolSize(readAttribute(line, "value=")));
                } else if (line.startsWith("<opacity")) {
                    _namedTransparencies.put(readAttribute(line, "name="), interpretTransparency(readAttribute(line, "value=")));
                } else if (line.startsWith("<layout ")) {
                    _pagebounds = interpretPageBounds(readAttribute(line, "paper="));
                }
            }

            line = _source.readLine();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    public Map<String, Color> getNamedColors() {
        return _namedColors;
    }

    public Map<String, Double> getNamedStrokeWidths() {
        return _namedStrokewidths;
    }

    public Map<String, Double> getNamedSymbolSizes() {
        return _namedSymbolsizes;
    }

    public Map<String, Double> getNamedTransparencies() {
        return _namedTransparencies;
    }

    public Map<String, Dashing> getNamedDashing() {
        return _namedDashing;
    }

    public Rectangle getPageBounds() {
        return _pagebounds;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PRIVATE">    
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

    /**
     * Read a <path> element in the XML format.
     *
     * NB: will read up to and including the closing <path> tag.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readPath(String line) throws IOException {
        if (line.contains("layer=")) {
            _currentLayer = readAttribute(line, "layer=");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        Color fill = interpretColor(readAttribute(line, "fill="));
        double strokewidth = interpretPen(readAttribute(line, "pen="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = interpretDash(readAttribute(line, "dash="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));

        // start reading geometries
        List<BaseGeometry> complexgeos = new ArrayList();
        List<Vector> polyline = null;

        line = _source.readLine();

        Vector first = null;
        Vector prev = null;
        int nummoves = 0;
        boolean closed = false;
        while (!line.startsWith("</path>")) {
            if (line.endsWith("h")) {
                // return to first
                if (polyline != null) {
                    complexgeos.add(new Polygon(polyline));
                } else if (prev != null) {
                    complexgeos.add(new LineSegment(prev, first));
                }
                polyline = null;
                prev = null;
                first = null;
                closed = true;
            } else if (line.endsWith(" m")) {
                nummoves++;
                if (polyline != null) {
                    if (polyline.size() > 2) {
                        complexgeos.add(new PolyLine(polyline));
                    } else if (polyline.size() == 2) {
                        complexgeos.add(new LineSegment(polyline.get(0), polyline.get(1)));
                    }
                }
                // move
                first = interpretPosition(line, m);
                prev = first;

                polyline = new ArrayList();
                polyline.add(first);
            } else if (line.endsWith(" l")) {
                // line to
                Vector loc = interpretPosition(line, m);

                if (polyline == null) {
                    polyline = new ArrayList();
                    polyline.add(prev);
                }
                polyline.add(loc);

//                if (polyline != null) {
//                    //if (!polyline.get(polyline.size() - 1).close(loc)) {
//                    polyline.add(loc);
//                    //}
//                } else {
//                    complexgeos.add(new LineSegment(prev, loc));
//                }
                prev = loc;
            } else if (line.endsWith(" e")) {
                if (polyline != null) {
                    if (polyline.size() > 2) {
                        complexgeos.add(new PolyLine(polyline));
                    } else if (polyline.size() == 2) {
                        complexgeos.add(new LineSegment(polyline.get(0), polyline.get(1)));
                    }
                }

                // circle (NB: closed)
                complexgeos.add(interpretCircle(line, m));
                prev = null;
                first = null;
                polyline = null;
                nummoves++;
            } else if (line.endsWith(" a")) {
                if (polyline != null) {
                    if (polyline.size() > 2) {
                        complexgeos.add(new PolyLine(polyline));
                    } else if (polyline.size() == 2) {
                        complexgeos.add(new LineSegment(polyline.get(0), polyline.get(1)));
                    }
                }

                // circular arc
                CircularArc arc = interpretCircularArc(line, prev, m);
                complexgeos.add(arc);
                prev = arc.getEnd();

                //first = null;
                polyline = null;

            } else if (line.endsWith(" c")) {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Encountered Bézier curve, sampling for now...");

                int samples = 20;
                if (polyline == null) {
                    polyline = new ArrayList();
                    polyline.add(first);
                }

                assert prev != null;

                String[] coords = (line).split(" ");
                Vector p0 = prev;
                Vector p1 = interpretPosition(coords[0], coords[1], m);
                Vector p2 = interpretPosition(coords[2], coords[3], m);
                Vector p3 = interpretPosition(coords[4], coords[5], m);

                for (int i = 1; i < samples - 1; i++) {
                    double t = i / (double) (samples - 1);

                    double x = (1 - t) * (1 - t) * (1 - t) * p0.getX() + 3 * (1 - t) * (1 - t) * t * p1.getX() + 3 * (1 - t) * t * t * p2.getX() + t * t * t * p3.getX();
                    double y = (1 - t) * (1 - t) * (1 - t) * p0.getY() + 3 * (1 - t) * (1 - t) * t * p1.getY() + 3 * (1 - t) * t * t * p2.getY() + t * t * t * p3.getY();

                    Vector v = new Vector(x, y);
                    polyline.add(v);
                }

                polyline.add(p3);

                prev = p3;
            } else if (line.indexOf(' ', line.indexOf(' ') + 1) < 0) {
                // point for curve
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected command: \"{0}\"", line);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected command: \"{0}\"", line);
            }

            line = _source.readLine();
        }

        if (polyline != null) {
            if (polyline.size() > 2) {
                complexgeos.add(new PolyLine(polyline));
            } else if (polyline.size() == 2) {
                complexgeos.add(new LineSegment(polyline.get(0), polyline.get(1)));
            }
        }

        BaseGeometry newgeom;
        if (complexgeos.size() == 1) {
            newgeom = complexgeos.get(0);
        } else if (nummoves == 1) {
            if (closed) {
                newgeom = new GeometryCycle(complexgeos);
            } else {
                newgeom = new GeometryString(complexgeos);
            }
        } else {
            newgeom = new GeometryGroup(complexgeos);
        }

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);

        return item;
    }

    /**
     * Read a <mark> elements in the XML format.
     *
     * @param line first line containing the mark-tag
     * @return ReadItem containing the geometry and style of the mark
     */
    private ReadItem readMark(String line) {

        if (line.contains("layer")) {
            _currentLayer = readAttribute(line, "layer");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        Color fill = interpretColor(readAttribute(line, "fill="));

        double size = interpretSymbolSize(readAttribute(line, "size="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));

        Dashing dash = interpretDash(readAttribute(line, "dash="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));

        Vector v = interpretPosition(readAttribute(line, "pos="), m);

        ReadItem item = new ReadItem();

        item.setGeometry(v);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setStrokewidth(0.4 * size);
        item.setSymbolsize(size);

        return item;
    }

    /**
     * Read a <group> elements in the XML format.
     *
     * NB: will read up to and including the closing <group> tag.
     *
     * @param line first line containing the group-tag
     * @return ReadItem containing the geometry and last encountered style of
     * the group
     * @throws IOException
     */
    private ReadItem readGroup(String line) throws IOException {
        if (line.contains("layer")) {
            _currentLayer = readAttribute(line, "layer");
        }

        double[][] m = interpretMatrix(readAttribute(line, "matrix"));

        List<BaseGeometry> parts = new ArrayList();
        String string = null;

        Color stroke = null;
        Color fill = null;
        double strokewidth = -1;
        double size = -1;
        Dashing dash = null;
        double alpha = -1;

        line = _source.readLine();

        while (!line.startsWith("</group")) {
            ReadItem item = null;
            if (line.startsWith("<path")) {
                item = readPath(line);
            } else if (line.startsWith("<use") && line.contains("name=\"mark")) {
                item = readMark(line);
            } else if (line.startsWith("<group")) {
                item = readGroup(line);
            } else if (line.startsWith("<text")) {
                item = readText(line);
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

                if (item.getString() != null) {
                    if (string == null) {
                        string = item.getString();
                    } else {
                        string += "\n" + item.getString();
                    }
                }
            }

            line = _source.readLine();
        }

        ReadItem item = new ReadItem();

        item.setString(string);
        item.setLayer(_currentLayer);
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

        return item;
    }

    private ReadItem readText(String line) throws IOException {
        if (line.contains("layer=")) {
            _currentLayer = readAttribute(line, "layer=");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));
        Vector pos = interpretPosition(readAttribute(line, "pos="), m);

        double size = interpretTextSize(readAttribute(line, "size="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));
        TextAnchor anchor = interpretTextAnchor(readAttribute(line, "halign="), readAttribute(line, "valign="));

        // start reading geometries
        String string = line.substring(line.indexOf(">") + 1);

        while (!string.endsWith("</text>")) {
            string += "\n" + _source.readLine();
        }

        string = string.substring(0, string.length() - 7);

        ReadItem item = new ReadItem();

        item.setString(string);
        item.setGeometry(pos);
        item.setAlpha(alpha);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setSymbolsize(size * m[0][0]);
        item.setAnchor(anchor);

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
        } else if (geometry instanceof GeometryCycle) {
            GeometryCycle<? extends BaseGeometry> g = (GeometryCycle) geometry;
            for (BaseGeometry part : g.edges()) {
                applyMatrixToGeometry(part, matrix);
            }
        } else if (geometry instanceof GeometryString) {
            GeometryString<? extends BaseGeometry> g = (GeometryString) geometry;
            for (BaseGeometry part : g.edges()) {
                applyMatrixToGeometry(part, matrix);
            }
        } else {
            Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected type in IPERead: {0}", geometry.getClass().getName());
        }
    }

    /**
     * Interpret a color value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private Color interpretColor(String attr) {
        if (attr == null) {
            return null;
        }

        try {
            String[] split = attr.split(" ");
            if (split.length == 1) {

                double v = Double.parseDouble(attr);
                return new Color((int) (255 * v),
                        (int) (255 * v),
                        (int) (255 * v));

            } else {
                double r, g, b;
                r = Double.parseDouble(split[0]);
                g = Double.parseDouble(split[1]);
                b = Double.parseDouble(split[2]);

                return new Color((int) (255 * r),
                        (int) (255 * g),
                        (int) (255 * b));
            }
        } catch (NumberFormatException ex) {
            if (_namedColors == null) {
                _namedColors = IPEDefaults.getColors();
            }

            if (_namedColors.containsKey(attr)) {
                return _namedColors.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected color name: {0}", attr);
                return Color.red;
            }
        }
    }

    /**
     * Interpret a text-size value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private double interpretTextSize(String attr) {
        if (attr == null) {
            return 1;
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Not (yet) supporting named text-sizes", attr);
            return 1;
        }
    }

    /**
     * Interpret a point-size value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private double interpretSymbolSize(String attr) {
        if (attr == null) {
            return 1;
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            if (_namedSymbolsizes == null) {
                _namedSymbolsizes = IPEDefaults.getSymbolSizes();
            }

            if (_namedSymbolsizes.containsKey(attr)) {
                return _namedSymbolsizes.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected symbol-size name: {0}", attr);
                return 1;
            }
        }
    }

    private TextAnchor interpretTextAnchor(String hattr, String vattr) {
        TextAnchor anchor = TextAnchor.BASELINE;
        if (hattr != null) {
            if (hattr.equals("center")) {
                anchor = TextAnchor.BASELINE_CENTER;
            } else if (hattr.equals("right")) {
                anchor = TextAnchor.BASELINE_RIGHT;
            }
        }

        if (vattr != null) {
            if (vattr.equals("center")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.CENTER;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.RIGHT;
                        break;
                }
            } else if (vattr.equals("top")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.TOP_LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.TOP;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.TOP_RIGHT;
                        break;
                }
            } else if (vattr.equals("bottom")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.BOTTOM_LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.BOTTOM;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.BOTTOM_RIGHT;
                        break;
                }
            }
        }

        return anchor;
    }

    /**
     * Interpret a page size from the IPE style specification.
     *
     * @param attr two space-separated dimensions (width followed by height)
     * @return a rectangle matching the page boundaries
     */
    private Rectangle interpretPageBounds(String attr) {
        if (attr == null) {
            return null;
        }
        String[] split = attr.split(" ");
        double width = Double.parseDouble(split[0]);
        double height = Double.parseDouble(split[1]);
        return new Rectangle(0, width, 0, height);
    }

    /**
     * Interpret a transparency value, possibly looking it up in the named
     * values. Although IPE allows only named values, we'll also allow any
     * numeric value in between 0 and 1.
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
            if (_namedTransparencies == null) {
                _namedTransparencies = IPEDefaults.getTransparencies();
            }

            if (_namedTransparencies.containsKey(attr)) {
                return _namedTransparencies.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected transparency name: {0}", attr);
                return 1;
            }
        }
    }

    /**
     * Interpret a dash-style value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private Dashing interpretDash(String attr) {
        if (attr == null) {
            return Dashing.SOLID;
        }

        int index = attr.indexOf("[");
        if (index >= 0) {
            String[] split = attr.substring(index + 1, attr.indexOf("]")).split(" ");
            double[] dash = new double[split.length];
            for (int i = 0; i < dash.length; i++) {
                dash[i] = Double.parseDouble(split[i]);
            }
            return new Dashing(dash);
        } else {
            if (_namedDashing == null) {
                _namedDashing = IPEDefaults.getDashStyles();
            }

            if (_namedDashing.containsKey(attr)) {
                return _namedDashing.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected dash name: {0}", attr);
                return Dashing.SOLID;
            }
        }
    }

    /**
     * Interpret a position (x,y) value, applying the transformation matrix, if
     * any.
     *
     * @param attr Attribute value
     * @param matrix Matrix for transforming the (x,y) value
     */
    private Vector interpretPosition(String attr, double[][] matrix) {
        double x, y;
        if (attr == null) {
            x = 0;
            y = 0;
        } else {
            String[] split = attr.split(" ");
            x = Double.parseDouble(split[0]);
            y = Double.parseDouble(split[1]);
        }

        Vector v = new Vector(x, y);
        applyMatrixToPosition(v, matrix);
        return v;
    }

    /**
     * Interpret a position (x,y) value, applying the transformation matrix, if
     * any.
     *
     * @param strX X-coordinate as string
     * @param strY Y-coordinate as string
     * @param matrix Matrix for transforming the (x,y) value
     */
    private Vector interpretPosition(String strX, String strY, double[][] matrix) {
        double x, y;

        x = Double.parseDouble(strX);
        y = Double.parseDouble(strY);

        Vector v = new Vector(x, y);
        applyMatrixToPosition(v, matrix);
        return v;
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

        String[] split = attr.split(" ");

        return new double[][]{
            {Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[4])},
            {Double.parseDouble(split[1]), Double.parseDouble(split[3]), Double.parseDouble(split[5])}
        };
    }

    /**
     * Interpret a stroke-width value, possibly looking it up in the named
     * values.
     *
     * @param attr Attribute value
     */
    private double interpretPen(String attr) {
        if (attr == null) {
            attr = "normal"; // standard normal...
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException e) {
            if (_namedStrokewidths == null) {
                _namedStrokewidths = IPEDefaults.getStrokeWidths();
            }

            if (_namedStrokewidths.containsKey(attr)) {
                return _namedStrokewidths.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected pen name: {0}", attr);
                return 1;
            }
        }
    }

    /**
     * Interpret a specified Circle from the line, applying the given matrix, if
     * any.
     *
     * @param line Line containing Circle description
     * @param matrix Matrix to be applied (if any)
     */
    private Circle interpretCircle(String line, double[][] matrix) {
        String[] parts = line.split(" ");
        double r = Vector.subtract(
                interpretPosition(parts[0], parts[1], matrix),
                interpretPosition("0", "0", matrix)).length();
        Vector c = interpretPosition(parts[4], parts[5], matrix);
        return new Circle(c, r);
    }

    /**
     * Interpret a specified CircularArc from the line, applying the given
     * matrix, if any.
     *
     * @param line Line containing CircularArc description
     * @param matrix Matrix to be applied (if any)
     */
    private CircularArc interpretCircularArc(String line, Vector prev, double[][] matrix) {
        String[] parts = line.split(" ");
        Vector origin = interpretPosition("0", "0", matrix);
        Vector arm1 = Vector.subtract(
                interpretPosition(parts[0], parts[1], matrix),
                origin);
        Vector arm2 = Vector.subtract(
                interpretPosition(parts[2], parts[3], matrix),
                origin);
        Vector center = interpretPosition(parts[4], parts[5], matrix);
        Vector end = interpretPosition(parts[6], parts[7], matrix);

        CircularArc arc;
        if (Vector.crossProduct(arm1, arm2) > 0) {
            // counter clockwise
            arc = new CircularArc(center, prev, end, true);
        } else {
            arc = new CircularArc(center, prev, end, false);
            // clockwise by default
        }
        return arc;
    }
    //</editor-fold>
}
