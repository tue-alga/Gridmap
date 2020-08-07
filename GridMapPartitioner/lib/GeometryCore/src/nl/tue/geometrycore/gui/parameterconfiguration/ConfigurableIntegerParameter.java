/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ConfigurableIntegerParameter extends ConfigurableParameter<Integer> {

    private final int _minValue, _maxValue;
    // interface
    private int _stepValue;
    private JSpinner _spinner;
    private JSlider _slider;
    private boolean _slidermode;

    public ConfigurableIntegerParameter(String label, int defaultvalue) {
        this(label, defaultvalue, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
    }

    public ConfigurableIntegerParameter(String label, int defaultvalue, int minValue, int maxValue) {
        this(label, defaultvalue, minValue, maxValue, 1);
    }

    public ConfigurableIntegerParameter(String label, int defaultvalue, int minValue, int maxValue, int stepValue) {
        super(label, defaultvalue);
        _minValue = minValue;
        _maxValue = maxValue;
        _stepValue = stepValue;
        _slidermode = false;
    }

    public ConfigurableIntegerParameter(String label, int defaultvalue, int minValue, int maxValue, int stepValue, boolean slidermode) {
        super(label, defaultvalue);
        _minValue = minValue;
        _maxValue = maxValue;
        _stepValue = stepValue;
        _slidermode = slidermode;
    }

    @Override
    public void setValue(Integer value) {
        if (value < _minValue || value > _maxValue) {
            Logger.getLogger(ConfigurableIntegerParameter.class.getName()).log(Level.WARNING, "Setting value {0} for parameter {1} which is out of specified bounds.", new Object[]{value, getLabel()});
            value = Math.min(_maxValue, Math.max(_minValue, value));
        }
        super.setValue(value);
    }

    @Override
    public boolean setValueFromString(String valuestring) {
        try {
            int val = Integer.parseInt(valuestring);
            setValue(val);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void addToTab(SideTab tab) {
        super.addToTab(tab);
        if (_slidermode) {
            _slider = tab.addIntegerSlider(getValue(), _minValue, _maxValue, (e, v) -> {
                setValue(v);
            });
        } else {
            _spinner = tab.addIntegerSpinner(_minValue, _minValue, _maxValue, _stepValue, (e, v) -> {
                setValue(v);
            });
        }
    }

    @Override
    public void updateInterface() {
        if (_slidermode) {
            _slider.setValue(getValue());
        } else {
            _spinner.setValue(getValue());
        }
    }

}
