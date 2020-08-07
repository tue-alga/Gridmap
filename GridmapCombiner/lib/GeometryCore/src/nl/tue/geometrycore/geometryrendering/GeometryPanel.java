/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering;

import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.LayeredWriter;
import nl.tue.geometrycore.io.raster.RasterWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class GeometryPanel extends JPanel implements GeometryRenderer<Object>, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    // draw settings
    private GeometryRenderer _renderer;
    private Graphics2D _graphics;
    private boolean _antialiasing = true;
    private final AffineTransform _worldToView;
    // interaction
    private double _margin = 0.03;
    private double _zoomRate = 11.0 / 10.0;
    private int _animationStepDuration = 1000 / 50; // 50 FPS
    private boolean _defaultInteractionEnabled = true;
    // mouse events
    private int _mouseButton = MouseEvent.NOBUTTON;
    private Vector _mousePrevWorld;
    private Vector _mousePrevView;
    private Color _zoomboxColor = new Color(4, 162, 176);
    private Vector _zoomboxStart = null, _zoomboxEnd = null;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public GeometryPanel() {
        super(true); // enable doublebuffering

        setBackground(Color.white);
        _worldToView = new AffineTransform();

        addAllListeners();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void addAllListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public boolean isAntialiasing() {
        return _antialiasing;
    }

    public void setAntialiasing(boolean antialiasing) {
        _antialiasing = antialiasing;
    }

    public boolean isDefaultInteractionEnabled() {
        return _defaultInteractionEnabled;
    }

    public void setDefaultInteractionEnabled(boolean enableDefaultInteraction) {
        _defaultInteractionEnabled = enableDefaultInteraction;
    }

    public double getMargin() {
        return _margin;
    }

    public void setMargin(double margin) {
        _margin = margin;
    }

    public double getZoomRate() {
        return _zoomRate;
    }

    public void setZoomRate(double zoomRate) {
        _zoomRate = zoomRate;
    }

    public Color getZoomboxColor() {
        return _zoomboxColor;
    }

    public void setZoomboxColor(Color zoomboxColor) {
        _zoomboxColor = zoomboxColor;
    }

    public int getAnimationStepDuration() {
        return _animationStepDuration;
    }

    public void setAnimationStepDuration(int animationStepDuration) {
        _animationStepDuration = animationStepDuration;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RENDERING">
    @Override
    protected void paintComponent(Graphics g) {
        _graphics = (Graphics2D) g;
        try (RasterWriter writer = RasterWriter.graphicsWriter(_worldToView, getWidth(), getHeight(), _graphics)) {
            writer.initialize();
            render(writer);
        } catch (IOException ex) {
            Logger.getLogger(GeometryPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public GeometryRenderer getRenderer() {
        return _renderer;
    }

    public void render(GeometryRenderer renderer) {
        _renderer = renderer;

        drawScene();

        if (_zoomboxEnd != null) {
            setSizeMode(SizeMode.VIEW);
            setAlpha(0.7);
            setStroke(_zoomboxColor, 1, Dashing.SOLID);
            setFill(null, Hashures.SOLID);
            draw(Rectangle.byCorners(_zoomboxStart, _zoomboxEnd));
            // restore defaults...
            setSizeMode(SizeMode.WORLD);
            setAlpha(1);
        }
    }

    public void repaintNow() {
        paintImmediately(0, 0, getWidth(), getHeight());
    }
    //</editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="RENDERER INTERFACE">
    @Override
    public Object getRenderObject() {
        return _renderer.getRenderObject();
    }

    @Override
    public void draw(GeometryConvertable... geos) {
        _renderer.draw(geos);
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geos) {
        _renderer.draw(geos);
    }

    @Override
    public void draw(Vector location, String text) {
        _renderer.draw(location, text);
    }

    public void setLayer(String layer) {
        if (_renderer instanceof LayeredWriter) {
            ((LayeredWriter) _renderer).setLayer(layer);
        }
    }

    @Override
    public void setAlpha(double alpha) {
        _renderer.setAlpha(alpha);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        setTextStyle(anchor, textsize, FontStyle.NORMAL);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        _renderer.setTextStyle(anchor, textsize, fontstyle);
    }

    @Override
    public void setSizeMode(SizeMode sizeMode) {
        _renderer.setSizeMode(sizeMode);
    }

    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        _renderer.setStroke(color, strokewidth, dash);
    }

    @Override
    public void setFill(Color color, Hashures hash) {
        _renderer.setFill(color, hash);
    }

    @Override
    public void setPointStyle(PointStyle pointstyle, double pointsize) {
        _renderer.setPointStyle(pointstyle, pointsize);
    }

    @Override
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        _renderer.setBackwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        _renderer.setForwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        _renderer.pushMatrix(transform);
    }

    @Override
    public void popMatrix() {
        _renderer.popMatrix();
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        _renderer.pushClipping(geometry);
    }

    @Override
    public void popClipping() {
        _renderer.popClipping();
    }

    @Override
    public void pushGroup() {
        _renderer.pushGroup();
    }

    @Override
    public void popGroup() {
        _renderer.popGroup();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ABSTRACT SCENE">
    protected abstract void drawScene();

    public abstract Rectangle getBoundingRectangle();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="VIEW CONTROL">  
    public Vector convertViewToWorld(Vector worldPosition) {
        Point2D.Double point = new Point2D.Double(worldPosition.getX(), worldPosition.getY());
        try {
            _worldToView.inverseTransform(point, point);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GeometryPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Vector(point.x, point.y);
    }

    public double convertViewToWorld(double distance) {
        return distance / _worldToView.getScaleX();
    }

    public Vector convertWorldToView(Vector viewPosition) {
        Point2D.Double point = new Point2D.Double(viewPosition.getX(), viewPosition.getY());
        _worldToView.transform(point, point);
        return new Vector(point.x, point.y);
    }

    public double convertWorldToView(double distance) {
        return distance * _worldToView.getScaleX();
    }

    public void resetView() {
        _worldToView.setToIdentity();
        repaint();
    }

    public void zoom(double strength) {
        _worldToView.scale(strength, strength);
        repaint();
    }

    public void translate(Vector d) {
        translate(d.getX(), d.getY());
    }

    public void translate(double dx, double dy) {
        _worldToView.translate(dx, dy);
        repaint();
    }

    public void translateView(Vector d) {
        translateView(d.getX(), d.getY());
    }

    public void translateView(double dx, double dy) {
        _worldToView.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
        repaint();
    }

    public Rectangle getWorldview() {
        return Rectangle.byCorners(
                convertViewToWorld(new Vector(0, 0)),
                convertViewToWorld(new Vector(getWidth(), getHeight())));
    }

    public Rectangle getView() {
        return Rectangle.byCorners(new Vector(0, 0),
                new Vector(getWidth(), getHeight()));
    }

    public void zoomToFit() {
        zoomToFit(0);
    }

    public void zoomToFit(int animateduration) {
        zoomToBox(getBoundingRectangle(), animateduration);
    }

    public void zoomToBox(Rectangle box) {
        zoomToBox(box, 0);
    }

    public void zoomToBox(Rectangle box, int animateduration) {

        if (box != null && !box.isEmpty()) {

            Rectangle viewrect = new Rectangle(0, getWidth(), 0, getHeight());
            viewrect.scale(1 - _margin, viewrect.center());

            if (animateduration <= _animationStepDuration) {
                AffineTransformUtil.setWorldToView(_worldToView, box, viewrect);
            } else {
                AffineTransform target = new AffineTransform();
                AffineTransformUtil.setWorldToView(target, box, viewrect);

                // TODO: get animations to work again...
//                int nsteps = animateduration / _animationStepDuration;
//                
//                double[][] deltaVTW = new double[3][3];
//                double[][] deltaWTV = new double[3][3];
//
//                for (int row = 0; row < 3; row++) {
//                    for (int col = 0; col < 3; col++) {
//                        deltaVTW[row][col] = (target.getViewToWorld()[row][col] - _view.getViewToWorld()[row][col]);
//                        deltaWTV[row][col] = (target.getWorldToView()[row][col] - _view.getWorldToView()[row][col]);
//                    }
//                }
//
//                double[] smoothzooming = new double[nsteps];
//                double tot = 0;
//                if (deltaVTW[0][0] < 0) {
//                    // zooming in
//                    for (int i = 0; i < nsteps; i++) {
//                        smoothzooming[i] = 1 + i;
//                        tot += 1 + i;
//                    }
//                } else {
//                    // zooming out
//                    for (int i = 0; i < nsteps; i++) {
//                        smoothzooming[nsteps - i - 1] = 1 + i;
//                        tot += 1 + i;
//                    }
//                }
//                // normalize to sum to 1
//                for (int i = 0; i < nsteps; i++) {
//                    smoothzooming[i] /= tot;
//                }
//
//                for (int i = 0; i < nsteps - 1; i++) {
//
//                    for (int row = 0; row < 3; row++) {
//                        for (int col = 0; col < 3; col++) {
//                            _view.getViewToWorld()[row][col] += smoothzooming[i] * deltaVTW[row][col];
//                            _view.getWorldToView()[row][col] += smoothzooming[i] * deltaWTV[row][col];
//                        }
//                    }
//
//                    repaintNow();
//                    try {
//                        Thread.sleep(_animationStepDuration);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(GeometryDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
                _worldToView.setTransform(target);
            }
        } else {
            _worldToView.setToIdentity();
        }

        repaint();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="INTERACTION">
    public int getButtonCode(MouseEvent e) {
        if (isLeftMouseButton(e)) {
            return MouseEvent.BUTTON1;
        } else if (isRightMouseButton(e)) {
            return MouseEvent.BUTTON3;
        } else if (isMiddleMouseButton(e)) {
            return MouseEvent.BUTTON2;
        } else {
            return MouseEvent.NOBUTTON;
        }
    }

    private Vector convertScreenToView(MouseEvent e) {
        return new Vector(e.getX(), getHeight() - e.getY());
    }

    protected boolean isLeftMouseButton(MouseEvent e) {
        return (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0;
    }

    protected boolean isMiddleMouseButton(MouseEvent e) {
        return (e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0;
    }

    protected boolean isRightMouseButton(MouseEvent e) {
        return (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;
    }

    protected boolean isCtrlDown(InputEvent e) {
        return (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
    }

    protected boolean isShiftDown(InputEvent e) {
        return (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
    }

    protected boolean isAltDown(InputEvent e) {
        return (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;
    }

    protected abstract void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt);

    protected void mouseRelease(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
    }

    protected void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {
    }

    protected void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
    }

    protected void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {
    }

    protected abstract void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt);

    protected void keyRelease(int keycode, boolean ctrl, boolean shift, boolean alt) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocus();

        _mousePrevView = convertScreenToView(e);
        Vector loc = convertViewToWorld(_mousePrevView);
        _mouseButton = getButtonCode(e);

        if (_defaultInteractionEnabled) {
            if (_mouseButton == MouseEvent.BUTTON2) {
                _zoomboxStart = loc;
                _zoomboxEnd = loc;
            } else {
                _zoomboxStart = null;
                _zoomboxEnd = null;
            }
        }

        // forward
        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        // forward
        mousePress(loc, _mouseButton, ctrl, shift, alt);

        // store last loc
        _mousePrevWorld = loc;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        _mousePrevView = convertScreenToView(e);
        Vector loc = convertViewToWorld(_mousePrevView);

        if (_defaultInteractionEnabled) {
            if (_mouseButton == MouseEvent.BUTTON2 && _zoomboxStart != null) {
                Rectangle box = Rectangle.byCorners(_zoomboxStart, loc);
                zoomToBox(box);
            }
            _zoomboxStart = null;
            _zoomboxEnd = null;
        }

        // forward
        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        // forward
        mouseRelease(loc, _mouseButton, ctrl, shift, alt);

        // store last loc
        _mousePrevWorld = loc;
        _mouseButton = MouseEvent.NOBUTTON;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (_defaultInteractionEnabled) {
            if (_mouseButton == MouseEvent.BUTTON3) {
                // pan
                Vector newmouse = convertScreenToView(e);
                double dx = newmouse.getX() - _mousePrevView.getX();
                double dy = newmouse.getY() - _mousePrevView.getY();

                translateView(dx, dy);
            }
        }

        _mousePrevView = convertScreenToView(e);
        Vector loc = convertViewToWorld(_mousePrevView);

        if (_defaultInteractionEnabled) {
            if (_mouseButton == MouseEvent.BUTTON2 && _zoomboxStart != null) {
                _zoomboxEnd = loc;
                repaint();
            } else {
                _zoomboxStart = null;
                _zoomboxEnd = null;
            }
        }

        // forward
        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        mouseDrag(loc, _mousePrevWorld, _mouseButton, ctrl, shift, alt);

        // store last loc
        _mousePrevWorld = loc;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        _mousePrevView = convertScreenToView(e);
        Vector loc = convertViewToWorld(_mousePrevView);

        // forward
        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        mouseMove(loc, _mouseButton, ctrl, shift, alt);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        _mousePrevView = convertScreenToView(e);
        Vector preWorld = convertViewToWorld(_mousePrevView);

        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        if (_defaultInteractionEnabled && !(ctrl || shift || alt)) {
            double factor = e.getWheelRotation() < 0 ? _zoomRate : 1.0 / _zoomRate;

            zoom(factor);

            Vector postViewport = convertWorldToView(preWorld);
            Vector diff = Vector.subtract(_mousePrevView, postViewport);

            translateView(diff);
        }

        // forward
        mouseWheelMove(preWorld, e.getWheelRotation(), ctrl, shift, alt);

        // store last loc
        _mousePrevWorld = preWorld;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                if (_defaultInteractionEnabled) {
                    if (isShiftDown(e)) {
                        resetView();
                    } else {
                        zoomToFit();
                    }
                    break;
                }
            default:
                // forward
                boolean ctrl = isCtrlDown(e);
                boolean shift = isShiftDown(e);
                boolean alt = isAltDown(e);

                keyPress(e.getKeyCode(), ctrl, shift, alt);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // forward
        boolean ctrl = isCtrlDown(e);
        boolean shift = isShiftDown(e);
        boolean alt = isAltDown(e);

        keyRelease(e.getKeyCode(), ctrl, shift, alt);
    }
    //</editor-fold>
}
