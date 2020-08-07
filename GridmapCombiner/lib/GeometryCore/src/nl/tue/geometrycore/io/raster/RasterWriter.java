/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.raster;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.AWTGeometryConversion;
import nl.tue.geometrycore.geometryrendering.AffineTransformUtil;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.glyphs.Glyph;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.BaseWriter;
import nl.tue.geometrycore.util.Pair;

/**
 * Provides a renderer based on the Java2D graphics library. Can be used to
 * render to any Graphics2D object, but is particularly useful to render to a
 * panel, see {@link GeometryPanel}, or to render to a bitmap. A BufferedImage
 * may be returned on close, depending on the constructor used.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class RasterWriter extends BaseWriter<BufferedImage, Graphics2D> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final File _file;
    private final BufferedImage _img;
    private final Graphics2D _graphics;
    // transformation settings
    private final AffineTransform _screenToView;
    private final AffineTransform _worldToView;
    private final List<AffineTransform> _renderTransforms;
    private final List<Shape> _renderClips;
    private final Rectangle _worldview;
    // make a backup of the dashed stroke to avoid having to recompute it all the time when dashing needs to be suppresed
    private Stroke _dashedstroke = null;
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">    
    private RasterWriter(Rectangle worldview, int width, int height, BufferedImage img, File file, Color background) {
        _img = img;
        _graphics = _img.createGraphics();
        _file = file;
        _worldview = worldview;

        _renderClips = new ArrayList();
        _renderTransforms = new ArrayList();

        _screenToView = new AffineTransform(new double[]{1, 0, 0, -1, 0, height});
        _worldToView = new AffineTransform();
        AffineTransformUtil.setWorldToView(_worldToView, worldview, new Rectangle(0, width, 0, height));

        if (background != null) {
            _graphics.setColor(background);
            _graphics.fillRect(0, 0, width, height);
        }
    }

    private RasterWriter(AffineTransform worldToView, int width, int height, Graphics2D graphics, Color background) {
        _img = null;
        _graphics = graphics;
        _file = null;

        _renderClips = new ArrayList();
        _renderTransforms = new ArrayList();

        _screenToView = new AffineTransform(new double[]{1, 0, 0, -1, 0, height});
        _worldToView = worldToView;

        _worldview = Rectangle.byCorners(
                convertViewToWorld(new Vector(0, 0)),
                convertViewToWorld(new Vector(width, height)));

        if (background != null) {
            _graphics.setColor(background);
            _graphics.fillRect(0, 0, width, height);
        }
    }

    /**
     * Constructs a writer for the provided graphics object, based on a
     * transformation. The provided width and height should match the dimensions
     * of the graphics object. This writer returns null on close.
     *
     * @param worldToView transformation from worldspace to viewspace
     * @param width width of the graphics object
     * @param height height of the graphics object
     * @param graphics Java2D graphics object to render to
     * @return new renderer that renders to the provided graphics object
     */
    public static RasterWriter graphicsWriter(AffineTransform worldToView, int width, int height, Graphics2D graphics) {
        return new RasterWriter(worldToView, width, height, graphics, Color.white);
    }
    
    /**
     * Constructs a writer for the provided graphics object, based on a
     * transformation. The provided width and height should match the dimensions
     * of the graphics object. This writer returns null on close.
     *
     * @param worldToView transformation from worldspace to viewspace
     * @param width width of the graphics object
     * @param height height of the graphics object
     * @param graphics Java2D graphics object to render to
     * @param background Background color (null for transparent)
     * @return new renderer that renders to the provided graphics object
     */
    public static RasterWriter graphicsWriter(AffineTransform worldToView, int width, int height, Graphics2D graphics, Color background) {
        return new RasterWriter(worldToView, width, height, graphics, background);
    }

    /**
     * Constructs a writer to produce a BufferedImage, which is returned on
     * close.
     *
     * @param worldview the area in worldspace to be mapped onto the bitmap
     * @param width width of the bitmap
     * @param height height of the bitmap
     * @return new renderer to compute the desired bitmap
     */
    public static RasterWriter imageWriter(Rectangle worldview, int width, int height) {
        return imageWriter(worldview, width, height, null, Color.white);
    }
    
    /**
     * Constructs a writer to produce a BufferedImage, which is returned on
     * close.
     *
     * @param worldview the area in worldspace to be mapped onto the bitmap
     * @param width width of the bitmap
     * @param height height of the bitmap
     * @param background Background color (null for transparent)
     * @return new renderer to compute the desired bitmap
     */
    public static RasterWriter imageWriter(Rectangle worldview, int width, int height, Color background) {
        return imageWriter(worldview, width, height, null, background);
    }

    /**
     * Constructs a writer to produce a BufferedImage, which is returned on
     * close. The bitmap is also automatically saved to the provided file. The
     * format depends on the file extension. Supported formats include at least
     * JPEG, PNG, GIF, BMP and WBMP, but are system dependent. Run
     * {@link ImageIO.getWriterFormatNames} to find out the list of available
     * formats on the JRE.
     *
     * @param worldview the area in worldspace to be mapped onto the bitmap
     * @param width width of the bitmap
     * @param height height of the bitmap
     * @param file file to write the bitmap to.
     * @return new renderer to compute the desired bitmap
     */
    public static RasterWriter imageWriter(Rectangle worldview, int width, int height, File file) {
        return new RasterWriter(worldview, width, height, new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), file, Color.white);
    }
    
    /**
     * Constructs a writer to produce a BufferedImage, which is returned on
     * close. The bitmap is also automatically saved to the provided file. The
     * format depends on the file extension. Supported formats include at least
     * JPEG, PNG, GIF, BMP and WBMP, but are system dependent. Run
     * {@link ImageIO.getWriterFormatNames} to find out the list of available
     * formats on the JRE.
     *
     * @param worldview the area in worldspace to be mapped onto the bitmap
     * @param width width of the bitmap
     * @param height height of the bitmap
     * @param file file to write the bitmap to.
     * @param background Background color (null for transparent)
     * @return new renderer to compute the desired bitmap
     */
    public static RasterWriter imageWriter(Rectangle worldview, int width, int height, File file, Color background) {
        return new RasterWriter(worldview, width, height, new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), file, background);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Converts the provided position in viewspace to worldspace.
     *
     * @param viewposition location in viewsspace
     * @return corresponding location in worldspace
     */
    public Vector convertViewToWorld(Vector viewposition) {
        Point2D.Double point = new Point2D.Double(viewposition.getX(), viewposition.getY());
        try {
            _worldToView.inverseTransform(point, point);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GeometryPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Vector(point.x, point.y);
    }

    /**
     * Converts the provided position in worldspace to viewspace.
     *
     * @param worldposition location in worldspace
     * @return corresponding location in viewspace
     */
    public Vector convertWorldToView(Vector worldposition) {
        Point2D.Double point = new Point2D.Double(worldposition.getX(), worldposition.getY());
        _worldToView.transform(point, point);
        return new Vector(point.x, point.y);
    }

    /**
     * Computes the world-space text box that encloses the provided text.
     *
     * @param position anchor position for the text
     * @param text text for which to compute the containing box
     * @return new rectangle enclosing the text in worldspace
     */
    public Rectangle getTextBox(Vector position, String text) {
        Rectangle2D rect = _graphics.getFontMetrics().getStringBounds(text, _graphics);
        // NB: flip Y axis
        Rectangle textbox = new Rectangle(rect.getMinX(), rect.getMaxX(), -rect.getMaxY(), -rect.getMinY());

        Vector baselinePosition;
        if (_anchor == TextAnchor.BASELINE) {
            baselinePosition = position;
        } else {
            double relbaseline = position.getY() - textbox.getBottom();
            baselinePosition = Vector.subtract(position, _anchor.getPositionFor(textbox,relbaseline));
        }
        textbox.translate(baselinePosition);
        return textbox;
    }
    ///</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public Graphics2D getRenderObject() {
        return _graphics;
    }

    @Override
    public void initialize() throws IOException {
        _graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        _graphics.setTransform(_screenToView);
        _graphics.transform(_worldToView);
    }

    @Override
    public BufferedImage closeWithResult() throws IOException {
        _graphics.dispose();
        if (_file != null) {
            ImageIO.write(_img, _file.getName().substring(_file.getName().lastIndexOf(".") + 1), _file);
        }
        return _img;
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        for (GeometryConvertable gc : geometries) {
            if (gc == null) {
                continue;
            }

            BaseGeometry geom = gc.toGeometry();
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
    }

    @Override
    public void draw(Vector location, String text) {
        _graphics.setColor(_strokecolor);

        // to get the best rendering, we're going to undo view->world transformation, so find out the view location
        location = convertWorldToView(location);

        Vector baselinePosition;
        if (_anchor == TextAnchor.BASELINE) {
            baselinePosition = location;
        } else {
            Rectangle2D rect = _graphics.getFontMetrics().getStringBounds(text, _graphics);
            // NB: flip Y axis?
            Rectangle textbox = new Rectangle(rect.getMinX(), rect.getMaxX(), -rect.getMaxY(), -rect.getMinY());
            baselinePosition = Vector.subtract(location, _anchor.getPositionFor(textbox,0));
        }

        AffineTransform oldtransform = _graphics.getTransform();
        _graphics.setTransform(_screenToView);
        // make sure not to render the text upside down: we must reverse the vertical axis flip
        _graphics.transform(new AffineTransform(new double[]{1, 0, 0, -1, baselinePosition.getX(), baselinePosition.getY()}));
        _graphics.drawString(text, 0, 0);
        _graphics.setTransform(oldtransform);
    }

    @Override
    public void setAlpha(double alpha) {
        super.setAlpha(alpha);
        _graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        super.setTextStyle(anchor, textsize);
        updateStyleObjects(false, true);
    }
    
    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        super.setTextStyle(anchor, textsize, fontstyle);
        updateStyleObjects(false, true);
    }

    @Override
    public void setSizeMode(SizeMode sizeMode) {
        super.setSizeMode(sizeMode);
        updateStyleObjects(true, true);
    }

    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        super.setStroke(color, strokewidth, dash);
        updateStyleObjects(true, false);
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        _renderTransforms.add(_graphics.getTransform());
        _graphics.transform(transform);
    }

    @Override
    public void popMatrix() {
        _graphics.setTransform(_renderTransforms.remove(_renderTransforms.size() - 1));
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        _renderClips.add(_graphics.getClip());
        _graphics.clip(convert(geometry.toGeometry()));
    }

    @Override
    public void popClipping() {
        _graphics.setClip(_renderClips.remove(_renderClips.size() - 1));
    }

    @Override
    public void pushGroup() {
        // no effect
    }

    @Override
    public void popGroup() {
        // no effect
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void renderGlyph(Glyph glyphstyle, double scalefactor, BaseGeometry representation) {
        Shape shape = convert(representation);

        Color glyphfillcolor;
        switch (glyphstyle.getFillMode()) {
            case FILL:
                glyphfillcolor = _fillcolor;
                _graphics.setColor(_fillcolor);
                break;
            case FIXED:
                glyphfillcolor = glyphstyle.getFillColor();
                break;
            case STROKE:
                glyphfillcolor = _strokecolor;
                break;
            default:
            case CLEAR:
                glyphfillcolor = null;
                break;
        }
        if (glyphfillcolor != null) {
            _graphics.setColor(glyphfillcolor);
            _graphics.fill(shape);
        }

        Color glyphstrokecolor;
        switch (glyphstyle.getStrokeMode()) {
            case FIXED:
                glyphstrokecolor = glyphstyle.getStrokeColor();
                break;
            case STROKE:
                glyphstrokecolor = _strokecolor;
                break;
            default:
            case CLEAR:
                glyphstrokecolor = null;
                break;
        }
        if (glyphstrokecolor != null) {
            switch (glyphstyle.getStrokeWidthMode()) {
                case FIXED:
                    _graphics.setStroke(getUndashedStroke(scalefactor * glyphstyle.getStrokeWidth()));
                    break;
                case STROKE:
                    _graphics.setStroke(getUndashedStroke(_strokewidth));
                    break;
            }
            _graphics.setColor(glyphstrokecolor);
            _graphics.draw(shape);
            _graphics.setStroke(_dashedstroke);
        }
    }

    private void renderPoint(Vector point) {
        if (_pointstyle == null) {
            return;
        }

        BaseGeometry representation = _pointstyle.represent(point, size(_pointsize));
        renderGlyph(_pointstyle, _pointsize, representation);
    }

    private void renderGeometry(BaseGeometry geom) {
        // assume that this isn't a Vector or a GeometryGroup

        if ((_strokecolor == null || _strokewidth == 0) && _fillcolor == null) {
            return;
        }

        Shape shape = convert(geom);
        if (shape == null) {
            return;
        }

        // can we fill it?
        if (_fillcolor != null && (geom.getGeometryType().isCyclic() || geom.getGeometryType() == GeometryType.GEOMETRYGROUP)) {
            renderFill(geom, shape);
        }

        if (_strokecolor != null) {
            _graphics.setColor(_strokecolor);
            _graphics.draw(shape);
        }

        if (geom.getGeometryType().isOrientable()) {
            renderArrowheads((OrientedGeometry) geom);
        }

    }

    private void renderArrowheads(OrientedGeometry geom) {
        if (_backwardArrow != null && _backwardArrowsize != 0) {
            Vector backdir = geom.getStartTangent();
            backdir.invert();

            BaseGeometry arrow = _backwardArrow.represent(geom.getStart(), backdir, size(_backwardArrowsize));
            renderGlyph(_backwardArrow, _backwardArrowsize, arrow);
        }

        if (_forwardArrow != null && _forwardArrowsize != 0) {
            Vector fwddir = geom.getEndTangent();
            BaseGeometry arrow = _forwardArrow.represent(geom.getEnd(), fwddir, size(_forwardArrowsize));
            renderGlyph(_forwardArrow, _forwardArrowsize, arrow);
        }
    }

    private void renderFill(BaseGeometry geom, Shape shape) {

        // assume fill color is set!
        if ((_strokecolor == null || _strokewidth == 0)
                && _fillcolor == null) {
            return;
        }

        if (_hash != null) {
            Shape oldclip = _graphics.getClip();
            _graphics.clip(shape);
            _graphics.setColor(_fillcolor);

            Pair<List<LineSegment>,Integer> hashures = _hash.computeHashures(geom, size(1));
            
            int pi = hashures.getSecond();
            for (LineSegment hashure : hashures.getFirst()) {
                _graphics.setStroke(getUndashedStroke(_hash.getPattern()[pi]));
                _graphics.draw(new Line2D.Double(
                        hashure.getStart().getX(), hashure.getStart().getY(),
                        hashure.getEnd().getX(), hashure.getEnd().getY()));
                pi = (pi + 2) % _hash.getPattern().length;
            }

            _graphics.setStroke(_dashedstroke);
            _graphics.setClip(oldclip);

        } else if (_fillcolor != null) {
            _graphics.setColor(_fillcolor);
            _graphics.fill(shape);
        }

        if (_strokecolor == null || _strokewidth == 0) {
            return;
        }

        _graphics.setColor(_strokecolor);
        _graphics.draw(shape);
    }

    private void renderComposite(BaseGeometry geom) {
        // only geometrygroup for now
        //draw(((GeometryGroup) geom).getParts());
        renderGeometry(geom);
    }

    private Shape convert(BaseGeometry geometry) {
        return AWTGeometryConversion.toAWTShape(geometry, infiniteClipBox());
    }

    private Rectangle infiniteClipBox() {
        Rectangle clipbox = _worldview.clone();
        clipbox.grow(3 * size(_strokewidth));
        return clipbox;
    }

    private double size(double givensize) {
        if (_sizeMode == SizeMode.WORLD) {
            return givensize;
        } else {
            return givensize / _worldToView.getScaleX();
        }
    }

    private double sizeViewbased(double givensize) {
        if (_sizeMode == SizeMode.WORLD) {
            return givensize * _worldToView.getScaleX();
        } else {
            return givensize;
        }
    }

    private Stroke getUndashedStroke(double strokewidth) {
        return new BasicStroke((float) size(strokewidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private void updateStyleObjects(boolean normalstroke, boolean text) {
        if (normalstroke) {
            if (_dash != null) {
                float[] pattern = new float[_dash.getPattern().length];
                for (int i = 0; i < pattern.length; i++) {
                    pattern[i] = (float) size(_dash.getPattern()[i]);
                }
                _dashedstroke = new BasicStroke((float) size(_strokewidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, pattern, 0.0f);
            } else {
                _dashedstroke = getUndashedStroke(_strokewidth);
            }
            _graphics.setStroke(_dashedstroke);
        }
        if (text) {
            int style;
            switch (_fontstyle) {
                default:
                case NORMAL:
                    style = Font.PLAIN;
                    break;
                case BOLD:
                    style = Font.BOLD;
                    break;
                case ITALICS:
                    style = Font.ITALIC;
                    break;
            }
            _graphics.setFont(new Font("SansSerif", style, (int) Math.round(sizeViewbased(_textsize))));
        }
    }
    //</editor-fold>
}
