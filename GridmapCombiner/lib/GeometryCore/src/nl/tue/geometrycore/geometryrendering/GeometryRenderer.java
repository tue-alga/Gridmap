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
import java.awt.geom.AffineTransform;
import java.util.Collection;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;

/**
 * This is interface should be used for rendering as much as possible. This
 * allows the same methods to be used to render to screen, or files, etc.
 *
 * @param <TRenderObject> type of object to that can be used for custom
 * rendering
 * @see {@link BaseRenderer}
 * @see {@link BaseWriter}
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface GeometryRenderer<TRenderObject> {

    //<editor-fold defaultstate="collapsed" desc="STYLING">
    /**
     * Configures the opacity, with 1 being fully opaque and 0 being fully
     * transparent.
     *
     * @param alpha value in [0,1]
     */
    public void setAlpha(double alpha);

    /**
     * Configures the appearance of text, through an anchor and text size. Font style defaults to normal text.
     *
     * @param anchor anchor for text placement
     * @param textsize size of text: world or screen space depends on
     * {@link #setSizeMode}
     */
    public void setTextStyle(TextAnchor anchor, double textsize);

    /**
     * Configures the appearance of text, through an anchor and text size, and
     * font style.
     *
     * @param anchor anchor for text placement
     * @param textsize size of text: world or screen space depends on
     * {@link #setSizeMode}
     * @param fontstyle which type of font to use
     */
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle);

    /**
     * Configures whether all style sizes (i.e., pointsize, arrowsize,
     * strokewidth, dashing, hashures, textsize) are configured in worldspace or
     * in viewspace.
     *
     * @param sizeMode new mode for sizing
     */
    public void setSizeMode(SizeMode sizeMode);

    /**
     * Configures the stroke style. Set color to null to disable stroke. Setting
     * the dash parameter to null gives a solid line.
     *
     * @param color stroke color
     * @param strokewidth width: world or screen space depends on
     * {@link #setSizeMode}
     * @param dash dashing style
     */
    public void setStroke(Color color, double strokewidth, Dashing dash);

    /**
     * Configures the fill color. Set color to null to disable fill. Set hash to
     * null to use a solid fill.
     *
     * @param color fill color
     * @param hash hashures: world or screen space depends on
     * {@link #setSizeMode}
     */
    public void setFill(Color color, Hashures hash);

    /**
     * Configures the point style and size.
     *
     * @param pointstyle point style
     * @param pointsize scaling factor: world or screen space depends on
     * {@link #setSizeMode}
     */
    public void setPointStyle(PointStyle pointstyle, double pointsize);

    /**
     * Configures the arrowhead for the start of oriented geometries. Set arrow
     * to null to disable this arrowhead.
     *
     * @param arrow style of the arrowhead
     * @param arrowsize scaling factor: world or screen space depends on
     * {@link #setSizeMode}
     */
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize);

    /**
     * Configures the arrowhead for the end of oriented geometries. Set arrow to
     * null to disable this arrowhead.
     *
     * @param arrow style of the arrowhead
     * @param arrowsize scaling factor: world or screen space depends on
     * {@link #setSizeMode}
     */
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="VIEW CONTROL"> 
    /**
     * Applies a transformation matrix to the renderer. Calls to
     * {@link #pushMatrix} and {@link #popMatrix}, {@link #pushClipping} and
     * {@link #popClipping}, and {@link #pushGroup} and {@link #popGroup} should
     * follow a combined proper nesting structure.
     *
     * @param transform
     */
    public void pushMatrix(AffineTransform transform);

    /**
     * Removes the last applied view transformation. Calls to
     * {@link #pushMatrix} and {@link #popMatrix}, {@link #pushClipping} and
     * {@link #popClipping}, and {@link #pushGroup} and {@link #popGroup} should
     * follow a combined proper nesting structure.
     */
    public void popMatrix();

    /**
     * Adds a clipping path to the current rendering. Calls to
     * {@link #pushMatrix} and {@link #popMatrix}, {@link #pushClipping} and
     * {@link #popClipping}, and {@link #pushGroup} and {@link #popGroup} should
     * follow a combined proper nesting structure.
     *
     * @param geometry clipping path
     */
    public void pushClipping(GeometryConvertable geometry);

    /**
     * Removes the most-recently adding clipping path. Calls to
     * {@link #pushMatrix} and {@link #popMatrix}, {@link #pushClipping} and
     * {@link #popClipping}, and {@link #pushGroup} and {@link #popGroup} should
     * follow a combined proper nesting structure.
     */
    public void popClipping();

    /**
     * Combines all following draw operations into a single group, until
     * {@link #popGroup} is called. Calls to {@link #pushMatrix} and
     * {@link #popMatrix}, {@link #pushClipping} and {@link #popClipping}, and
     * {@link #pushGroup} and {@link #popGroup} should follow a combined proper
     * nesting structure.
     */
    public void pushGroup();

    /**
     * Ends the most recently started group. Calls to {@link #pushMatrix} and
     * {@link #popMatrix}, {@link #pushClipping} and {@link #popClipping}, and
     * {@link #pushGroup} and {@link #popGroup} should follow a combined proper
     * nesting structure.
     */
    public void popGroup();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RENDERING">
    /**
     * Renders the given geometries using the current rendering settings.
     *
     * @param geometries geometries to be drawn
     */
    public void draw(Collection<? extends GeometryConvertable> geometries);

    /**
     * Renders the given geometries using the current rendering settings.
     *
     * @param geometries geometries to be drawn
     */
    public void draw(GeometryConvertable... geometries);

    /**
     * Renders the given text at the specified location.
     *
     * @param location position of the text anchor
     * @param text string of text to be rendered
     */
    public void draw(Vector location, String text);
    //</editor-fold>

    /**
     * Returns the render object used by this renderer. This object can be used
     * for more customized render handling, but breaks from the genericity of
     * renderers. For advanced use only. May return null.
     *
     * @return object for custom rendering
     */
    public TRenderObject getRenderObject();
}
