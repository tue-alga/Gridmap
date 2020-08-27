/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.styling;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Provides options for text placement by specifying an "anchor". By specifying
 * the anchor, we can control how the text rectangle (and thus the text) is
 * placed with respect to the provided point. Read this as "the anchor is the
 * ... position of the text rectangle".
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum TextAnchor {

    /**
     * The anchor is on the baseline, on the left side.
     */
    BASELINE {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.getLeft(), baseline);
                }
            },
    /**
     * The anchor is on the baseline, but horizontally in the middle.
     */
    BASELINE_CENTER {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.center().getX(),baseline);
                }
            },
    /**
     * The anchor is on the baseline, on the right side.
     */
    BASELINE_RIGHT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.getRight(),baseline);
                }
            },
    /**
     * Anchor in the middle of the text.
     */
    CENTER {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return rect.center();
                }
            },
    /**
     * Anchor on the left side, but vertically in the middle.
     */
    LEFT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.getLeft(), rect.verticalCenter());
                }
            },
    /**
     * Anchor on the right side, but vertically in the middle.
     */
    RIGHT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.getRight(), rect.verticalCenter());
                }
            },
    /**
     * Anchor on the top side, but horizontally in the middle.
     */
    TOP {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.horizontalCenter(), rect.getTop());
                }
            },
    /**
     * Anchor on the bottom side, but horizontally in the middle.
     */
    BOTTOM {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return new Vector(rect.horizontalCenter(), rect.getBottom());
                }
            },
    /**
     * Anchor in the top-left corner.
     */
    TOP_LEFT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return rect.leftTop();
                }
            },
    /**
     * Anchor in the top-right corner.
     */
    TOP_RIGHT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return rect.rightTop();
                }
            },
    /**
     * Anchor in the bottom-left corner.
     */
    BOTTOM_LEFT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return rect.leftBottom();
                }
            },
    /**
     * Anchor in the bottom-right corner.
     */
    BOTTOM_RIGHT {
                @Override
                public Vector getPositionFor(Rectangle rect, double baseline) {
                    return rect.rightBottom();
                }
            };

    /**
     * Gets the position of this anchor, with respect to the provided rectangle.
     *
     * @param rect rectangle bounding the desired text
     * @param baseline vertical coordinate of the baseline
     * @return anchor position
     */
    public abstract Vector getPositionFor(Rectangle rect, double baseline);
}
