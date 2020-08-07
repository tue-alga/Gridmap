/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io;

import java.awt.Color;
import java.util.Map;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

/**
 * Class to represent an object that has been read via some inherited class of
 * BaseReader.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ReadItem implements GeometryConvertable {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private BaseGeometry _geometry = null;
    private String _string = null;
    private double _strokewidth = 0;
    private Dashing _dash = null;
    private Color _stroke = null, _fill = null;
    private double _size = 1;
    private double _alpha = 1;
    private String _layer = null;
    private Map<String,String> _aux = null;
    private int _pagenumber = 1;
    private TextAnchor _anchor = TextAnchor.BASELINE;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public BaseGeometry toGeometry() {
        return _geometry;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">  
    public int getPageNumber() {
        return _pagenumber;
    }

    public void setPageNumber(int pageNumber) {
        _pagenumber = pageNumber;
    }

    public TextAnchor getAnchor() {
        return _anchor;
    }

    public void setAnchor(TextAnchor anchor) {
        _anchor = anchor;
    }
    
    public double getAlpha() {
        return _alpha;
    }

    public void setAlpha(double alpha) {
        _alpha = alpha;
    }

    public Dashing getDash() {
        return _dash;
    }

    public void setDash(Dashing dash) {
        _dash = dash;
    }

    public Color getFill() {
        return _fill;
    }

    public void setFill(Color fill) {
        _fill = fill;
    }

    public BaseGeometry getGeometry() {
        return _geometry;
    }

    public void setGeometry(BaseGeometry geometry) {
        _geometry = geometry;
    }

    public String getString() {
        return _string;
    }

    public void setString(String string) {
        _string = string;
    }

    public String getLayer() {
        return _layer;
    }

    public void setLayer(String layer) {
        _layer = layer;
    }

    public Color getStroke() {
        return _stroke;
    }

    public void setStroke(Color stroke) {
        _stroke = stroke;
    }

    public double getStrokewidth() {
        return _strokewidth;
    }

    public void setStrokewidth(double strokewidth) {
        _strokewidth = strokewidth;
    }

    public double getSymbolsize() {
        return _size;
    }

    public void setSymbolsize(double size) {
        _size = size;
    }    

    public Map<String, String> getAuxiliary() {
        return _aux;
    }    

    public void setAuxiliary(Map<String, String> aux) {
        _aux = aux;
    } 
    //</editor-fold>
}
