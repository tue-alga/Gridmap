/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.ipe;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.InfiniteGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.geometryrendering.AffineTransformUtil;
import nl.tue.geometrycore.geometryrendering.glyphs.Glyph;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.BaseWriter;
import nl.tue.geometrycore.io.LayeredWriter;
import nl.tue.geometrycore.util.ClipboardUtil;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;

/**
 * Writer for the IPE XML format. It can handle both files as well as selection
 * code, allowing easy copy-pasting to IPE from Java.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IPEWriter extends BaseWriter<String, Appendable> implements LayeredWriter {

    //<editor-fold defaultstate="collapsed" desc="STATIC METHODS">
    /**
     * Provides the page dimensions to get an A4-sized page in IPE.
     *
     * @return new rectangle of 595 by 842
     */
    public static Rectangle getA4Size() {
        return new Rectangle(0, 595, 0, 842);
    }

    /**
     * Provides the page dimensions for a 4:3 presentation in IPE.
     *
     * @return new rectangle of 800 by 600
     */
    public static Rectangle getPresentationSize() {
        return new Rectangle(-16, 784, -16, 684);
    }

    /**
     * Provides the page dimensions for a 16:9 view in IPE.
     *
     * @return new rectangle of 1920 by 1080
     */
    public static Rectangle getHDSize() {
        return new Rectangle(0, 1920, 0, 1080);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    // Writing basics
    private Appendable _out;
    private final boolean _fileMode;
    private final File _file;
    private final boolean _clipboard;
    // Default maps
    private Map<String, Color> _namedColors;
    private Map<String, Double> _namedStrokewidths;
    private Map<String, Double> _namedSymbolsizes;
    private Map<String, Double> _namedTransparencies;
    private Map<String, Dashing> _namedDashing;
    // View conttrol
    private final List<AffineTransform> _transformstack;
    private AffineTransform _currentTransform;
    private Rectangle _world;
    private Rectangle _view;
    private double _zoom;
    // layers and pages (only used if _fileMode is true)
    private boolean _layersEnabled = false;
    private String _layer = null;
    private boolean _firstInLayer = false;
    private boolean _onPage = false;
    // other settings
    private boolean _textSerifs = false;
    private boolean _scaleText = true;
    private double _baseTextSize = 8;
    private boolean _useTextDiscrepancy = true;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    private IPEWriter(boolean fileMode, File file) {
        _transformstack = new ArrayList();
        setView(getA4Size());

        _fileMode = fileMode;
        _file = file;
        _clipboard = false;
    }

    private IPEWriter(boolean fileMode, boolean clipboard) {
        _transformstack = new ArrayList();
        setView(getA4Size());

        _fileMode = fileMode;
        _file = null;
        _clipboard = clipboard;
    }

    /**
     * Creates an IPE writer that will output the result to the clipboard,
     * suitable to be pasted into IPE. Note that the clipboard contents are also
     * returned when invoking .close().
     *
     * @return new instance of IPEWriter
     */
    public static IPEWriter clipboardWriter() {
        return new IPEWriter(false, true);
    }

    /**
     * Creates an IPE writer that will output the result to the clipboard,
     * either the complete file (fileMode is true) or suitable for pasting into
     * IPE (fileMode is false). Note that the clipboard contents are also
     * returned when invoking .close().
     *
     * @param fileMode whether to write the full file content
     * @return new instance of IPEWriter
     */
    public static IPEWriter clipboardWriter(boolean fileMode) {
        return new IPEWriter(fileMode, true);
    }

    /**
     * Creates an IPE writer that will return the result on calling .close(),
     * either the complete file (fileMode is true) or suitable for pasting into
     * IPE (fileMode is false).
     *
     * @param fileMode whether to write the full file content
     * @return new instance of IPEWriter
     */
    public static IPEWriter stringWriter(boolean fileMode) {
        return new IPEWriter(fileMode, false);
    }

    /**
     * Creates an IPE writer that will write the full file contents to the
     * provided file. Note that the return value of .close() will be null.
     *
     * @param file file to be created or overwritten
     * @return new instance of IPEWriter
     */
    public static IPEWriter fileWriter(File file) {
        return new IPEWriter(true, file);
    }

    /**
     * Creates an IPE writer that will either write the full file contents to
     * the provided file, or just the code suitable for pasting into IPE. Note
     * that the return value of .close() will be null.
     *
     * @param file file to be created or overwritten
     * @param fileMode whether to write the full file content
     * @return new instance of IPEWriter
     */
    public static IPEWriter fileWriter(File file, boolean fileMode) {
        return new IPEWriter(fileMode, file);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRE-INIT">
    /**
     * Sets up the view area for the document (in file mode). Resets any
     * specified transformation. This method should only be used before calling
     * .initialize() on the writer.
     *
     * @param view rectangle specifying the view area
     */
    public void setView(Rectangle view) {
        _view = view;
        if (_view.leftBottom().isApproximately(Vector.origin())) {
            setWorldview(null);
        } else {
            Rectangle world = _view.clone();
            _view.translate(-_view.getLeft(), -_view.getBottom());
            setWorldview(world);
        }
    }

    /**
     * Shorthand for disabling all named values for IPE. Note that this
     * effectively disables transparency, since these can only be named.
     */
    public void setNoNamedValues() {
        _namedColors = new HashMap();
        _namedStrokewidths = new HashMap();
        _namedSymbolsizes = new HashMap();
        _namedTransparencies = new HashMap();
        _namedDashing = new HashMap();
    }

    /**
     * Configures which transparency values are used. Note that transparencies
     * must be named in IPE; specifying an alpha value will look for the closest
     * value in this map. An alpha value of 1 (fully opaque) need not be
     * included. Setting this to null will use the values from IPEDefaults. This
     * method should only be used before calling .initialize() on the writer.
     *
     * @param namedTransparencies map encoding names
     */
    public void setTransparencies(Map<String, Double> namedTransparencies) {
        _namedTransparencies = namedTransparencies;
    }

    /**
     * Configures which colors are converted to named values. Setting this to
     * null will use the values from IPEDefaults. This method should only be
     * used before calling .initialize() on the writer.
     *
     * @param namedColors map encoding names
     */
    public void setNamedColors(Map<String, Color> namedColors) {
        _namedColors = namedColors;
    }

    /**
     * Configures which strokewidths are converted to named values. Setting this
     * to null will use the values from IPEDefaults. This method should only be
     * used before calling .initialize() on the writer.
     *
     * @param namedWidths map encoding names
     */
    public void setNamedStrokewidths(Map<String, Double> namedWidths) {
        _namedStrokewidths = namedWidths;
    }

    /**
     * Configures which symbol sizes are converted to named values. Setting this
     * to null will use the values from IPEDefaults. This method should only be
     * used before calling .initialize() on the writer.
     *
     * @param namedSizes map encoding names
     */
    public void setNamedSymbolsizes(Map<String, Double> namedSizes) {
        _namedSymbolsizes = namedSizes;
    }

    /**
     * Configures which dash patterns are converted to named values. Setting
     * this to null will use the values from IPEDefaults. This method should
     * only be used before calling .initialize() on the writer.
     *
     * @param namedDashing map encoding names
     */
    public void setNamedDashing(Map<String, Dashing> namedDashing) {
        _namedDashing = namedDashing;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PAGE CONTROL">    
    /**
     * Sets up a transformation such that the provided rectangle in worldspace
     * is rendered in the view rectangle (provided with setView()). Provide null
     * as a parameter to clear the transformation. This method can be called
     * after writing geometries to a page, but the old transformation cannot be
     * recovered.
     *
     * @param world the area in worldspace to be seen in the view area
     *
     */
    public void setWorldview(Rectangle world) {
        if (world == null) {
            _world = _view;
            _currentTransform = null;
            _zoom = 1;
        } else {
            _world = world;

            _transformstack.clear();
            _currentTransform = new AffineTransform();
            AffineTransformUtil.setWorldToView(_currentTransform, world, _view);
            _zoom = _currentTransform.getScaleX();
        }
    }

    /**
     * Closes the current page. This method should only be used for writers in
     * file mode.
     */
    public void closePage() {
        if (!_fileMode) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.WARNING, "Trying to use a file-mode-only method while not in file mode");
            return;
        }
        if (!_onPage) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, "Calling closePage() while not on a page: command omitted but IPE file may be corrupt");
            return;
        }
        try {
            write("</page>\n");
            _onPage = false;
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts a new page with the given list of layers. Layer names may not
     * contain whitespaces. If no layers are specified, one layer "default", is
     * created. A call to this method must be followed by one or more calls to
     * newView() to specify the visible layers. This method should only be used
     * for writers in file mode.
     *
     * @param layers layers for the new page
     */
    public void newPage(String... layers) {

        if (!_fileMode) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.WARNING, "Trying to use a file-mode-only method while not in file mode");
            return;
        }

        _layersEnabled = layers.length > 0;
        _firstInLayer = true;

        if (_layersEnabled) {
            _layer = layers[0];
        } else {
            _layer = "default";
            layers = new String[]{"default"};
        }

        try {
            if (_onPage) {
                write("</page>\n");
                _onPage = false;
            }

            write("<page>\n");
            for (String layer : layers) {
                write("<layer name=\"" + layer + "\"/>\n");
            }
            _onPage = true;
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Specifies a new view with the given list of layers visible. If no layers
     * were specified during newPage(), then a view with the default layer
     * visible is created. This method should only be used for writers in file
     * mode.
     *
     * @param visible visible layers in the new view
     */
    public void newView(String... visible) {

        if (!_fileMode) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.WARNING, "Trying to use a file-mode-only method while not in file mode");
            return;
        }
        if (!_onPage) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, "Calling newView while not on a page; IPE file may be corrupt");
        }

        try {

            if (_layersEnabled) {
                write("<view layers=\"" + visible[0]);
                for (int i = 1; i < visible.length; i++) {
                    write(" " + visible[i]);
                }
                write("\" active=\"" + visible[0] + "\"/>\n");
            } else {
                write("<view layers=\"default\" active=\"default\"/>\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void pushMatrix(AffineTransform transform) {
        _transformstack.add(_currentTransform);
        if (_currentTransform != null) {
            transform.preConcatenate(_currentTransform);
        }
        _currentTransform = transform;
    }

    @Override
    public void popMatrix() {
        _currentTransform = _transformstack.remove(_transformstack.size() - 1);
    }

    /**
     * Specifies the layer for next items to be written. This method should only
     * be used for writers in file mode. Note that the layer should be one
     * specified in the most recent call to newPage().
     *
     * @param layer layer name
     */
    public void setLayer(String layer) {
        if (!_fileMode) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.WARNING, "Trying to use a file-mode-only method while not in file mode");
            return;
        }

        _layer = layer;
        _firstInLayer = true;
    }

    @Override
    public void pushGroup() {
        startGroup(null);
    }

    @Override
    public void popGroup() {
        endGroup();
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        startGroup(geometry);
    }

    @Override
    public void popClipping() {
        endGroup();
    }

    public void setTextSerifs(boolean textSerifs) {
        _textSerifs = textSerifs;
    }

    @Override
    public void initialize() throws IOException {

        // initialize defaults, unless specified
        if (_namedColors == null) {
            _namedColors = IPEDefaults.getColors();
        }
        if (_namedSymbolsizes == null) {
            _namedSymbolsizes = IPEDefaults.getSymbolSizes();
        }
        if (_namedStrokewidths == null) {
            _namedStrokewidths = IPEDefaults.getStrokeWidths();
        }
        if (_namedDashing == null) {
            _namedDashing = IPEDefaults.getDashStyles();
        }
        if (_namedTransparencies == null) {
            _namedTransparencies = IPEDefaults.getTransparencies();
        }

        if (_file != null) {
            _out = new BufferedWriter(new FileWriter(_file));
        } else {
            _out = new StringBuilder();
        }

        if (_fileMode) {
            write("<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE ipe SYSTEM \"ipe.dtd\">\n"
                    + "<ipe version=\"70010\" creator=\"Ipe 7.0.10\">\n"
                    + "<info created=\"D:20100909134504\" modified=\"D:20100909150018\"/>\n"
                    + "<ipestyle name=\"GeometryCoreExport\">\n"
                    + "<layout paper=\"" + _view.width() + " " + _view.height()
                    + "\" origin=\"0 0\" frame=\"" + _view.width() + " " + _view.height() + "\"/>\n");

            for (Entry<String, Color> entry : _namedColors.entrySet()) {
                write("<color name=\"" + entry.getKey() + "\" value=\"" + colorToString(entry.getValue()) + "\"/>\n");
            }

            for (Entry<String, Double> entry : _namedSymbolsizes.entrySet()) {
                write("<symbolsize name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n");
            }

            for (Entry<String, Double> entry : _namedStrokewidths.entrySet()) {
                write("<pen name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n");
            }

            for (Entry<String, Dashing> entry : _namedDashing.entrySet()) {
                if (entry.getValue() != null) {
                    write("<dashstyle name=\"" + entry.getKey() + "\" value=\"" + dashToString(entry.getValue().getPattern()) + "\"/>\n");
                }
            }

            for (Entry<String, Double> entry : _namedTransparencies.entrySet()) {
                write("<opacity name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n");
            }

            write("</ipestyle>\n");
        } else {
            write("<ipeselection pos=\"0 0\">\n");
        }
    }

    @Override
    public String closeWithResult() throws IOException {
        if (_fileMode) {
            ensurePage();
            write("</page>\n</ipe>");
        } else {
            write("</ipeselection>");
        }

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

    /**
     * Configures how text is positioned and sized. If scaleText is true,
     * transformations on the text items are set to "affine" and matrix
     * transformations are used to determine the text size, using the given
     * baseTextSize as the font size to be scaled. If set to false, then the
     * font size is configured directly: no scaling is performed and
     * transformations on the text are set to "rigid". Note that this may give
     * warped text if the resulting font size is very small or very big.
     *
     * Raster rendering and IPE's text size scale differently.
     * useTextDiscrepancy indicates whether compensation needs to happen, using
     * {@link IPEDefaults.TEXT_DISCREPANCY}. Set to false to use IPE's text
     * sizing.
     *
     * @param scaleText
     * @param baseTextSize
     * @param useTextDiscrepancy
     */
    public void configureTextHandling(boolean scaleText, double baseTextSize, boolean useTextDiscrepancy) {
        _baseTextSize = baseTextSize;
        _scaleText = scaleText;
        _useTextDiscrepancy = useTextDiscrepancy;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RENDERING">
    @Override
    public Appendable getRenderObject() {
        return _out;
    }

    @Override
    public void draw(Vector location, String text) {
        try {
            ensurePage();

            String fontSetting = "";
            String fontSettingEnd = "";
            if (!_textSerifs) {
                fontSetting += "\\textsf{";
                fontSettingEnd += "}";
            }
            switch (_fontstyle) {
                default:
                case NORMAL:
                    // do nothing
                    break;
                case BOLD:
                    fontSetting += "\\textbf{";
                    fontSettingEnd += "}";
                    break;
                case ITALICS:
                    fontSetting += "\\textit{";
                    fontSettingEnd += "}";
                    break;
            }

            if (_scaleText) {
                AffineTransform transform = new AffineTransform();
                transform.translate(location.getX(), location.getY());
                transform.scale(_textsize / _baseTextSize / (_useTextDiscrepancy ? IPEDefaults.TEXT_DISCREPANCY : 1.0), _textsize / _baseTextSize / (_useTextDiscrepancy ? IPEDefaults.TEXT_DISCREPANCY : 1.0));
                pushMatrix(transform);
                write("<text type=\"label\" transformations=\"affine\""
                        + " pos=\"0 0\""
                        + " size=\"" + _baseTextSize + "\""
                        + getStrokeAttribute()
                        + getAnchorAttribute()
                        + getMatrixAttribute()
                        + getLayerAttribute()
                        + getOpacityAttribute()
                        + ">"
                        + fontSetting
                        + text
                        + fontSettingEnd
                        + "</text>\n");
                popMatrix();
            } else {
                write("<text type=\"label\" transformations=\"rigid\""
                        + " pos=\"" + location.getX() + " " + location.getY() + "\""
                        + " size=\"" + _textsize / (_useTextDiscrepancy ? IPEDefaults.TEXT_DISCREPANCY : 1.0) + "\""
                        + getStrokeAttribute()
                        + getAnchorAttribute()
                        + getMatrixAttribute()
                        + getLayerAttribute()
                        + getOpacityAttribute()
                        + ">"
                        + fontSetting
                        + text
                        + fontSettingEnd
                        + "</text>\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        try {
            ensurePage();

            // NB: IPE only does strokes in viewspace!
            Rectangle clip = _world.clone();
            clip.grow(3 * size(_strokewidth, true));

            for (GeometryConvertable gc : geometries) {
                if (gc == null) {
                    continue;
                }

                BaseGeometry geom = gc.toGeometry();
                if (geom.getGeometryType().isInfinite()) {
                    geom = ((InfiniteGeometry) geom).clip(clip);
                }

                if (geom == null) {
                    continue;
                }

                switch (geom.getGeometryType()) {
                    case VECTOR: {
                        renderPoint((Vector) geom);
                        break;
                    }
                    case GEOMETRYGROUP: {
                        renderComposite(geom);
                        break;
                    }
                    default: {
                        renderGeometry(geom);
                        break;
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startCustomPath() {
        try {
            ensurePage();
            writePathStart(true);
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void appendCustomPathCommand(String s) {
        try {
            write(s + "\n");
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void endCustomPath() {
        try {
            writePathEnd();
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void renderGlyph(Glyph glyphstyle, BaseGeometry representation) throws IOException {
        writeGlyphStart(glyphstyle);
        writeGeometry(representation, false);
        writeGlyphEnd();
    }

    private void renderPoint(Vector point) throws IOException {
        if (_pointstyle == null) {
            return;
        }

        // TODO: named marks
        BaseGeometry representation = _pointstyle.represent(point, size(_pointsize, true));
        renderGlyph(_pointstyle, representation);
    }

    private void renderGeometry(BaseGeometry geom) throws IOException {
        // assume that this isn't a Vector or a GeometryGroup

        boolean needsfill = (geom.getGeometryType().isCyclic() || geom.getGeometryType() == GeometryType.GEOMETRYGROUP);
        if (_hash != null && needsfill) {
            renderHashure(geom);

            // no need to render path anymore
            if (_strokecolor == null) {
                return;
            }
        }

        writePathStart(_hash == null && needsfill);
        writeGeometry(geom, false);
        writePathEnd();

        // TODO: named arrow heads?
        if (geom.getGeometryType().isOrientable()) {
            renderArrowheads((OrientedGeometry) geom);
        }
    }

    private void renderArrowheads(OrientedGeometry geom) throws IOException {
        if (_backwardArrow != null && _backwardArrowsize != 0) {
            Vector backdir = geom.getStartTangent();
            backdir.invert();
            BaseGeometry arrow = _backwardArrow.represent(geom.getStart(), backdir, size(_backwardArrowsize, true));
            renderGlyph(_backwardArrow, arrow);
        }

        if (_forwardArrow != null && _forwardArrowsize != 0) {
            Vector fwddir = geom.getEndTangent();
            BaseGeometry arrow = _forwardArrow.represent(geom.getEnd(), fwddir, size(_forwardArrowsize, true));
            renderGlyph(_forwardArrow, arrow);
        }
    }

    private void renderHashure(BaseGeometry geom) throws IOException {
        Pair<List<LineSegment>,Integer> hashures = _hash.computeHashures(geom, size(1, true));

        startGroup(geom);
        int pi = hashures.getSecond();
        for (LineSegment hashure : hashures.getFirst()) {
            writeHashureStart(_hash.getPattern()[pi]);
            writeLineSegment(hashure, false);
            writeHashureEnd();
            pi = (pi + 2) % _hash.getPattern().length;
        }
        endGroup();
    }

    private void renderComposite(BaseGeometry geom) throws IOException {
        renderGeometry(geom);
//        pushGroup();
//        draw(((GeometryGroup) geom).getParts());
//        endGroup();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void write(String string) throws IOException {
        _out.append(string);
    }

    private void ensurePage() throws IOException {
        if (_fileMode && !_onPage) {
            newPage();
            newView();
        }
    }

    private void startGroup(GeometryConvertable clip) {
        try {
            ensurePage();

            if (clip == null) {
                write("<group" + getLayerAttribute() + getMatrixAttribute() + ">\n");
            } else {
                write("<group" + getLayerAttribute() + getMatrixAttribute() + " clip=\"");
                writeGeometry(clip.toGeometry(), false);
                write("\">");
            }
            _transformstack.add(_currentTransform);
            _currentTransform = null;
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void endGroup() {
        try {
            _currentTransform = _transformstack.remove(_transformstack.size() - 1);
            write("</group>\n");
        } catch (IOException ex) {
            Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double size(double givensize, boolean needWorldSpace) {
        if (needWorldSpace) {
            if (_sizeMode == SizeMode.WORLD) {
                return givensize;
            } else {
                return givensize / _zoom;
            }
        } else {
            if (_sizeMode == SizeMode.WORLD) {
                return givensize * _zoom;
            } else {
                return givensize;
            }
        }
    }

    private String colorToString(Color color) {
        return color.getRed() / 255f + " " + color.getGreen() / 255f + " " + color.getBlue() / 255f;
    }

    private String dashToString(double[] dash) {
        String result = "[" + dash[0];
        for (int i = 1; i < dash.length; i++) {
            result += " " + dash[i];
        }
        result += "] 0";
        return result;
    }

    private String pointToString(Vector v) {
        return v.getX() + " " + v.getY();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ATTRIBUTES">
    private <T> String inverseSearch(T obj, Map<String, T> map) {
        for (Entry<String, T> entry : map.entrySet()) {
            if (entry.getValue() == obj) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getOpacityAttribute() {
        if (DoubleUtil.close(_alpha, 1)) {
            return "";
        }

        // NB: must be named
        String closest = "";
        double delta = 1 - _alpha;

        for (Entry<String, Double> entry : _namedTransparencies.entrySet()) {
            double d = Math.abs(_alpha - entry.getValue());
            if (d < delta - DoubleUtil.EPS) {
                closest = " opacity=\"" + entry.getKey() + "\"";
                delta = d;
            }
        }

        return closest;
    }

    private String getLayerAttribute() {
        if (_fileMode && _firstInLayer && _layer != null) {
            _firstInLayer = false;
            return " layer=\"" + _layer + "\"";
        } else {
            return "";
        }
    }

    private String getDashAttribute() {
        if (_dash == null) {
            return "";
        }

        double[] pattern = _dash.getPattern();
        if (_sizeMode == SizeMode.WORLD && _zoom != 1) {
            // pattern must be scaled accordingly
            // NB: dash pattern specified in view space!
            double[] newpattern = new double[pattern.length];
            for (int i = 0; i < newpattern.length; i++) {
                newpattern[i] = size(pattern[i], false);

            }
            pattern = newpattern;
        } else {
            String name = inverseSearch(_dash, _namedDashing);
            if (name != null) {
                return " dash=\"" + name + "\"";
            }
        }
        return " dash=\"" + dashToString(pattern) + "\"";
    }

    private String getPenAttribute() {
        return getPenAttribute(null);
    }

    private String getPenAttribute(Glyph glyphstyle) {
        double strokewidth;

        if (glyphstyle == null) {
            strokewidth = _strokewidth;
        } else {
            switch (glyphstyle.getStrokeWidthMode()) {
                case FIXED:
                    strokewidth = glyphstyle.getStrokeWidth();
                    break;
                default:
                case STROKE:
                    strokewidth = _strokewidth;
                    break;
            }
        }

        return getPenAttribute(strokewidth);
    }

    private String getPenAttribute(double strokewidth) {
        strokewidth = size(strokewidth, false);

        for (Entry<String, Double> entry : _namedStrokewidths.entrySet()) {
            if (DoubleUtil.close(entry.getValue(), strokewidth)) {
                return " pen=\"" + entry.getKey() + "\"";
            }
        }

        return " pen=\"" + strokewidth + "\"";
    }

    private String getMatrixAttribute() {
        if (_currentTransform == null) {
            return "";
        } else {
            double[] flatmatrix = new double[6];
            _currentTransform.getMatrix(flatmatrix);
            return " matrix=\"" + flatmatrix[0] + " " + flatmatrix[1]
                    + " " + flatmatrix[2] + " " + flatmatrix[3]
                    + " " + flatmatrix[4] + " " + flatmatrix[5] + "\"";
        }
    }

    private String getFillAttribute() {
        return getFillAttribute(null);
    }

    private String getFillAttribute(Glyph glyphstyle) {
        Color color;
        if (glyphstyle == null) {
            if (_hash == null) {
                color = _fillcolor;
            } else {
                color = null;
            }
        } else {
            switch (glyphstyle.getFillMode()) {
                default:
                case CLEAR:
                    color = null;
                    break;
                case FILL:
                    color = _fillcolor;
                    break;
                case STROKE:
                    color = _strokecolor;
                    break;
                case FIXED:
                    color = glyphstyle.getFillColor();
                    break;
            }
        }

        if (color == null) {
            return "";
        } else {
            String name = inverseSearch(color, _namedColors);
            if (name != null) {
                return " fill=\"" + name + "\"";
            }
        }

        return " fill=\"" + colorToString(color) + "\"";
    }

    private String getAnchorAttribute() {
        String valign;
        switch (_anchor) {
            case BOTTOM:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                valign = " valign=\"bottom\"";
                break;
            case CENTER:
            case LEFT:
            case RIGHT:
                valign = " valign=\"center\"";
                break;
            case TOP:
            case TOP_LEFT:
            case TOP_RIGHT:
                valign = " valign=\"top\"";
                break;
            default:
            case BASELINE:
                valign = " valign=\"baseline\"";
                break;
        }

        String halign;
        switch (_anchor) {
            case LEFT:
            case TOP_LEFT:
            case BOTTOM_LEFT:
                halign = " halign=\"left\"";
                break;
            case CENTER:
            case TOP:
            case BOTTOM:
            case BASELINE_CENTER:
                halign = " halign=\"center\"";
                break;
            case RIGHT:
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
            case BASELINE_RIGHT:
                halign = " halign=\"right\"";
                break;
            default:
            case BASELINE:
                halign = "";
                break;
        }

        return valign + halign;
    }

    private String getStrokeAttribute() {
        return getStrokeAttribute(null);
    }

    private String getStrokeAttribute(Object object) {
        Color color;
        if (object == null) {
            color = _strokecolor;
        } else if (object instanceof Glyph) {
            Glyph glyphstyle = (Glyph) object;
            switch (glyphstyle.getStrokeMode()) {
                default:
                case CLEAR:
                    color = null;
                    break;
                case STROKE:
                    color = _strokecolor;
                    break;
                case FIXED:
                    color = glyphstyle.getStrokeColor();
                    break;
            }
        } else {
            // hashure!
            color = _fillcolor;
        }

        if (color == null) {
            return "";
        } else {
            String name = inverseSearch(color, _namedColors);
            if (name != null) {
                return " stroke=\"" + name + "\"";
            }
        }

        return " stroke=\"" + colorToString(color) + "\"";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="XML GENERATION">
    private void writeGeometry(BaseGeometry g, boolean suppressmove) throws IOException {

        switch (g.getGeometryType()) {
            case LINESEGMENT:
                writeLineSegment((LineSegment) g, suppressmove);
                break;
            case BEZIERCURVE:
                writeBezierCurve((BezierCurve) g, suppressmove);
                break;
            case CIRCULARARC:
                writeCircularArc((CircularArc) g, suppressmove);
                break;
            case CIRCLE:
                writeCircle((Circle) g, suppressmove);
                break;
            case POLYLINE:
                writePolyLine((PolyLine) g, suppressmove);
                break;
            case POLYGON:
                writePolygon((Polygon) g, suppressmove);
                break;
            case RECTANGLE:
                writeRectangle((Rectangle) g, suppressmove);
                break;
            case GEOMETRYSTRING:
                writeGeometryString((GeometryString) g, suppressmove);
                break;
            case GEOMETRYCYCLE:
                writeGeometryCycle((GeometryCycle) g, suppressmove);
                break;
            case LINE:
            case HALFLINE:
                Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, "Encountered infinite geometry while writing in path tag: {0}", g.getGeometryType());

                // add dummy geometry, so the ipefile doesn't break
                write("0 0 m\n 16 16 l\n");
                break;
            case GEOMETRYGROUP:
                assert !suppressmove;
                for (BaseGeometry part : ((GeometryGroup<?>) g).getParts()) {
                    writeGeometry(part, false);
                }
                break;
            default:
            case VECTOR:
                Logger.getLogger(IPEWriter.class.getName()).log(Level.SEVERE, "Unexpected geometry while writing in path tag: {0}", g.getGeometryType());

                // add dummy geometry, so the ipefile doesn't break
                write("0 0 m\n 16 16 l\n");
                break;

        }
    }

    private void writeLineSegment(LineSegment lineSegment, boolean suppressmove) throws IOException {

        if (!suppressmove) {
            write(pointToString(lineSegment.getStart()) + " m\n");
        }
        write(pointToString(lineSegment.getEnd()) + " l\n");
    }

    private void writePolyLine(PolyLine polyLine, boolean suppressmove) throws IOException {

        if (!suppressmove) {
            write(pointToString(polyLine.vertex(0)) + " m\n");
        }
        for (int i = 1; i < polyLine.vertexCount(); i++) {
            write(pointToString(polyLine.vertex(i)) + " l\n");
        }
    }

    private void writePolygon(Polygon polygon, boolean suppressmove) throws IOException {
        assert !suppressmove;

        write(pointToString(polygon.vertex(0)) + " m\n");
        for (int i = 1; i < polygon.vertexCount(); i++) {
            write(pointToString(polygon.vertex(i)) + " l\n");
        }
        write("h\n");
    }

    private void writeRectangle(Rectangle rectangle, boolean suppressmove) throws IOException {
        assert !suppressmove;

        write(rectangle.getLeft() + " " + rectangle.getBottom() + " m\n");
        write(rectangle.getRight() + " " + rectangle.getBottom() + " l\n");
        write(rectangle.getRight() + " " + rectangle.getTop() + " l\n");
        write(rectangle.getLeft() + " " + rectangle.getTop() + " l\n");
        write("h\n");
    }

    private void writeCircle(Circle circle, boolean suppressmove) throws IOException {
        assert !suppressmove;

        write(circle.getRadius() + " 0 0 " + circle.getRadius() + " " + pointToString(circle.getCenter()) + " e\n");
    }

    private void writeCircularArc(CircularArc circularArc, boolean suppressmove) throws IOException {

        if (!suppressmove) {
            write(pointToString(circularArc.getStart()) + " m\n");
        }
        if (circularArc.getCenter() == null) {
            write(pointToString(circularArc.getEnd()) + " l\n");
        } else {
            double r = circularArc.radius();
            if (circularArc.isCounterclockwise()) {
                write(r + " 0 0 " + r + " " + pointToString(circularArc.getCenter()) + " " + pointToString(circularArc.getEnd()) + " a\n");
            } else {
                write(r + " 0 0 " + (-r) + " " + pointToString(circularArc.getCenter()) + " " + pointToString(circularArc.getEnd()) + " a\n");
            }
        }
    }

    private void writeBezierCurve(BezierCurve bezier, boolean suppressmove) throws IOException {

        if (!suppressmove) {
            write(pointToString(bezier.getStart()) + " m\n");
        }
        if (bezier.getControlpoints().size() == 2) {
            write(pointToString(bezier.getEnd()) + " l\n");
        } else {
            for (int i = 1; i < bezier.getControlpoints().size() - 1; i++) {
                Vector cp = bezier.getControlpoints().get(i);
                write(pointToString(cp)+"\n");
            }
            write(pointToString(bezier.getEnd()) + " c\n");
        }
    }

    private void writeGeometryString(GeometryString<? extends OrientedGeometry> string, boolean suppressmove) throws IOException {

        for (OrientedGeometry edge : string.edges()) {
            writeGeometry(edge, suppressmove);
            suppressmove = true;
        }
    }

    private void writeGeometryCycle(GeometryCycle<? extends OrientedGeometry> cycle, boolean suppressmove) throws IOException {
        assert !suppressmove;

        for (OrientedGeometry edge : cycle.edges()) {
            writeGeometry(edge, suppressmove);
            suppressmove = true;
        }
        write("h\n");
    }

    private void writeGlyphStart(Glyph glyphstyle) throws IOException {
        write("<path cap=\"1\""
                + getLayerAttribute()
                + getMatrixAttribute()
                + getStrokeAttribute(glyphstyle)
                + getFillAttribute(glyphstyle)
                + getPenAttribute(glyphstyle)
                + getOpacityAttribute() + ">\n");
    }

    private void writeGlyphEnd() throws IOException {
        write("</path>\n");
    }

    private void writeHashureStart(double strokewidth) throws IOException {
        write("<path cap=\"1\""
                + getLayerAttribute()
                + getMatrixAttribute()
                + getStrokeAttribute(_hash)
                + getPenAttribute(strokewidth)
                + getOpacityAttribute() + ">\n");
    }

    private void writeHashureEnd() throws IOException {
        write("</path>\n");
    }

    private void writePathStart(boolean usefill) throws IOException {
        write("<path cap=\"1\""
                + getLayerAttribute()
                + getMatrixAttribute()
                + getStrokeAttribute()
                + (usefill ? getFillAttribute() : "")
                + getPenAttribute()
                + getDashAttribute()
                + getOpacityAttribute() + ">\n");
    }

    private void writePathEnd() throws IOException {
        write("</path>\n");
    }
    //</editor-fold>
}
