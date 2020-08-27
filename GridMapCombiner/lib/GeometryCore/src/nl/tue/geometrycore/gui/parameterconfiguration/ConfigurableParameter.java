/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class ConfigurableParameter<TValue> {
    
    private final String _label;
    private final TValue _default;
    private TValue _value;
    
    public ConfigurableParameter(String label, TValue defaultvalue) {
        _label = label;
        _value = defaultvalue;
        _default = defaultvalue;        
    }    
    
    public String getLabel() {
        return _label;
    }
    
    @Override 
    public String toString() {
        return _label + "(" +getValueAsString()+ ")";
    }
    
    public void setValue(TValue value) {
        _value = value;
        updateInterface();
    }
    
    public TValue getValue() {
        return _value;
    }
    
    public void restoreDefault() {
        setValue(_default);
    }
    
    public String getValueAsString() {
        return _value.toString();
    }
    
    public abstract boolean setValueFromString(String valuestring);
    
    public abstract void updateInterface();
    
    public void addToTab(SideTab tab) {
        tab.makeCustomSplit(4, 0.7, 0.3);
        tab.addLabel(getLabel());
        tab.addButton("D", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                restoreDefault();
            }
        });
    }
     
    public void promptForNewValue(JComponent parent) {
        String result = JOptionPane.showInputDialog(parent, _label, getValueAsString());
        while (result != null) {
            if (!setValueFromString(result)) {
                result = JOptionPane.showInputDialog(parent, _label + " - INVALID VALUE SPECIFIED", result);
            } else {
                break;
            }
        }
    }
    
}
