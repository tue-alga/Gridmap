/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum FrameLogo {

    DEFAULT {
        @Override
        public List<Image> getLogos() throws IOException {
            return null;
        }
    },
    
    GEOMETRYCORE {
        private List<Image> list = null;

        @Override
        public List<Image> getLogos() throws IOException {
            if (list == null) {
                list = new ArrayList();
                list.add(ImageIO.read(FrameLogo.class.getResource("/gc-logo-32.png")));
                list.add(ImageIO.read(FrameLogo.class.getResource("/gc-logo-64.png")));
                list.add(ImageIO.read(FrameLogo.class.getResource("/gc-logo-128.png")));
            }
            return list;
        }
    },
    
    AGA {
        private List<Image> list = null;

        @Override
        public List<Image> getLogos() throws IOException {
            if (list == null) {
                list = new ArrayList();
                list.add(ImageIO.read(FrameLogo.class.getResource("/aga-logo-32.png")));
                list.add(ImageIO.read(FrameLogo.class.getResource("/aga-logo-64.png")));
                list.add(ImageIO.read(FrameLogo.class.getResource("/aga-logo-128.png")));
            }
            return list;
        }
    };

    public abstract List<Image> getLogos() throws IOException;
}
