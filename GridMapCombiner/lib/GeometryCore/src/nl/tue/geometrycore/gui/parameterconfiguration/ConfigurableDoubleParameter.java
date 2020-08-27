/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ConfigurableDoubleParameter extends ConfigurableParameter<Double> {

    private final double _minValue, _maxValue;

    public ConfigurableDoubleParameter(String label, double defaultvalue) {
        this(label, defaultvalue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public ConfigurableDoubleParameter(String label, double defaultvalue, double minValue, double maxValue) {
        super(label, defaultvalue);
        _minValue = minValue;
        _maxValue = maxValue;
    }

    @Override
    public void setValue(Double value) {
        if (value < _minValue || value > _maxValue) {
            Logger.getLogger(ConfigurableDoubleParameter.class.getName()).log(Level.WARNING, "Setting value {0} for parameter {1} which is out of specified bounds.", new Object[]{value, getLabel()});
        }
        super.setValue(value);
    }

    @Override
    public boolean setValueFromString(String valuestring) {
        try {
            double d = Double.parseDouble(valuestring);
            if (d < _minValue || d > _maxValue) {
                return false;
            }
            setValue(d);
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
