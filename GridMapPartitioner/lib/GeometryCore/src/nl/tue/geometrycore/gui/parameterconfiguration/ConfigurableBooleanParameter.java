/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ConfigurableBooleanParameter extends ConfigurableParameter<Boolean> {

    public ConfigurableBooleanParameter(String label, boolean defaultvalue) {
        super(label, defaultvalue);
    }

    @Override
    public boolean setValueFromString(String valuestring) {
        try {
            boolean b = Boolean.parseBoolean(valuestring);
            setValue(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateInterface() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
