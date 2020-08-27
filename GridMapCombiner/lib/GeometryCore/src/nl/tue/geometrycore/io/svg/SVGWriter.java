/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.svg;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.geometryrendering.AffineTransformUtil;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.BaseWriter;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * Writer for the IPE xml format.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SVGWriter extends BaseWriter<String, Appendable> {

    // -------------------------------------------------------------------------
    // PUBLIC STATIC FIELDS
    // -------------------------------------------------------------------------     
    public static Rectangle getA4Size() {
        return new Rectangle(0, 595, 0, 842);
    }

    public static Rectangle getHDSize() {
        return new Rectangle(0, 1920, 0, 1080);
    }

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------         
    private Rectangle _world;
    private Rectangle _view;
    private AffineTransform _screenToView;
    private AffineTransform _worldToView;
    private double _zoom = 1;
    private boolean _writeGroupAsCompositePath = false;
    private String _customAttributes = null;

    private double _viewstrokewidth;
    private Appendable _out;
    private File _file;
    private boolean _clipboard;

    private SVGWriter() {
        _world = _view = getA4Size();
    }

    private SVGWriter(File file) {
        this();
        _file = file;
        _clipboard = false;
    }

    private SVGWriter(boolean clipboard) {
        this();
        _file = null;
        _clipboard = clipboard;
    }

    public static SVGWriter clipboardWriter() {
        return new SVGWriter(true);
    }

    public static SVGWriter fileWriter(File file) {
        return new SVGWriter(file);
    }

    // -------------------------------------------------------------------------
    // PROTECTED ABSTRACT METHODS
    // -------------------------------------------------------------------------
    protected void write(String string) throws IOException {
        _out.append(string);
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------
    @Override
    public Appendable getRenderObject() {
        return _out;
    }

    public void setNoTransformationView(Rectangle view) {
        _world = _view = view;
        _worldToView = null;
        //_screenToView = new AffineTransform(new double[]{1, 0, 0, -1, 0, -view.height()});
        _zoom = 1;
    }

    public void setTransformation(Rectangle world, Rectangle view) {
        _world = world;
        _view = view;

        if (world != view) {

            //_screenToView = new AffineTransform(new double[]{1, 0, 0, -1, 0, -view.height()});
            _worldToView = new AffineTransform();
            AffineTransformUtil.setWorldToView(_worldToView, world, view);

            _zoom = _worldToView.getScaleX();
        } else {
            _zoom = 1;
            _worldToView = null;
        }
    }

    public void setWriteGroupAsCompositePath(boolean writeGroupAsCompositePath) {
        _writeGroupAsCompositePath = writeGroupAsCompositePath;
    }

    public boolean isWriteGroupAsCompositePath() {
        return _writeGroupAsCompositePath;
    }

    public void setCustomAttributes(String customAttributes) {
        _customAttributes = customAttributes;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATIONS OF BASE CLASS
    // -------------------------------------------------------------------------
    @Override
    public void draw(Collection<? extends GeometryConvertable> geos) {

        try {
            _viewstrokewidth = _sizeMode == SizeMode.WORLD && _zoom != 1 ? _zoom * _strokewidth : _strokewidth;

            for (GeometryConvertable gc : geos) {
                if (gc == null) {
                    continue;
                }

                writeGeometry(gc.toGeometry(), false, false);
            }

        } catch (IOException ex) {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        String trans = " transform=\"matrix(" + matrix[0] + "," + matrix[1]
                + "," + matrix[2] + "," + matrix[3]
                + "," + matrix[4] + "," + matrix[5] + ")\"";

        try {
            write("<g" + trans + ">\n");
        } catch (IOException ex) {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void popMatrix() {
        try {
            write("</g>\n");
        } catch (IOException ex) {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pushGroup() {
        try {
            write("<g>\n");
        } catch (IOException ex) {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void popGroup() {
        try {
            write("</g>\n");
        } catch (IOException ex) {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Vector location, String text) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initialize() throws IOException {

        if (_file != null) {
            _out = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(_file), StandardCharsets.UTF_8)));
        } else {
            _out = new StringBuilder();
        }

        write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");

        write("<svg xmlns=\"http://www.w3.org/2000/svg\""
                + " width=\"" + _view.width()
                + "\" height=\"" + _view.height()
                + "\" viewBox=\"" + _view.getLeft() + " " + _view.getBottom() + " " + _view.width() + " " + _view.height()
                + "\">\n");

        _screenToView = new AffineTransform(new double[]{1, 0, 0, -1, 0, _view.height()});
        if (_screenToView != null) {
            pushMatrix(_screenToView);
        }
        if (_worldToView != null) {
            pushMatrix(_worldToView);
        }
    }

    @Override
    public String closeWithResult() throws IOException {
        if (_worldToView != null) {
            popMatrix();
        }
        if (_screenToView != null) {
            popMatrix();
        }

        write("</svg>");

        if (_file != null) {
            ((BufferedWriter) _out).close();
            return null;
        } else {
            String result = ((StringBuilder) _out).toString();
            if (_clipboard) {
                ClipboardUtil.setClipboardContents(result);
            }
            return result;
        }
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void popClipping() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // -------------------------------------------------------------------------
    // INTERNAL METHODS
    // -------------------------------------------------------------------------
    private String getStyleAndTransform() {
        String strokeAttr = getStrokeAttribute();
        String fillAttr = getFillAttribute();
        String penAttr = getPenAttribute();
        String dashAttr = getDashAttribute();
        String alphaAttr = getOpacityAttribute();

        return " style=\"stroke-linecap:round;stroke-linejoin:round;" + strokeAttr + penAttr + fillAttr + dashAttr + alphaAttr + "\"";
    }

    private String getPointStyleAndTransform() {
        String strokeAttr = getStrokeAttribute();
        String fillAttr = getFillAttribute();
        String penAttr = getPointPenAttribute();
        String dashAttr = getDashAttribute();
        String alphaAttr = getOpacityAttribute();

        return " style=\"stroke-linecap:round;stroke-linejoin:round;" + strokeAttr + penAttr + fillAttr + dashAttr + alphaAttr + "\"";
    }

    private String getOpacityAttribute() {
        if (_alpha < 1) {
            return "stroke-opacity:" + _alpha + ";fill-opacity:" + _alpha + ";";
        } else {
            return "";
        }
    }

    private String getDashAttribute() {
        if (_dash == null) {
            return "";
        } else {
            double[] pattern = _dash.getPattern();
            if (_sizeMode == SizeMode.WORLD && _zoom != 1) {
                double[] newpattern = new double[pattern.length];
                for (int i = 0; i < newpattern.length; i++) {
                    newpattern[i] = _zoom * pattern[i];

                }
                pattern = newpattern;
            }
            return "stroke-dasharray:" + dashToString(pattern) + ";";
        }
    }

    private String getPenAttribute() {
        if (_sizeMode != SizeMode.WORLD && _zoom != 1) {
            _strokewidth = _strokewidth / _zoom;
        }

        return "stroke-width:" + _strokewidth + ";";
    }

    private String getPointPenAttribute() {
        double strokewidth = _pointsize * _pointstyle.getStrokeWidth();
        if (_sizeMode == SizeMode.WORLD && _zoom != 1) {
            strokewidth = _zoom * strokewidth;
        }

        return "stroke-width:" + strokewidth + ";";
    }

    private String getFillAttribute() {
        if (_fillcolor == null) {
            return "fill:none;";
        } else {
            return "fill:" + colorToString(_fillcolor) + ";";
        }
    }

    private String getStrokeAttribute() {
        if (_strokecolor == null) {
            return "stroke:none;";
        } else {
            return "stroke:" + colorToString(_strokecolor) + ";";
        }
    }

    protected String colorToString(Color color) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    protected String dashToString(double[] dash) {
        String result = "" + dash[0];
        for (int i = 1; i < dash.length; i++) {
            result += "," + dash[i];
        }
        result += "";
        return result;
    }

    public String convertLength(double len) {
        return "" + len;
    }

    public String convertX(double x) {
        return "" + x;
    }

    public String convertY(double y) {
        if (_screenToView != null) {
            return "" + y;
        } else {
            return "" + (_world.getTop() - y);
        }
    }

    public String pointToString(Vector v) {
        return convertX(v.getX()) + "," + convertY(v.getY());
    }

    private void writeGeometry(BaseGeometry g, boolean inpath, boolean suppressmove) throws IOException {

        if (g instanceof Vector) {
            writeVector((Vector) g, inpath);
        } else if (g instanceof LineSegment) {
            writeLineSegment((LineSegment) g, inpath, suppressmove);
        } else if (g instanceof HalfLine) {
            writeHalfLine((HalfLine) g, inpath);
        } else if (g instanceof Line) {
            writeLine((Line) g, inpath);
        } else if (g instanceof PolyLine) {
            writePolyLine((PolyLine) g, inpath, suppressmove);
        } else if (g instanceof Polygon) {
            writePolygon((Polygon) g, inpath);
        } else if (g instanceof Rectangle) {
            writeRectangle((Rectangle) g, inpath);
        } else if (g instanceof Circle) {
            writeCircle((Circle) g, inpath);
        } else if (g instanceof CircularArc) {
            writeCircularArc((CircularArc) g, inpath, suppressmove);
        } else if (g instanceof GeometryGroup) {
            writeGeometryGroup((GeometryGroup) g, inpath);
        } else if (g instanceof GeometryGroup) {
            writeGeometryString((GeometryString) g, inpath, suppressmove);
        } else if (g instanceof GeometryGroup) {
            writeGeometryCycle((GeometryCycle) g, inpath);
        } else {
            Logger.getLogger(SVGWriter.class.getName()).log(Level.WARNING, "Unexpected geometry type: {0}", g.getClass().getSimpleName());
        }
    }

    private void writeVector(Vector vector, boolean inpath) throws IOException {
        // NB: in a path, this is already transformed by a matrix into view space
        // thus size must be converted into worldspace!
        BaseGeometry represent = _pointstyle.represent(vector, _sizeMode == SizeMode.WORLD && _zoom != 1 ? _pointsize : _pointsize / _zoom);

        if (!inpath) {
            writePointPathStart();
        }

        writeGeometry(represent, true, false);

        if (!inpath) {
            writeEndPath();
        }
    }

    private void writeLineSegment(Vector start, Vector end, boolean inpath, boolean suppressmove) throws IOException {

        if (!inpath) {
            assert !suppressmove;

            write("<line" + getStyleAndTransform()
                    + " x1=\"" + convertX(start.getX())
                    + "\" y1=\"" + convertY(start.getY())
                    + "\" x2=\"" + convertX(end.getX())
                    + "\" y2=\"" + convertY(end.getY())
                    + "\"/>\n");
        } else {

            if (!suppressmove) {
                write("M" + pointToString(start));
            }
            write("L" + pointToString(end));

        }
    }

    private void writeLineSegment(LineSegment lineSegment, boolean inpath, boolean suppressmove) throws IOException {
        writeLineSegment(lineSegment.getStart(), lineSegment.getEnd(), inpath, suppressmove);
    }

    private void writeHalfLine(HalfLine halfLine, boolean inpath) throws IOException {
        List<BaseGeometry> intersections = _world.intersect(halfLine);

        // four outcomes:
        // (1) no intersections: halfline isnt visible
        // (2) an overlap: halfline is partially visible, but only that what overlaps a side (overlap is a LineSegment)
        // (3) 1 intersection: origin lies within view
        // (4) 2 intersections: origin lies outside, but halfline crosses view
        if (intersections.isEmpty()) {
            // (1)
        } else if (intersections.get(0) instanceof Vector) {
            // (3) or (4)
            Vector end;
            Vector is1 = (Vector) intersections.get(0);
            if (intersections.size() > 1) {
                // (4)
                Vector is2 = (Vector) intersections.get(1);
                if (is1.squaredDistanceTo(halfLine.getOrigin()) < is2.squaredDistanceTo(halfLine.getOrigin())) {
                    // is1 is nearer, draw all the way to is2
                    end = is2;
                } else {
                    // is2 is nearer, draw all the way to is1
                    end = is1;
                }
            } else {
                // (3)
                end = is1;
            }

            double deltaX = 5 * _viewstrokewidth * halfLine.getDirection().getX();
            double deltaY = 5 * _viewstrokewidth * halfLine.getDirection().getY();
            end.translate(deltaX, deltaY);

            writeLineSegment(halfLine.getOrigin(), end, inpath, false);
        } else {
            // (2)
            LineSegment ls = (LineSegment) intersections.get(0);

            Vector dir = ls.getDirection();
            dir.scale(5 * _viewstrokewidth);

            if (ls.getStart().squaredDistanceTo(halfLine.getOrigin()) < ls.getEnd().squaredDistanceTo(halfLine.getOrigin())) {
                // start is closer, snap to origin
                ls.getStart().set(halfLine.getOrigin());

                // extend end
                ls.getEnd().translate(dir);
            } else {
                // extend start
                dir.invert();
                ls.getStart().translate(dir);

                // end is closer, snap to origin
                ls.getEnd().set(halfLine.getOrigin());
            }

            writeLineSegment(ls, inpath, false);
        }
    }

    private void writeLine(Line line, boolean inpath) throws IOException {
        List<BaseGeometry> intersections = _world.intersect(line);

        // four outcomes:
        // (1) no intersections: line isnt visible
        // (2) an overlap: line is partially visible, but only that what overlaps a side (overlap is a LineSegment)
        // (3) 1 intersection: line touches a corner
        // (4) 2 intersections: line crosses view
        if (intersections.isEmpty()) {
            // (1)
        } else if (intersections.size() == 2) {
            // (4)
            Vector start = (Vector) intersections.get(0);
            Vector end = (Vector) intersections.get(1);

            double distance = start.distanceTo(end);
            double deltaX = 5 * _viewstrokewidth * (end.getX() - start.getX()) / distance;
            double deltaY = 5 * _viewstrokewidth * (end.getY() - start.getY()) / distance;

            start.translate(-deltaX, -deltaY);
            end.translate(deltaX, deltaY);

            writeLineSegment(start, end, inpath, false);

        } else if (intersections.get(0) instanceof Vector) {
            // (3)
            Vector start = (Vector) intersections.get(0);

            double deltaX = 5 * _viewstrokewidth * line.getDirection().getX();
            double deltaY = 5 * _viewstrokewidth * line.getDirection().getY();

            Vector end = start.clone();
            end.translate(deltaX, deltaY);
            start.translate(-deltaX, -deltaY);

            writeLineSegment(start, end, inpath, false);
        } else {
            // (2)
            LineSegment ls = (LineSegment) intersections.get(0);

            Vector dir = ls.getDirection();
            dir.scale(5 * _viewstrokewidth);

            ls.getEnd().translate(dir);
            dir.invert();
            ls.getStart().translate(dir);

            writeLineSegment(ls, inpath, false);
        }
    }

    private void writePolyLine(PolyLine polyLine, boolean inpath, boolean suppressmove) throws IOException {
        if (!inpath) {
            assert !suppressmove;
            write("<polyline" + getStyleAndTransform() + " points=\"");
            write(pointToString(polyLine.vertex(0)));
            for (int i = 1; i < polyLine.vertexCount(); i++) {
                write(" " + pointToString(polyLine.vertex(i)));
            }
            write("\"/>\n");
        } else {
            if (!suppressmove) {
                write("M" + pointToString(polyLine.vertex(0)));
            }
            if (polyLine.vertexCount() > 1) {
                write("L" + pointToString(polyLine.vertex(1)));
                for (int i = 1; i < polyLine.vertexCount(); i++) {
                    write(" " + pointToString(polyLine.vertex(i)));
                }
            }
        }
    }

    private void writePolygon(Polygon polygon, boolean inpath) throws IOException {
        if (!inpath) {
            write("<polygon" + getStyleAndTransform() + " points=\"");
            write(pointToString(polygon.vertex(0)));
            for (int i = 1; i < polygon.vertexCount(); i++) {
                write(" " + pointToString(polygon.vertex(i)));
            }
            write("\"/>\n");
        } else {
            write("M" + pointToString(polygon.vertex(0)));
            write("L" + pointToString(polygon.vertex(1)));
            for (int i = 1; i < polygon.vertexCount(); i++) {
                write(" " + pointToString(polygon.vertex(i)));
            }
            write("Z");
        }
    }

    private void writeRectangle(Rectangle rectangle, boolean inpath) throws IOException {
        if (!inpath) {
            write("<rect" + getStyleAndTransform()
                    + " x=\"" + convertX(rectangle.getLeft())
                    + "\" y=\"" + convertY(rectangle.getTop())
                    + "\" height=\"" + convertLength(rectangle.height())
                    + "\" width=\"" + convertLength(rectangle.width()) + "\"/>\n");
        } else {
            write("M" + pointToString(rectangle.leftTop()));
            write("L" + pointToString(rectangle.rightTop()));
            write(" " + pointToString(rectangle.rightBottom()));
            write(" " + pointToString(rectangle.leftBottom()));
            write("Z");
        }
    }

    private void writeCircle(Circle circle, boolean inpath) throws IOException {
        if (!inpath) {
            write("<circle" + getStyleAndTransform()
                    + " cx=\"" + convertX(circle.getCenter().getX())
                    + "\" cy=\"" + convertY(circle.getCenter().getY())
                    + "\" r=\"" + convertLength(circle.getRadius()) + "\"/>\n");
        } else {
            // got to use two circular arcs to make a full circle
            double r = circle.getRadius();
            write("M" + pointToString(circle.getCenter()));
            write("m-" + r + ",0");
            write("a" + r + "," + r + " 0 1,1 " + (2 * r) + ",0");
            write("a" + r + "," + r + " 0 1,1 -" + (2 * r) + ",0");
        }
    }

    private void writeCircularArc(CircularArc circularArc, boolean inpath, boolean suppressmove) throws IOException {
        if (!inpath) {
            writePathStart();
        }

        if (!suppressmove) {
            write("M" + pointToString(circularArc.getStart()));
        }

        if (circularArc.getCenter() == null) {
            write("L" + pointToString(circularArc.getEnd()));
        } else {
            double r = circularArc.radius();
            String sweepLarge;
            if (circularArc.centralAngle(r) > Math.PI) {
                sweepLarge = "1";
            } else {
                sweepLarge = "0";
            }

            String sweepCW;
            if (circularArc.isCounterclockwise()) {
                sweepCW = "1";
            } else {
                sweepCW = "0";
            }

            write("A" + r + "," + r + " 0 " + sweepLarge + "," + sweepCW + " " + pointToString(circularArc.getEnd()));
        }

        if (!inpath) {
            writeEndPath();
        }
    }

    private void writeGeometryGroup(GeometryGroup<? extends BaseGeometry> group, boolean inpath) throws IOException {

        if (!inpath) {
            if (_writeGroupAsCompositePath) {
                writePathStart();
            } else {
                write("<g>\n");
            }
        }
        for (BaseGeometry part : group.getParts()) {
            writeGeometry(part, inpath || _writeGroupAsCompositePath, false);
        }
        if (!inpath) {
            if (_writeGroupAsCompositePath) {
                writeEndPath();
            } else {
                write("</g>\n");
            }
        }
    }

    private void writeGeometryString(GeometryString<? extends OrientedGeometry> string, boolean inpath, boolean suppressmove) throws IOException {
        if (!inpath) {
            writePathStart();
        }

        for (OrientedGeometry edge : string.edges()) {
            writeGeometry(edge, true, suppressmove);
            suppressmove = true;
        }

        if (!inpath) {
            writeEndPath();
        }
    }

    private void writeGeometryCycle(GeometryCycle<? extends OrientedGeometry> cycle, boolean inpath) throws IOException {
        if (!inpath) {
            writePathStart();
        }

        boolean suppressmove = false;
        for (OrientedGeometry edge : cycle.edges()) {
            writeGeometry(edge, true, suppressmove);
            suppressmove = true;
        }
        write("h\n");

        if (!inpath) {
            writeEndPath();
        }
    }

    private void writePathStart() throws IOException {
        write("<path" + getStyleAndTransform() + (_customAttributes == null ? "" : " " + _customAttributes) + " d=\"");
    }

    private void writePointPathStart() throws IOException {
        write("<path" + getPointStyleAndTransform() + (_customAttributes == null ? "" : " " + _customAttributes) + " d=\"");
    }

    private void writeEndPath() throws IOException {
        write("\"/>\n");
    }
}
