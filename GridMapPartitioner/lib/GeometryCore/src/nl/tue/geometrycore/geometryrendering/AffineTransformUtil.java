/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering;

import java.awt.geom.AffineTransform;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class AffineTransformUtil {
    
     public static Rectangle setWorldToView(AffineTransform transform, Rectangle world, Rectangle view) {        
        transform.setToIdentity();
        
        if (world != null && !world.isEmpty()) {            
            
            // make sure not to change given box, the modified one is returned
            Rectangle worldclone = world.clone();
            if (worldclone.isSingleton()) {
                worldclone.setWidth(view.width());
                worldclone.setHeight(view.height());
            } else {
                worldclone.growToAspectRatio(view.width() / view.height());
            }

            // NB: aspect ratios of the boxes are equal
            double factor = view.width() / worldclone.width();

            // zoom keeping left bottom in place
            // set leftbottom to origin
            transform.translate(view.getLeft(), view.getBottom());

            // zoom with desired strength
            transform.scale(factor, factor);

            // move leftbottom to target position
            transform.translate(-worldclone.getLeft(), -worldclone.getBottom());
            
            return worldclone;
        } else {
            return null;
        }
        
    }
}
