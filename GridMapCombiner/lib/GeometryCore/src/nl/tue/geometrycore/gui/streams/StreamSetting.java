/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.streams;

/**
 * Configures to which output to direct strings written to System.out and
 * System.err.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public enum StreamSetting {

    /**
     * Write neither to the GUI nor to the console.
     */
    NONE,
    /**
     * Write only to the GUI.
     */
    GUI,
    /**
     * Write only to the console.
     */
    CONSOLE,
    /**
     * Write both to the GUI and to the console.
     */
    BOTH;

    //<editor-fold defaultstate="collapsed" desc="INTERNAL">
    boolean isSubSetOf(StreamSetting superset) {
        if (superset == BOTH) {
            return true;
        }
        switch (this) {
            case NONE:
                return true;
            case BOTH:
                return false;
            default:
                return this == superset;
        }
    }

    boolean containsGUI() {
        return this == GUI || this == BOTH;
    }

    boolean containsSystem() {
        return this == CONSOLE || this == BOTH;
    }

    StreamSetting changeGUI(boolean enabled) {
        if (enabled) {
            switch (this) {
                default:
                case NONE:
                case GUI:
                    return GUI;
                case BOTH:
                case CONSOLE:
                    return BOTH;
            }
        } else {
            switch (this) {
                default:
                case NONE:
                case GUI:
                    return NONE;
                case BOTH:
                case CONSOLE:
                    return CONSOLE;
            }
        }

    }

    StreamSetting changeSystem(boolean enabled) {
        if (enabled) {
            switch (this) {
                default:
                case NONE:
                case CONSOLE:
                    return CONSOLE;
                case BOTH:
                case GUI:
                    return BOTH;
            }
        } else {
            switch (this) {
                default:
                case NONE:
                case CONSOLE:
                    return NONE;
                case BOTH:
                case GUI:
                    return GUI;
            }
        }
    }
    //</editor-fold>
}
