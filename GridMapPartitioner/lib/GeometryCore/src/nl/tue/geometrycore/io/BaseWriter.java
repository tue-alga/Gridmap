/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

/**
 * This class provides a base interface to write files that somehow store
 * geometric shapes in a text-based format. Derived classes will allow writing
 * specific file formats. General pattern of usage:
 *
 * 1. Construct a writer; 2. Call initialize(); 3. Draw geometries; 4. Call
 * close();
 *
 * After close, calling methods on an instance may cause unexpected behavior.
 * Some writers may provide additional configuration options in between any of
 * these steps. However, any settings must allow for default settings, making
 * the above pattern a correct way of using the writer.
 *
 * @param <TResult> type of object returned on closing the writer
 * @param <TRenderObject> type of underlying render object, for custom rendering; see {@link GeometryRenderer} for more details
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class BaseWriter<TResult, TRenderObject> implements GeometryRenderer<TRenderObject>, AutoCloseable {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    protected double _alpha = 1;
    protected SizeMode _sizeMode = SizeMode.WORLD;
    protected Color _strokecolor = null;
    protected double _strokewidth = 1;
    protected Dashing _dash = null;
    protected Color _fillcolor = null;
    protected Hashures _hash = null;
    protected PointStyle _pointstyle = null;
    protected double _pointsize = 1;
    protected ArrowStyle _backwardArrow = null;
    protected double _backwardArrowsize = 1;
    protected ArrowStyle _forwardArrow = null;
    protected double _forwardArrowsize = 1;
    protected TextAnchor _anchor = TextAnchor.BASELINE;
    protected double _textsize = 1;
    protected FontStyle _fontstyle = FontStyle.NORMAL;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="INTERFACE IMPLEMENTATION">
   @Override
   public void close() throws IOException {
       closeWithResult();
   }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="ABSTRACT METHODS">
    /**
     * Initializes the writer, by setting up a transformation from world to file
     * coordinate systems. NB: any file-specific initialization should be
     * handled here. Specific custom initializers should be optional.
     *
     * @throws java.io.IOException
     */
    public abstract void initialize() throws IOException;

    /**
     * Finalizes the writer, e.g. closing the file or sending its content to the
     * clipboard. This should be the last call made to this object. NB: any
     * other file-specific closing should be handled here. Specific custom
     * closing handlers should be optional.
     *
     * @return object that represents the result; this may return null
     * @throws java.io.IOException
     */
    public abstract TResult closeWithResult() throws IOException;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RENDERER">
    @Override
    public void setAlpha(double alpha) {
        _alpha = alpha;
    }
    
    @Override
    public void setSizeMode(SizeMode sizeMode) {
        _sizeMode = sizeMode;
    }
    
    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        _strokecolor = color;
        _strokewidth = strokewidth;
        _dash = dash;
    }
    
    @Override
    public void setFill(Color color, Hashures hash) {
        _fillcolor = color;
        _hash = hash;
    }
    
    @Override
    public void setPointStyle(PointStyle pointstyle, double pointsize) {
        _pointstyle = pointstyle;
        _pointsize = pointsize;
    }
    
    @Override
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        _backwardArrow = arrow;
        _backwardArrowsize = arrowsize;
    }
    
    @Override
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        _forwardArrow = arrow;
        _forwardArrowsize = arrowsize;
    }
    
    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        setTextStyle(anchor, textsize, FontStyle.NORMAL);
    }
    
    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        _anchor = anchor;
        _textsize = textsize;
        _fontstyle = fontstyle;
    }
    
    @Override
    public void draw(GeometryConvertable... geos) {
        draw(Arrays.asList(geos));
    }
    //</editor-fold>
}
