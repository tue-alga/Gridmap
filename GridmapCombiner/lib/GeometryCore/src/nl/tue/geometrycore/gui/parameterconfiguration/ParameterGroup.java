/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

import java.util.List;
import javax.swing.JComponent;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 * Aggregate for easy processing of multiple parameters at the same time.
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class ParameterGroup {

    private final String _name;
    private final List<ConfigurableParameter> _parameters;

    public ParameterGroup(String name, List<ConfigurableParameter> parameters) {
        _name = name;
        _parameters = parameters;
    }

    public String getName() {
        return _name;
    }

    public List<ConfigurableParameter> getParameters() {
        return _parameters;
    }

    @Override
    public String toString() {
        String result = _name;

        if (!_parameters.isEmpty()) {
            result += "(" + _parameters.get(0).getValueAsString();
            for (int i = 1; i < _parameters.size(); i++) {
                result += "," + _parameters.get(i).getValueAsString();
            }
            result += ")";
        }

        return result;
    }

    public void promptForNewValues(JComponent parent) {
        for (ConfigurableParameter param : _parameters) {
            param.promptForNewValue(parent);
        }
    }

    public void restoreDefault() {
        for (ConfigurableParameter param : _parameters) {
            param.restoreDefault();
        }
    }
    
    public void addToTab(SideTab tab) {
        tab.addLabel(_name);
        for (ConfigurableParameter<?> param : _parameters) {
            param.addToTab(tab);
        }
    }
}
