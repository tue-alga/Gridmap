/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.ipe;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;

/**
 * Support class for default named values for the IPE XML format.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IPEDefaults {

    //<editor-fold defaultstate="collapsed" desc="STATIC FIELDS">
    /**
     * Size discrepancy between the size of text in java2D rendering and the IPE
     * sizes. Text in IPE appears this factor bigger than in the RasterWriter,
     * if we were to use the same value.
     */
    public static double TEXT_DISCREPANCY = 4.864;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STATIC METHODS">
    /**
     * Default named colors, including the paired 10-color Set scheme from
     * ColorBrewer.
     *
     * @return new map with named colors
     */
    public static Map<String, Color> getColors() {
        Map<String, Color> map = new HashMap();
        
        // ipe default colors
        map.put("red", Color.red);
        map.put("green", Color.green);
        map.put("blue", Color.blue);
        map.put("yellow", Color.yellow);
        map.put("orange", new Color(255, (int) (255 * 0.647), 0));
        map.put("gold", new Color(255, (int) (255 * 0.843), 0));
        map.put("purple", new Color((int) (255 * 0.627), (int) (255 * 0.125), (int) (255 * 0.941)));
        map.put("gray", new Color((int) (255 * 0.745), (int) (255 * 0.745), (int) (255 * 0.745)));
        map.put("brown", new Color((int) (255 * 0.647), (int) (255 * 0.165), (int) (255 * 0.165)));
        map.put("navy", new Color(0, 0, (int) (255 * 0.502)));
        map.put("pink", new Color(255, (int) (255 * 0.753), (int) (255 * 0.796)));
        map.put("seagreen", new Color((int) (255 * 0.18), (int) (255 * 0.545), (int) (255 * 0.341)));
        map.put("turquoise", new Color((int) (255 * 0.251), (int) (255 * 0.878), (int) (255 * 0.816)));
        map.put("violet", new Color((int) (255 * 0.933), (int) (255 * 0.51), (int) (255 * 0.933)));
        map.put("darkblue", new Color(0, 0, (int) (255 * 0.545)));
        map.put("darkcyan", new Color(0, (int) (255 * 0.545), (int) (255 * 0.545)));
        map.put("darkgray", new Color((int) (255 * 0.663), (int) (255 * 0.663), (int) (255 * 0.663)));
        map.put("darkgreen", new Color(0, (int) (255 * 0.392), 0));
        map.put("darkmagenta", new Color((int) (255 * 0.545), 0, (int) (255 * 0.545)));
        map.put("darkorange", new Color(255, (int) (255 * 0.549), 0));
        map.put("darkred", new Color((int) (255 * 0.545), 0, 0));
        map.put("lightblue", new Color((int) (255 * 0.678), (int) (255 * 0.847), (int) (255 * 0.902)));
        map.put("lightcyan", new Color((int) (255 * 0.878), 255, 255));
        map.put("lightgray", new Color((int) (255 * 0.827), (int) (255 * 0.827), (int) (255 * 0.827)));
        map.put("lightgreen", new Color((int) (255 * 0.565), (int) (255 * 0.933), (int) (255 * 0.565)));
        map.put("lightyellow", new Color(255, 255, (int) (255 * 0.878)));
        map.put("black", Color.black);
        map.put("white", Color.white);
        
        // extended Color brewer colors
        map.put("CB light blue", ExtendedColors.lightBlue);
        map.put("CB dark blue", ExtendedColors.darkBlue);
        
        map.put("CB light red", ExtendedColors.lightRed);
        map.put("CB dark red", ExtendedColors.darkRed);
        
        map.put("CB light green", ExtendedColors.lightGreen);
        map.put("CB dark green", ExtendedColors.darkGreen);
        
        map.put("CB light orange", ExtendedColors.lightOrange);
        map.put("CB dark orange", ExtendedColors.darkOrange);
        
        map.put("CB light purple", ExtendedColors.lightPurple);
        map.put("CB dark purple", ExtendedColors.darkPurple);
        
        return map;
    }
    
    /**
     * Default symbol sizes.
     *
     * @return new map with named sizes
     */
    public static Map<String, Double> getSymbolSizes() {
        Map<String, Double> map = new HashMap();
        map.put("tiny", 1.1);
        map.put("small", 2.0);
        map.put("normal", 3.0);
        map.put("large", 5.0);
        return map;
    }
    
    /**
     * Default named strokewidths.
     *
     * @return new map with named strokewidths
     */
    public static Map<String, Double> getStrokeWidths() {
        Map<String, Double> map = new HashMap();
        map.put("normal", 0.4);
        map.put("heavier", 0.8);
        map.put("fat", 1.2);
        map.put("ultrafat", 2.0);
        return map;
    }
    
    /**
     * Default named transparencies. Note that all use of transparency must be
     * named in IPE.
     *
     * @return new map with named transparencies
     */
    public static Map<String, Double> getTransparencies() {
        Map<String, Double> map = new HashMap();
        map.put("opaque", 1.0);
        map.put("10%", 0.1);
        map.put("20%", 0.2);
        map.put("25%", 0.25);
        map.put("30%", 0.3);
        map.put("40%", 0.4);
        map.put("50%", 0.5);
        map.put("60%", 0.6);
        map.put("70%", 0.7);
        map.put("70%", 0.75);
        map.put("80%", 0.8);
        map.put("90%", 0.9);
        map.put("100%", 1.0);
        return map;
    }
    
    /**
     * Default named dash styles, as well as the dashing style that are
     * width-appropriate.
     *
     * @return new map with named dash styles
     */
    public static Map<String, Dashing> getDashStyles() {
        Map<String, Dashing> map = new HashMap();
        
        // standard ipe dashing
        map.put("normal", Dashing.SOLID);
        map.put("dashed", new Dashing(4));
        map.put("dotted", new Dashing(1, 3));
        map.put("dash dotted", new Dashing(4, 2, 1, 2));
        map.put("dash dot dotted", new Dashing(4, 2, 1, 2, 1, 2));
        
        // extended dashing
        map.put("W dashed normal", new Dashing(1, 1.7));
        map.put("W dashed heavier", new Dashing(2, 3));
        map.put("W dashed fat", new Dashing(3, 5.1));
        map.put("W dashed ultrafat", new Dashing(5, 8.5));
        
        map.put("W dot normal", new Dashing(0.01, 0.8));
        map.put("W dot heavier", new Dashing(0.01, 1.6));
        map.put("W dot fat", new Dashing(0.01, 2.4));
        map.put("W dot ultrafat", new Dashing(0.01, 4));
        
        return map;
    }
    //</editor-fold>
}
